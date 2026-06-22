package org.strongcat.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailAlertContext {

    private String to;
    private String serviceName;
    private String logLevel;
    private String messageQuery;
    private long actualCount;
    private long thresholdCount;
}