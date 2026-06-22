package org.strongcat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class NotificationListResponse {

    private List<NotificationResponse> notifications;

}