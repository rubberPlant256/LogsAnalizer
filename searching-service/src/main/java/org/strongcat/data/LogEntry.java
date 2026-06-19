package org.strongcat.data;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @JsonProperty("@timestamp")
    private String timestamp;

    private String level;
    
    @JsonProperty("logger_name")
    private String loggerName;

    @JsonProperty("thread_name")
    private String threadName;

    private String message;
    private String service;

    @Setter(AccessLevel.NONE)
    private Map<String, Object> extraFields = new HashMap<>();

    @JsonAnySetter
    public void addExtraField(String key, Object value) {
        if (this.extraFields == null) {
            this.extraFields = new HashMap<>();
        }
        this.extraFields.put(key, value);
    }
}