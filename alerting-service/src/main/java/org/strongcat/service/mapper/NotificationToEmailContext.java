package org.strongcat.service.mapper;

import org.strongcat.data.EmailAlertContext;
import org.strongcat.data.Notification;

public class NotificationToEmailContext {

    public static EmailAlertContext buildEmailContext(Notification rule, long actualLogCount) {
        return new EmailAlertContext(
                rule.getEmail(),
                rule.getServiceName(),
                rule.getLevel(),
                rule.getTextQuery(),
                actualLogCount,
                rule.getThresholdCount()
        );
    }
}
