package pension_management_system.pension.payment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pension_management_system.pension.payment.dto.PaymentResponse;
import pension_management_system.pension.payment.entity.Payment;

/**
 * PaymentMapper - Convert between Payment entity and DTOs
 *
 * MapStruct automatically generates implementation
 * Converts Payment → PaymentResponse
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Convert Payment entity to PaymentResponse DTO
     *
     * Maps all matching fields automatically
     * Adds contribution.id as contributionId
     */
    @Mapping(source = "contribution.id", target = "contributionId")
    PaymentResponse toResponse(Payment payment);
}
