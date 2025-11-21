package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for member status distribution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatusDistribution {
    private long active;
    private long inactive;
    private long retired;
    private long deceased;
    private Map<String, Long> byStatus;
}
