package pension_management_system.pension.benefit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.benefit.dto.BenefitCalculationResponse;
import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.Benefit;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.entity.BenefitType;
import pension_management_system.pension.benefit.mapper.BenefitMapper;
import pension_management_system.pension.benefit.repository.BenefitRepository;
import pension_management_system.pension.benefit.service.BenefitService;
import pension_management_system.pension.common.exception.BenefitNotFoundException;
import pension_management_system.pension.common.exception.InvalidBenefitException;
import pension_management_system.pension.common.exception.MemberNotFoundException;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BenefitServiceImpl implements BenefitService {

    private final BenefitRepository benefitRepository;
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final BenefitMapper benefitMapper;

    // Configuration constants - in production, these would come from a config service
    private static final BigDecimal EMPLOYER_CONTRIBUTION_RATE = new BigDecimal("0.10"); // 10%
    private static final BigDecimal INVESTMENT_RETURN_RATE = new BigDecimal("0.08"); // 8% annual
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10%
    private static final BigDecimal ADMIN_FEE_RATE = new BigDecimal("0.02"); // 2%
    private static final int MIN_RETIREMENT_AGE = 60;
    private static final int MIN_SERVICE_YEARS = 5;

    @Override
    @Transactional(readOnly = true)
    public BenefitCalculationResponse calculateBenefit(Long memberId, BenefitType benefitType) {
        log.info("Calculating {} benefit for member: {}", benefitType, memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        // Calculate service period
        Period servicePeriod = Period.between(member.getCreatedAt().toLocalDate(), LocalDate.now());
        int yearsOfService = servicePeriod.getYears();
        int monthsOfService = servicePeriod.getMonths();
        int totalMonths = (yearsOfService * 12) + monthsOfService;

        // Get contributions
        BigDecimal totalMonthly = contributionRepository.getTotalByMemberAndType(member, ContributionType.MONTHLY);
        BigDecimal totalVoluntary = contributionRepository.getTotalByMemberAndType(member, ContributionType.VOLUNTARY);
        totalMonthly = totalMonthly != null ? totalMonthly : BigDecimal.ZERO;
        totalVoluntary = totalVoluntary != null ? totalVoluntary : BigDecimal.ZERO;

        BigDecimal totalContributions = totalMonthly.add(totalVoluntary);

        // Calculate employer contributions (10% of member contributions)
        BigDecimal employerContributions = totalContributions.multiply(EMPLOYER_CONTRIBUTION_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate investment returns (simplified - 8% annual return)
        // For more accuracy, this should use compound interest formula
        BigDecimal years = new BigDecimal(totalMonths).divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
        BigDecimal investmentReturns = totalContributions.add(employerContributions)
                .multiply(INVESTMENT_RETURN_RATE)
                .multiply(years)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate gross benefit
        BigDecimal grossBenefit = totalContributions
                .add(employerContributions)
                .add(investmentReturns);

        // Calculate deductions
        BigDecimal taxDeduction = grossBenefit.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal adminFees = grossBenefit.multiply(ADMIN_FEE_RATE).setScale(2, RoundingMode.HALF_UP);

        // Calculate net benefit
        BigDecimal netBenefit = grossBenefit.subtract(taxDeduction).subtract(adminFees);

        // Check eligibility
        String eligibilityStatus;
        String eligibilityMessage;

        switch (benefitType) {
            case RETIREMENT:
                int age = Period.between(member.getDateOfBirth(), LocalDate.now()).getYears();
                if (age >= MIN_RETIREMENT_AGE && yearsOfService >= MIN_SERVICE_YEARS) {
                    eligibilityStatus = "ELIGIBLE";
                    eligibilityMessage = "Member is eligible for retirement benefit";
                } else if (age < MIN_RETIREMENT_AGE) {
                    eligibilityStatus = "NOT_ELIGIBLE";
                    eligibilityMessage = String.format("Member must be at least %d years old (currently %d years)",
                            MIN_RETIREMENT_AGE, age);
                } else {
                    eligibilityStatus = "NOT_ELIGIBLE";
                    eligibilityMessage = String.format("Member must have at least %d years of service (currently %d years)",
                            MIN_SERVICE_YEARS, yearsOfService);
                }
                break;

            case VOLUNTARY_WITHDRAWAL:
                if (yearsOfService >= 5) {
                    eligibilityStatus = "ELIGIBLE";
                    eligibilityMessage = "Member is eligible for voluntary withdrawal";
                } else {
                    eligibilityStatus = "NOT_ELIGIBLE";
                    eligibilityMessage = String.format("Member must have at least 5 years of service (currently %d years)",
                            yearsOfService);
                }
                break;

            case DEATH_BENEFIT:
            case DISABILITY:
                eligibilityStatus = "REQUIRES_DOCUMENTATION";
                eligibilityMessage = "Benefit requires supporting documentation for processing";
                break;

            case PARTIAL_WITHDRAWAL:
                if (yearsOfService >= 2 && member.getMemberStatus() == MemberStatus.ACTIVE) {
                    eligibilityStatus = "ELIGIBLE";
                    eligibilityMessage = "Member is eligible for partial withdrawal (max 25% of contributions)";
                    // For partial withdrawal, limit to 25%
                    netBenefit = netBenefit.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP);
                } else {
                    eligibilityStatus = "NOT_ELIGIBLE";
                    eligibilityMessage = "Member must be active with at least 2 years of service";
                }
                break;

            default:
                eligibilityStatus = "UNKNOWN";
                eligibilityMessage = "Unknown benefit type";
        }

        return BenefitCalculationResponse.builder()
                .memberId(memberId)
                .memberName(member.getFirstName() + " " + member.getLastName())
                .yearsOfService(yearsOfService)
                .monthsOfService(monthsOfService)
                .totalMonthlyContributions(totalMonthly)
                .totalVoluntaryContributions(totalVoluntary)
                .totalContributions(totalContributions)
                .estimatedEmployerContributions(employerContributions)
                .estimatedInvestmentReturns(investmentReturns)
                .grossBenefit(grossBenefit)
                .estimatedTax(taxDeduction)
                .estimatedAdminFees(adminFees)
                .estimatedNetBenefit(netBenefit)
                .eligibilityStatus(eligibilityStatus)
                .eligibilityMessage(eligibilityMessage)
                .build();
    }

    @Override
    @Transactional
    public BenefitResponse applyForBenefit(BenefitRequest request) {
        log.info("Processing benefit application for member: {}", request.getMemberId());

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + request.getMemberId()));

        // Check if member already has a pending or approved benefit
        if (benefitRepository.existsByMemberAndStatus(member, BenefitStatus.PENDING) ||
            benefitRepository.existsByMemberAndStatus(member, BenefitStatus.APPROVED)) {
            throw new InvalidBenefitException("Member already has a pending or approved benefit application");
        }

        // Calculate benefit
        BenefitCalculationResponse calculation = calculateBenefit(request.getMemberId(), request.getBenefitType());

        // Check eligibility
        if ("NOT_ELIGIBLE".equals(calculation.getEligibilityStatus())) {
            throw new InvalidBenefitException("Member is not eligible: " + calculation.getEligibilityMessage());
        }

        // Create benefit entity
        Benefit benefit = benefitMapper.toEntity(request);
        benefit.setMember(member);
        benefit.setTotalContributions(calculation.getTotalContributions());
        benefit.setEmployerContributions(calculation.getEstimatedEmployerContributions());
        benefit.setInvestmentReturns(calculation.getEstimatedInvestmentReturns());
        benefit.setCalculatedBenefit(calculation.getGrossBenefit());
        benefit.setTaxDeductions(calculation.getEstimatedTax());
        benefit.setAdministrativeFees(calculation.getEstimatedAdminFees());
        benefit.setNetPayable(calculation.getEstimatedNetBenefit());
        benefit.setStatus(BenefitStatus.PENDING);

        Benefit savedBenefit = benefitRepository.save(benefit);
        log.info("Benefit application created with reference: {}", savedBenefit.getReferenceNumber());

        return benefitMapper.toResponse(savedBenefit);
    }

    @Override
    @Transactional(readOnly = true)
    public BenefitResponse getBenefitById(Long id) {
        log.info("Fetching benefit by id: {}", id);
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new BenefitNotFoundException("Benefit not found with id: " + id));
        return benefitMapper.toResponse(benefit);
    }

    @Override
    @Transactional(readOnly = true)
    public BenefitResponse getBenefitByReference(String referenceNumber) {
        log.info("Fetching benefit by reference: {}", referenceNumber);
        Benefit benefit = benefitRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new BenefitNotFoundException("Benefit not found with reference: " + referenceNumber));
        return benefitMapper.toResponse(benefit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenefitResponse> getMemberBenefits(Long memberId) {
        log.info("Fetching all benefits for member: {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        List<Benefit> benefits = benefitRepository.findByMember(member);
        return benefits.stream()
                .map(benefitMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenefitResponse> getBenefitsByStatus(BenefitStatus status) {
        log.info("Fetching benefits by status: {}", status);
        List<Benefit> benefits = benefitRepository.findByStatus(status);
        return benefits.stream()
                .map(benefitMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BenefitResponse approveBenefit(Long id, String approvedBy) {
        log.info("Approving benefit: {} by {}", id, approvedBy);
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new BenefitNotFoundException("Benefit not found with id: " + id));

        if (benefit.getStatus() != BenefitStatus.PENDING && benefit.getStatus() != BenefitStatus.UNDER_REVIEW) {
            throw new InvalidBenefitException("Only pending or under-review benefits can be approved");
        }

        benefit.approve(approvedBy);
        Benefit savedBenefit = benefitRepository.save(benefit);
        log.info("Benefit approved: {}", savedBenefit.getReferenceNumber());

        return benefitMapper.toResponse(savedBenefit);
    }

    @Override
    @Transactional
    public BenefitResponse rejectBenefit(Long id, String reason) {
        log.info("Rejecting benefit: {} with reason: {}", id, reason);
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new BenefitNotFoundException("Benefit not found with id: " + id));

        if (benefit.getStatus() == BenefitStatus.DISBURSED) {
            throw new InvalidBenefitException("Cannot reject a disbursed benefit");
        }

        benefit.reject(reason);
        Benefit savedBenefit = benefitRepository.save(benefit);
        log.info("Benefit rejected: {}", savedBenefit.getReferenceNumber());

        return benefitMapper.toResponse(savedBenefit);
    }

    @Override
    @Transactional
    public BenefitResponse disburseBenefit(Long id, String disbursedBy) {
        log.info("Disbursing benefit: {} by {}", id, disbursedBy);
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new BenefitNotFoundException("Benefit not found with id: " + id));

        if (benefit.getStatus() != BenefitStatus.APPROVED) {
            throw new InvalidBenefitException("Only approved benefits can be disbursed");
        }

        benefit.disburse(disbursedBy);

        // Update member status if retirement benefit
        if (benefit.getBenefitType() == BenefitType.RETIREMENT) {
            Member member = benefit.getMember();
            member.setMemberStatus(MemberStatus.RETIRED);
            memberRepository.save(member);
            log.info("Member status updated to RETIRED for member: {}", member.getMemberId());
        }

        Benefit savedBenefit = benefitRepository.save(benefit);
        log.info("Benefit disbursed: {}", savedBenefit.getReferenceNumber());

        return benefitMapper.toResponse(savedBenefit);
    }

    @Override
    @Transactional
    public void cancelBenefit(Long id) {
        log.info("Cancelling benefit: {}", id);
        Benefit benefit = benefitRepository.findById(id)
                .orElseThrow(() -> new BenefitNotFoundException("Benefit not found with id: " + id));

        if (benefit.getStatus() == BenefitStatus.DISBURSED) {
            throw new InvalidBenefitException("Cannot cancel a disbursed benefit");
        }

        benefit.cancel();
        benefitRepository.save(benefit);
        log.info("Benefit cancelled: {}", benefit.getReferenceNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingBenefits() {
        return benefitRepository.findByStatus(BenefitStatus.PENDING).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countMemberPendingBenefits(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));
        return benefitRepository.countByMemberAndStatus(member, BenefitStatus.PENDING);
    }
}
