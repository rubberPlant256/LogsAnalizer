package org.strongcat.data;

public record EmailAlertContext(
    String to,
    String serviceName,
    String logLevel,
    String messageQuery,
    long actualCount,
    long thresholdCount
) {}