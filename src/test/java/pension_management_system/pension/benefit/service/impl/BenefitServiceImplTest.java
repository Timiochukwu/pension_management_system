package pension_management_system.pension.benefit.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pension_management_system.pension.benefit.dto.BenefitCalculationResponse;
import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.Benefit;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.entity.BenefitType;
import pension_management_system.pension.benefit.mapper.BenefitMapper;
import pension_management_system.pension.benefit.repository.BenefitRepository;
import pension_management_system.pension.common.exception.InvalidBenefitException;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BenefitServiceImplTest {

    @Mock
    private BenefitRepository benefitRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private BenefitMapper benefitMapper;

    @InjectMocks
    private BenefitServiceImpl benefitService;

    private Member testMember;
    private Benefit testBenefit;
    private BenefitRequest testRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .memberId("MEM123")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1960, 1, 1)) // 64 years old
                .memberStatus(MemberStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusYears(10))
                .build();

        testBenefit = Benefit.builder()
                .id(1L)
                .referenceNumber("BEN123")
                .member(testMember)
                .benefitType(BenefitType.RETIREMENT)
                .status(BenefitStatus.PENDING)
                .totalContributions(BigDecimal.valueOf(100000))
                .netPayable(BigDecimal.valueOf(150000))
                .build();

        testRequest = BenefitRequest.builder()
                .memberId(1L)
                .benefitType(BenefitType.RETIREMENT)
                .paymentMethod("BANK_TRANSFER")
                .accountNumber("1234567890")
                .bankName("Test Bank")
                .build();
    }

    @Test
    void calculateBenefit_ForRetirement_ShouldReturnCalculation() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(contributionRepository.getTotalByMemberAndType(any(), eq(ContributionType.MONTHLY)))
                .thenReturn(BigDecimal.valueOf(50000));
        when(contributionRepository.getTotalByMemberAndType(any(), eq(ContributionType.VOLUNTARY)))
                .thenReturn(BigDecimal.valueOf(20000));

        // Act
        BenefitCalculationResponse result = benefitService.calculateBenefit(1L, BenefitType.RETIREMENT);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalContributions()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getEligibilityStatus()).isEqualTo("ELIGIBLE");
        verify(memberRepository).findById(1L);
    }

    @Test
    void applyForBenefit_WithValidData_ShouldSucceed() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(benefitRepository.existsByMemberAndStatus(any(), any())).thenReturn(false);
        when(contributionRepository.getTotalByMemberAndType(any(), any())).thenReturn(BigDecimal.valueOf(50000));
        when(benefitMapper.toEntity(any())).thenReturn(testBenefit);
        when(benefitRepository.save(any())).thenReturn(testBenefit);
        when(benefitMapper.toResponse(any())).thenReturn(new BenefitResponse());

        // Act
        BenefitResponse result = benefitService.applyForBenefit(testRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(benefitRepository).save(any());
    }

    @Test
    void applyForBenefit_WithExistingPendingBenefit_ShouldThrowException() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(benefitRepository.existsByMemberAndStatus(any(), eq(BenefitStatus.PENDING))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> benefitService.applyForBenefit(testRequest))
                .isInstanceOf(InvalidBenefitException.class)
                .hasMessageContaining("already has a pending or approved benefit");
    }

    @Test
    void approveBenefit_WithPendingStatus_ShouldSucceed() {
        // Arrange
        when(benefitRepository.findById(anyLong())).thenReturn(Optional.of(testBenefit));
        when(benefitRepository.save(any())).thenReturn(testBenefit);
        when(benefitMapper.toResponse(any())).thenReturn(new BenefitResponse());

        // Act
        BenefitResponse result = benefitService.approveBenefit(1L, "ADMIN");

        // Assert
        assertThat(result).isNotNull();
        verify(benefitRepository).save(any());
    }

    @Test
    void disburseBenefit_WithApprovedStatus_ShouldSucceed() {
        // Arrange
        testBenefit.setStatus(BenefitStatus.APPROVED);
        when(benefitRepository.findById(anyLong())).thenReturn(Optional.of(testBenefit));
        when(benefitRepository.save(any())).thenReturn(testBenefit);
        when(memberRepository.save(any())).thenReturn(testMember);
        when(benefitMapper.toResponse(any())).thenReturn(new BenefitResponse());

        // Act
        BenefitResponse result = benefitService.disburseBenefit(1L, "ADMIN");

        // Assert
        assertThat(result).isNotNull();
        verify(benefitRepository).save(any());
        verify(memberRepository).save(any()); // Member status updated to RETIRED
    }
}
