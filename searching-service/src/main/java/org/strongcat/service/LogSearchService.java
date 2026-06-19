package org.strongcat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;
import org.strongcat.repository.LogRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogSearchService {

    private final LogRepository logRepository;

    public PagedLogResponse search(String fieldName, String query, int size, List<String> cursor) {

        log.info("Searching logs: size={}", size);
        return logRepository.findByFieldAndValue(fieldName, query, size, cursor);
    }
}