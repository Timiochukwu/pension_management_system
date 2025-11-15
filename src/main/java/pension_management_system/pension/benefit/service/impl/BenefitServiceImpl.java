package pension_management_system.pension.benefit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.Benefit;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.mapper.BenefitMapper;
import pension_management_system.pension.benefit.repository.BenefitRepository;
import pension_management_system.pension.benefit.service.BenefitService;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BenefitServiceImpl - Business logic for benefit operations
 *
 * Responsibilities:
 * - Validate benefit requests
 * - Create and update benefit claims
 * - Manage approval workflow
 * - Track payments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BenefitServiceImpl implements BenefitService {

    private final BenefitRepository benefitRepository;
    private final MemberRepository memberRepository;
    private final BenefitMapper benefitMapper;

    @Override
    @Transactional
    public BenefitResponse createBenefit(BenefitRequest request) {
        log.info("Creating benefit claim for member ID: {}", request.getMemberId());

        // Find member
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Convert DTO to entity
        Benefit benefit = benefitMapper.toEntity(request);
        benefit.setMember(member);
        benefit.setStatus(BenefitStatus.PENDING);

        // Save to database
        Benefit saved = benefitRepository.save(benefit);
        log.info("Benefit created with ID: {}", saved.getId());

        return benefitMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BenefitResponse getBenefitById(Long id) {
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found"));
        return benefitMapper.toResponse(benefit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenefitResponse> getAllBenefits() {
        return benefitRepository.findAll().stream()
                .map(benefitMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenefitResponse> getBenefitsByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        return benefitRepository.findByMember(member).stream()
                .map(benefitMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenefitResponse> getBenefitsByStatus(BenefitStatus status) {
        return benefitRepository.findByStatus(status).stream()
                .map(benefitMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BenefitResponse updateBenefit(Long id, BenefitRequest request) {
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found"));

        if (benefit.getStatus() != BenefitStatus.PENDING) {
            throw new IllegalStateException("Can only update PENDING benefits");
        }

        benefitMapper.updateEntityFromRequest(request, benefit);
        Benefit updated = benefitRepository.save(benefit);

        return benefitMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public BenefitResponse approveBenefit(Long id, BigDecimal approvedAmount, String approvedBy) {
        log.info("Approving benefit ID: {} for amount: {}", id, approvedAmount);

        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found"));

        benefit.approve(approvedAmount, approvedBy);
        Benefit saved = benefitRepository.save(benefit);

        return benefitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BenefitResponse rejectBenefit(Long id, String reason, String rejectedBy) {
        log.info("Rejecting benefit ID: {}", id);

        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found"));

        benefit.reject(reason, rejectedBy);
        Benefit saved = benefitRepository.save(benefit);

        return benefitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BenefitResponse markAsPaid(Long id) {
        log.info("Marking benefit ID: {} as paid", id);

        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found"));

        benefit.markAsPaid();
        Benefit saved = benefitRepository.save(benefit);

        return benefitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BenefitResponse startReview(Long id) {
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found"));

        benefit.startReview();
        Benefit saved = benefitRepository.save(benefit);

        return benefitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BenefitResponse cancelBenefit(Long id) {
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found"));

        benefit.cancel();
        Benefit saved = benefitRepository.save(benefit);

        return benefitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBenefit(Long id) {
        if (!benefitRepository.existsById(id)) {
            throw new IllegalArgumentException("Benefit not found");
        }
        benefitRepository.deleteById(id);
    }
}
