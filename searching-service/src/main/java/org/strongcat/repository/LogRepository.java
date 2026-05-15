package org.strongcat.repository;

import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;

import java.util.List;

public interface LogRepository {

    PagedLogResponse findByFieldAndValue(String fieldName, String queryText, int size, List<String> cursor);
}