package pension_management_system.pension.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "https://.*", message = "Only HTTPS URLs allowed for security")
    private String url;

    @NotEmpty(message = "At least one event is required")
    private List<String> events;

    private String description;

    private Integer retryCount = 3;

    private Integer timeoutSeconds = 30;
}
