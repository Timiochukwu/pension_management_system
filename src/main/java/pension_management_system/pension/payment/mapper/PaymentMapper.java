package pension_management_system.pension.payment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pension_management_system.pension.payment.dto.PaymentResponse;
import pension_management_system.pension.payment.entity.Payment;

/**
 * Mapper for Payment entity to DTO conversion
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "contribution.id", target = "contributionId")
    PaymentResponse toResponse(Payment payment);
}
