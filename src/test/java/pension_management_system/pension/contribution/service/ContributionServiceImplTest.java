package pension_management_system.pension.contribution.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.mapper.ContributionMapper;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.contribution.service.impl.ContributionServiceImpl;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ContributionServiceImplTest - Unit tests for Contribution service
 *
 * Tests:
 * - Contribution creation
 * - Contribution retrieval
 * - Contribution status updates
 * - Member balance calculations
 * - Event publishing (email notifications)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Contribution Service Unit Tests")
class ContributionServiceImplTest {

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ContributionMapper contributionMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ContributionServiceImpl contributionService;

    private Member member;
    private Contribution contribution;
    private ContributionRequest contributionRequest;
    private ContributionResponse contributionResponse;

    @BeforeEach
    void setUp() {
        // Create test member
        member = new Member();
        member.setId(1L);
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john@example.com");

        // Create test contribution
        contribution = new Contribution();
        contribution.setId(1L);
        contribution.setMember(member);
        contribution.setContributionAmount(BigDecimal.valueOf(50000));
        contribution.setContributionDate(LocalDateTime.now());
        contribution.setStatus(ContributionStatus.PENDING);

        // Create contribution request
        contributionRequest = new ContributionRequest();
        contributionRequest.setMemberId(1L);
        contributionRequest.setContributionAmount(BigDecimal.valueOf(50000));

        // Create contribution response
        contributionResponse = new ContributionResponse();
        contributionResponse.setId(1L);
        contributionResponse.setMemberId(1L);
        contributionResponse.setContributionAmount(BigDecimal.valueOf(50000));
        contributionResponse.setStatus(ContributionStatus.PENDING);
    }

    @Test
    @DisplayName("Should create contribution and publish event")
    void createContribution_Success() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(contributionMapper.toEntity(any())).thenReturn(contribution);
        when(contributionRepository.save(any())).thenReturn(contribution);
        when(contributionMapper.toResponse(any())).thenReturn(contributionResponse);

        // Act
        ContributionResponse result = contributionService.createContribution(contributionRequest);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50000), result.getContributionAmount());
        assertEquals(1L, result.getMemberId());

        verify(memberRepository).findById(1L);
        verify(contributionRepository).save(any());
        verify(eventPublisher).publishEvent(any()); // Contribution email event
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void createContribution_MemberNotFound() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                contributionService.createContribution(contributionRequest));

        verify(contributionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get contribution by ID")
    void getContributionById_Success() {
        // Arrange
        when(contributionRepository.findById(1L)).thenReturn(Optional.of(contribution));
        when(contributionMapper.toResponse(any())).thenReturn(contributionResponse);

        // Act
        ContributionResponse result = contributionService.getContributionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(contributionRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get contributions by member")
    void getContributionsByMember_Success() {
        // Arrange
        List<Contribution> contributions = List.of(contribution);
        when(contributionRepository.findByMemberId(1L)).thenReturn(contributions);
        when(contributionMapper.toResponse(any())).thenReturn(contributionResponse);

        // Act
        List<ContributionResponse> results = contributionService.getContributionsByMember(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(contributionRepository).findByMemberId(1L);
    }

    @Test
    @DisplayName("Should calculate total contributions for member")
    void getTotalContributions_Success() {
        // Arrange
        when(contributionRepository.calculateTotalByMemberId(1L))
                .thenReturn(BigDecimal.valueOf(150000));

        // Act
        BigDecimal total = contributionService.getTotalContributions(1L);

        // Assert
        assertEquals(BigDecimal.valueOf(150000), total);
        verify(contributionRepository).calculateTotalByMemberId(1L);
    }

    @Test
    @DisplayName("Should update contribution status")
    void updateContributionStatus_Success() {
        // Arrange
        when(contributionRepository.findById(1L)).thenReturn(Optional.of(contribution));
        when(contributionRepository.save(any())).thenReturn(contribution);
        when(contributionMapper.toResponse(any())).thenReturn(contributionResponse);

        // Act
        ContributionResponse result = contributionService.updateStatus(1L, ContributionStatus.COMPLETED);

        // Assert
        assertNotNull(result);
        verify(contributionRepository).findById(1L);
        verify(contributionRepository).save(any());
    }
}
