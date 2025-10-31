package pension_management_system.pension.contribution.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.entity.Contribution;

@Mapper(componentModel = "spring")
public interface ContributionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "referenceNumber", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "proccessAt", ignore = true)
    @Mapping(target = "proccessBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Contribution toEntity(ContributionRequest request);

    @Mapping(source = "member.id", target = "targetId")
    @Mapping(source = "member.firstName", target = "memberName", qualifiedByName ="getFullName")
    @Mapping(source = "member.memberId", target = "memberBusinessId")
    ContributionResponse toResponse(Contribution contribution);

    @Named("getFullName")
    default String getFullName(Contribution contribution) {
        if (contribution.getMember() == null){
            return null;
        }
        return contribution.getMember().getFirstName() + " " +
                contribution.getMember().getLastName();
    }

}
