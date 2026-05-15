package org.strongcat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.strongcat.data.LogEntry;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class PagedLogResponse {
    private List<LogEntry> logs;
    private List<String> nextCursor;
}