package org.strongcat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.strongcat.data.Notification;

@Getter
@Setter
@AllArgsConstructor
public class LogSearchResult{
    Notification criteria;
    long count;
}