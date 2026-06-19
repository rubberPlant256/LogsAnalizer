package org.strongcat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.LogSearchRequest;
import org.strongcat.dto.PagedLogResponse;
import org.strongcat.service.LogSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogSearchController {

    private final LogSearchService logSearchService;

    @GetMapping("/search")
    public ResponseEntity<PagedLogResponse> search(LogSearchRequest request) {

        PagedLogResponse response = logSearchService.search(
                request.getFieldName(),
                request.getQuery(),
                request.getSize(),
                request.getCursor());

        return ResponseEntity.ok(response);
    }
}