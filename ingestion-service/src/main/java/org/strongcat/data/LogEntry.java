package org.strongcat.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
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

    @JsonIgnore
    @Builder.Default
    private Map<String, Object> extraFields = new HashMap<>();

    @JsonAnySetter
    public void addExtraField(String key, Object value) {
        if (this.extraFields == null) {
            this.extraFields = new HashMap<>();
        }
        this.extraFields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAllFields() {
        Map<String, Object> all = (extraFields != null)
                ? new HashMap<>(extraFields)
                : new HashMap<>();
        all.put("@timestamp", timestamp);
        all.put("level", level);
        all.put("logger_name", loggerName);
        all.put("thread_name", threadName);
        all.put("message", message);
        all.put("service", service);
        return all;
    }
}