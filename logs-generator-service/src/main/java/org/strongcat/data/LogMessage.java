package org.strongcat.data;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {

  private UUID id;
  private String traceId;
}
