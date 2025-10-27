package pension_management_system.pension.employer.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pension_management_system.pension.dto.EmployerRequest;
import pension_management_system.pension.dto.EmployerResponse;
import pension_management_system.pension.employer.entity.Employer;

@Mapper(componentModel = "spring")
public interface EmployerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employerId", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Employer toEntity(EmployerRequest request);

    @Mapping(target = "memberCount", expression = "java(employer.getMemberCount())")
    EmployerResponse toResponse(Employer employer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employerId", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(EmployerRequest request, @MappingTarget Employer employer);
}
