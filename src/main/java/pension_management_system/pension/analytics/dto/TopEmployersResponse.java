package pension_management_system.pension.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopEmployersResponse {
    private List<EmployerData> topEmployers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployerData {
        private String employerId;
        private String companyName;
        private Long memberCount;
        private BigDecimal totalContributions;
    }
}
