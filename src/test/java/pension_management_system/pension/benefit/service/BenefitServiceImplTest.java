package pension_management_system.pension.benefit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.Benefit;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.entity.BenefitType;
import pension_management_system.pension.benefit.mapper.BenefitMapper;
import pension_management_system.pension.benefit.repository.BenefitRepository;
import pension_management_system.pension.benefit.service.impl.BenefitServiceImpl;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BenefitServiceImplTest - Unit tests for Benefit service
 *
 * Tests:
 * - Benefit claim creation
 * - Benefit approval/rejection
 * - Benefit retrieval
 * - Status tracking
 * - Member eligibility
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Benefit Service Unit Tests")
class BenefitServiceImplTest {

    @Mock
    private BenefitRepository benefitRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BenefitMapper benefitMapper;

    @InjectMocks
    private BenefitServiceImpl benefitService;

    private Member member;
    private Benefit benefit;
    private BenefitRequest benefitRequest;
    private BenefitResponse benefitResponse;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setFirstName("John");
        member.setEmail("john@example.com");

        benefit = new Benefit();
        benefit.setId(1L);
        benefit.setMember(member);
        benefit.setBenefitType(BenefitType.RETIREMENT);
        benefit.setBenefitAmount(BigDecimal.valueOf(500000));
        benefit.setStatus(BenefitStatus.PENDING);

        benefitRequest = new BenefitRequest();
        benefitRequest.setMemberId(1L);
        benefitRequest.setBenefitType(BenefitType.RETIREMENT);
        benefitRequest.setBenefitAmount(BigDecimal.valueOf(500000));

        benefitResponse = new BenefitResponse();
        benefitResponse.setId(1L);
        benefitResponse.setMemberId(1L);
        benefitResponse.setBenefitType(BenefitType.RETIREMENT);
        benefitResponse.setBenefitAmount(BigDecimal.valueOf(500000));
        benefitResponse.setStatus(BenefitStatus.PENDING);
    }

    @Test
    @DisplayName("Should create benefit claim")
    void createBenefit_Success() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(benefitMapper.toEntity(any())).thenReturn(benefit);
        when(benefitRepository.save(any())).thenReturn(benefit);
        when(benefitMapper.toResponse(any())).thenReturn(benefitResponse);

        // Act
        BenefitResponse result = benefitService.createBenefit(benefitRequest);

        // Assert
        assertNotNull(result);
        assertEquals(BenefitType.RETIREMENT, result.getBenefitType());
        assertEquals(BigDecimal.valueOf(500000), result.getBenefitAmount());

        verify(memberRepository).findById(1L);
        verify(benefitRepository).save(any());
    }

    @Test
    @DisplayName("Should get benefit by ID")
    void getBenefitById_Success() {
        // Arrange
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        when(benefitMapper.toResponse(any())).thenReturn(benefitResponse);

        // Act
        BenefitResponse result = benefitService.getBenefitById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(benefitRepository).findById(1L);
    }

    @Test
    @DisplayName("Should approve benefit claim")
    void approveBenefit_Success() {
        // Arrange
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        when(benefitRepository.save(any())).thenReturn(benefit);
        when(benefitMapper.toResponse(any())).thenReturn(benefitResponse);

        // Act
        BenefitResponse result = benefitService.approveBenefit(1L, "Approved by admin");

        // Assert
        assertNotNull(result);
        verify(benefitRepository).findById(1L);
        verify(benefitRepository).save(any());
    }

    @Test
    @DisplayName("Should reject benefit claim")
    void rejectBenefit_Success() {
        // Arrange
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        when(benefitRepository.save(any())).thenReturn(benefit);
        when(benefitMapper.toResponse(any())).thenReturn(benefitResponse);

        // Act
        BenefitResponse result = benefitService.rejectBenefit(1L, "Insufficient balance");

        // Assert
        assertNotNull(result);
        verify(benefitRepository).findById(1L);
        verify(benefitRepository).save(any());
    }

    @Test
    @DisplayName("Should get benefits by member")
    void getBenefitsByMember_Success() {
        // Arrange
        List<Benefit> benefits = List.of(benefit);
        when(benefitRepository.findByMemberId(1L)).thenReturn(benefits);
        when(benefitMapper.toResponse(any())).thenReturn(benefitResponse);

        // Act
        List<BenefitResponse> results = benefitService.getBenefitsByMember(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(benefitRepository).findByMemberId(1L);
    }

    @Test
    @DisplayName("Should get benefits by status")
    void getBenefitsByStatus_Success() {
        // Arrange
        List<Benefit> benefits = List.of(benefit);
        when(benefitRepository.findByStatus(BenefitStatus.PENDING)).thenReturn(benefits);
        when(benefitMapper.toResponse(any())).thenReturn(benefitResponse);

        // Act
        List<BenefitResponse> results = benefitService.getBenefitsByStatus(BenefitStatus.PENDING);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(benefitRepository).findByStatus(BenefitStatus.PENDING);
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void createBenefit_MemberNotFound() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                benefitService.createBenefit(benefitRequest));

        verify(benefitRepository, never()).save(any());
    }
}
