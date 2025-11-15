package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatusDistribution {
    private List<StatusData> distribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusData {
        private String status;
        private Long count;
        private Double percentage;
    }
}
