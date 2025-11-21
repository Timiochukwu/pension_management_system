package pension_management_system.pension.report.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pension_management_system.pension.report.dto.ReportRequest;
import pension_management_system.pension.report.dto.ReportResponse;
import pension_management_system.pension.report.entity.Report;

/**
 * ReportMapper - MapStruct mapper for Report entity
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "generatedAt", ignore = true)
    @Mapping(target = "filePath", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Report toEntity(ReportRequest request);

    ReportResponse toResponse(Report report);
}
