package pension_management_system.pension.contribution.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pension_management_system.pension.common.exception.InvalidContributionException;
import pension_management_system.pension.common.exception.MemberNotFoundException;
import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.mapper.ContributionMapper;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContributionServiceImplTest {

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ContributionMapper contributionMapper;

    @InjectMocks
    private ContributionServiceImpl contributionService;

    private Member testMember;
    private Contribution testContribution;
    private ContributionRequest testRequest;
    private ContributionResponse testResponse;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .memberId("MEM123")
                .firstName("John")
                .lastName("Doe")
                .active(true)
                .build();

        testContribution = Contribution.builder()
                .id(1L)
                .referenceNumber("CON123")
                .member(testMember)
                .contributionType(ContributionType.MONTHLY)
                .contributionAmount(BigDecimal.valueOf(500))
                .contributionDate(LocalDate.now())
                .build();

        testRequest = ContributionRequest.builder()
                .memberId(1L)
                .contributionType(ContributionType.MONTHLY)
                .contributionAmount(BigDecimal.valueOf(500))
                .contributionDate(LocalDate.now())
                .build();

        testResponse = ContributionResponse.builder()
                .id(1L)
                .referenceNumber("CON123")
                .contributionAmount(BigDecimal.valueOf(500))
                .build();
    }

    @Test
    void processContribution_WithValidData_ShouldSucceed() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(contributionRepository.findMonthlyContributionByMemberAndYearMonth(
                any(), any(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(contributionMapper.toEntity(any())).thenReturn(testContribution);
        when(contributionRepository.save(any())).thenReturn(testContribution);
        when(contributionMapper.toResponse(any())).thenReturn(testResponse);

        // Act
        ContributionResponse result = contributionService.processContribution(testRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReferenceNumber()).isEqualTo("CON123");
        verify(contributionRepository, times(2)).save(any());
    }

    @Test
    void processContribution_WithInvalidAmount_ShouldThrowException() {
        // Arrange
        testRequest.setContributionAmount(BigDecimal.valueOf(50));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // Act & Assert
        assertThatThrownBy(() -> contributionService.processContribution(testRequest))
                .isInstanceOf(InvalidContributionException.class)
                .hasMessageContaining("Contribution amount must be greater than zero");
    }

    @Test
    void processContribution_WithNonActiveMember_ShouldThrowException() {
        // Arrange
        testMember.setActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // Act & Assert
        assertThatThrownBy(() -> contributionService.processContribution(testRequest))
                .isInstanceOf(InvalidContributionException.class)
                .hasMessageContaining("Cannot process contribution for non-active member");
    }

    @Test
    void getMemberContributions_ShouldReturnList() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(contributionRepository.findByMemberId(any())).thenReturn(Arrays.asList(testContribution));
        when(contributionMapper.toResponse(any())).thenReturn(testResponse);

        // Act
        List<ContributionResponse> results = contributionService.getMemberContributions(1L);

        // Assert
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        verify(contributionRepository).findByMemberId(any());
    }

    @Test
    void calculateTotalContributions_ShouldReturnTotal() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(contributionRepository.getTotalContributionsByMember(any()))
                .thenReturn(BigDecimal.valueOf(5000));

        // Act
        BigDecimal total = contributionService.calculateTotalContributions(1L);

        // Assert
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }
}
