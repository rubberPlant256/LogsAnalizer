package org.strongcat.dto;

import jakarta.annotation.Nullable;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogSearchRequest {
    @Nullable
    private String fieldName;
    @Nullable
    private String query;

    private int size = 20;
    @Nullable
    private List<String> cursor;
}