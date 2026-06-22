package org.strongcat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.strongcat.data.EmailAlertContext;
import org.strongcat.data.Notification;
import org.strongcat.dto.LogSearchResult;
import org.strongcat.event.NotificationChangedEvent;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static org.strongcat.service.mapper.NotificationToEmailContext.buildEmailContext;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogPollingScheduler {

    private final ConcurrentTaskScheduler taskScheduler;
    private final OpenSearchBatchQueryService batchQueryService;
    private final EmailSenderService emailSenderService;
    private final NotificationService notificationService;

    private final Map<Integer, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    @EventListener(ContextRefreshedEvent.class)
    public void initSchedulers() {
        scheduleActiveRules();
    }

    @EventListener
    public void handleRulesChanged(NotificationChangedEvent event) {
        log.info("Received notification changed event. Re-scheduling");
        scheduleActiveRules();
    }

    public synchronized void scheduleActiveRules() {
        log.info("Refreshing virtual thread log polling schedulers");

        List<Notification> allActiveRules = notificationService.findAllActiveRules();

        Map<Integer, List<Notification>> rulesByInterval = allActiveRules.stream()
                .collect(Collectors.groupingBy(Notification::getPollingIntervalSeconds));

        cancelObsoleteTasks(rulesByInterval.keySet());
        registerNewTasks(rulesByInterval.keySet());
    }

    private void cancelObsoleteTasks(Set<Integer> activeIntervalsInDb) {
        activeTasks.keySet().forEach(interval -> {
            if (!activeIntervalsInDb.contains(interval)) {
                log.info("Canceling virtual scheduler for interval {}s", interval);
                activeTasks.get(interval).cancel(false);
                activeTasks.remove(interval);
            }
        });
    }

    private void registerNewTasks(Set<Integer> activeIntervalsInDb) {
        for (Integer intervalSeconds : activeIntervalsInDb) {
            if (activeTasks.containsKey(intervalSeconds)) {
                continue;
            }

            log.info("Registering new virtual thread scheduler for interval: {}s", intervalSeconds);

            ScheduledFuture<?> scheduledTask = taskScheduler.scheduleAtFixedRate(
                    () -> runPollingForInterval(intervalSeconds),
                    Duration.ofSeconds(intervalSeconds)
            );

            activeTasks.put(intervalSeconds, scheduledTask);
        }
    }

    private void runPollingForInterval(Integer intervalSeconds) {

        List<Notification> rulesForThisInterval = notificationService.findActiveRulesByInterval(intervalSeconds);
        if (rulesForThisInterval.isEmpty()) {
            return;
        }

        List<LogSearchResult> batchResults = batchQueryService.executeMultiSearch(rulesForThisInterval, intervalSeconds);

        processSearchResults(batchResults);
    }

    private void processSearchResults(List<LogSearchResult> batchResults) {
        for (LogSearchResult result : batchResults) {
            Notification rule = result.getCriteria();
            long actualLogCount = result.getCount();

            if (isThresholdExceeded(actualLogCount, rule.getThresholdCount())) {
                log.warn("🚨 Threshold exceeded for service {}! Found: {}", rule.getServiceName(), actualLogCount);

                EmailAlertContext emailContext = buildEmailContext(rule, actualLogCount);
                emailSenderService.sendAlert(emailContext);
            }
        }
    }

    private boolean isThresholdExceeded(long actualCount, int thresholdCount) {
        return actualCount >= thresholdCount;
    }


}