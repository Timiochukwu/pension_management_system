package pension_management_system.pension.benefit.mapper;

import org.mapstruct.*;
import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.Benefit;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BenefitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "referenceNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalContributions", ignore = true)
    @Mapping(target = "employerContributions", ignore = true)
    @Mapping(target = "investmentReturns", ignore = true)
    @Mapping(target = "calculatedBenefit", ignore = true)
    @Mapping(target = "taxDeductions", ignore = true)
    @Mapping(target = "administrativeFees", ignore = true)
    @Mapping(target = "netPayable", ignore = true)
    @Mapping(target = "approvalDate", ignore = true)
    @Mapping(target = "disbursementDate", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "disbursedBy", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Benefit toEntity(BenefitRequest request);

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.memberId", target = "memberIdNumber")
    @Mapping(target = "memberName", expression = "java(benefit.getMember().getFirstName() + \" \" + benefit.getMember().getLastName())")
    BenefitResponse toResponse(Benefit benefit);

    /**
     * Update existing entity from request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "referenceNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalContributions", ignore = true)
    @Mapping(target = "employerContributions", ignore = true)
    @Mapping(target = "investmentReturns", ignore = true)
    @Mapping(target = "calculatedBenefit", ignore = true)
    @Mapping(target = "taxDeductions", ignore = true)
    @Mapping(target = "administrativeFees", ignore = true)
    @Mapping(target = "netPayable", ignore = true)
    @Mapping(target = "approvalDate", ignore = true)
    @Mapping(target = "disbursementDate", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "disbursedBy", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(BenefitRequest request, @MappingTarget Benefit benefit);
}
