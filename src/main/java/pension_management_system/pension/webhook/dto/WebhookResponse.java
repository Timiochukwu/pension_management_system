package pension_management_system.pension.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {
    private Long id;
    private String url;
    private List<String> events;
    private Boolean active;
    private String description;
    private String secret; // Only shown once during creation
    private Integer failureCount;
    private LocalDateTime lastTriggeredAt;
    private LocalDateTime createdAt;
}
