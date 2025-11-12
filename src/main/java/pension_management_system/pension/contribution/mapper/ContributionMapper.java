    package pension_management_system.pension.contribution.mapper;


    import org.mapstruct.Mapper;
    import org.mapstruct.Mapping;
    import org.mapstruct.Named;
    import pension_management_system.pension.contribution.dto.ContributionRequest;
    import pension_management_system.pension.contribution.dto.ContributionResponse;
    import pension_management_system.pension.contribution.entity.Contribution;
    import pension_management_system.pension.member.entity.Member;

    @Mapper(componentModel = "spring")
    public interface ContributionMapper {

        // --------------------- Request → Entity ---------------------
        @Mapping(target = "id", ignore = true)
        @Mapping(target = "referenceNumber", ignore = true)
        @Mapping(target = "member", ignore = true)
        @Mapping(target = "status", ignore = true)
        @Mapping(target = "processedAt", ignore = true)
        @Mapping(target = "processedBy", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        Contribution toEntity(ContributionRequest request);

        // --------------------- Entity → Response ---------------------

        @Mapping(source = "member.id", target = "memberId")
        @Mapping(source = "member", target = "memberName", qualifiedByName ="getFullName")
        @Mapping(source = "member.memberId", target = "memberBusinessId")
        ContributionResponse toResponse(Contribution contribution);


        // --------------------- Helper Methods ---------------------
        @Named("getFullName")
        default String getFullName(Member member) {
            if (member == null) return null;
            return member.getFirstName() + " " + member.getLastName();
        }

    //    @Named("getFullName")
    //    default String getFullName(Contribution contribution) {
    //        if (contribution.getMember() == null){
    //            return null;
    //        }
    //        return contribution.getMember().getFirstName() + " " +
    //                contribution.getMember().getLastName();
    //    }

    }
