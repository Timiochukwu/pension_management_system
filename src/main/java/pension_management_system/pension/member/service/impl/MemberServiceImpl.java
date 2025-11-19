package pension_management_system.pension.member.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.common.exception.MemberNotFoundException;
import pension_management_system.pension.employer.entity.Employer;
import pension_management_system.pension.employer.repository.EmployerRepository;
import pension_management_system.pension.member.dto.MemberRequest;
import pension_management_system.pension.member.dto.MemberResponse;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.mapper.MemberMapper;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.member.service.MemberService;
import pension_management_system.pension.member.specification.MemberSpecification;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MemberServiceImpl - Implementation of MemberService interface
 *
 * Annotations explained:
 * @Service - Marks this as a Spring service component (business logic layer)
 * @RequiredArgsConstructor - Lombok: Creates constructor with required fields (final fields)
 * @Slf4j - Lombok: Provides logging capability (log.info(), log.error(), etc.)
 * @Transactional - Makes methods run in database transactions (rollback on error)
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    // DEPENDENCIES (injected by Spring through constructor)
    private final MemberRepository memberRepository; // Database repository
    private final EmployerRepository employerRepository;
    private final MemberMapper memberMapper; // Convert between Entity and DTO

    /**
     * Register new member
     *
     * @Transactional: If any error occurs, all database changes are rolled back
     */
    @Override
    public MemberResponse registerMember(MemberRequest request) {
        // STEP 1: Log the operation (good for debugging and auditing)
        log.info("Starting member registration for email: {}", request.getEmail() );

        // STEP 2: Validate age (must be 18-70 years old)
        validateAge(request.getDateOfBirth());

        // STEP 3: Check if email already exists (business rule: email must be unique)
        if (memberRepository.existsByEmail(request.getEmail())) {
            log.error("Registration failed - Email already exists: {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists: "+ request.getEmail());
        }
        if (memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.error("Registration failed - Phone number already exists: {}", request.getPhoneNumber());
            throw new IllegalArgumentException("Phone number already exists: "+ request.getPhoneNumber());
        }
        // STEP 4: Convert DTO (MemberRequest) to Entity (Member)
        // This is done by MapStruct - it copies fields from request to entity
        Member member = memberMapper.toEntity(request);
        // STEP 5: Set default values
        member.setMemberStatus(MemberStatus.ACTIVE);
        member.setActive(true);
        // STEP 5: Handle employer safely
        if (request.getEmployerId() != null) {
            Employer employer = employerRepository.findById(request.getEmployerId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Employer not found with ID: " + request.getEmployerId()
                    ));
            member.setEmployer(employer);
        } else {
            member.setEmployer(null); // No employer assigned
        }
        // STEP 6: Save to database
        // save() returns the saved entity with generated ID
        Member savedMember = memberRepository.save(member);
        // STEP 7: Log success
        log.info("Member registered successfully with ID: {} and MemberID: {}",
                savedMember.getId(), savedMember.getMemberId());
        // STEP 8: Convert Entity back to DTO and return
        return memberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional
    @CachePut(value = "members", key = "#id")
    public MemberResponse updateMember(Long id, MemberRequest request) {
        log.info("Updating member with ID: {}", id);

        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID {}" + id));

        if (!existingMember.getDateOfBirth().equals(request.getDateOfBirth())) {
            validateAge(request.getDateOfBirth());
        }
        if (!existingMember.getEmail().equals(request.getEmail())) {
            if(memberRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
        }
        memberMapper.updateEntityFromRequest(request,  existingMember);

        // Handle employer manually
        if (request.getEmployerId() != null) {
            Employer employer = employerRepository.findById(request.getEmployerId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Employer not found with ID: " + request.getEmployerId()
                    ));
            existingMember.setEmployer(employer);
        } else {
            existingMember.setEmployer(null); // optional
        }
        Member  updatedMember = memberRepository.save(existingMember);

        log.info("Member updated successfully with ID: {}", id);
        return memberMapper.toResponse(updatedMember);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "members", key="#id")
    public MemberResponse getMemberById(Long id) {
        log.info("Fetching member with ID: {}", id);
        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID: " + id));
        return memberMapper.toResponse(existingMember);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "members", key = "'memberId:' + #memberId")
    public MemberResponse getMemberByMemberId(String memberId) {
        log.info("Fetching member with memberID: {}", memberId);
        Member existingMember = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID: " + memberId));
        return memberMapper.toResponse(existingMember);
    }

    @Override
    @Transactional
    @Cacheable(value = "activeMembers")
    public List<MemberResponse> getAllActiveMembers() {
        log.info("Fetching all members for active");
        return memberRepository.findAll().stream()
                .filter(Member::getActive)
                .map(memberMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponse> getAllActiveMembersWithPagination(Pageable pageable) {
        log.info("Fetching all active members with pagination - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<Member> members = memberRepository.findByActive(true, pageable);
        return members.map(memberMapper::toResponse);
    }

    @Override
    @Transactional
    public void sofDeleteMember(Long id) {
        log.info("Deleting member with ID: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID: " + id));
        member.softDelete();
        memberRepository.save(member);

        log.info("Member deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void deactivateMember(Long id) {
        log.info("Deactivating member with ID: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID: " + id));
        member.deactivate();
        memberRepository.save(member);
        log.info("Member deactivated successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void reactivateMember(Long id) {
        log.info("Reactivating member with ID: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID: " + id));
        if(member.getMemberStatus().equals(MemberStatus.TERMINATED)) {
            throw new IllegalStateException("Member is already terminated, Cannot reactivate terminated member");
        }
        member.setMemberStatus(MemberStatus.ACTIVE);
        member.setActive(true);
        memberRepository.save(member);

        log.info("Member reactivated successfully with ID: {}", id);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponse> searchMembers(
            String memberId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            MemberStatus status,
            Boolean active,
            Long employerId,
            LocalDate dateOfBirthFrom,
            LocalDate dateOfBirthTo,
            String city,
            String state,
            String country,
            Pageable pageable
    ) {
        log.info("Searching members with filters");

        Specification<Member> spec = MemberSpecification.filterMembers(
                memberId, firstName, lastName, email, phoneNumber, status, active,
                employerId, dateOfBirthFrom, dateOfBirthTo, city, state, country
        );

        Page<Member> members = memberRepository.findAll(spec, pageable);
        return members.map(memberMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponse> quickSearch(String searchTerm, Pageable pageable) {
        log.info("Quick search for members with term: {}", searchTerm);

        Specification<Member> spec = MemberSpecification.searchMembers(searchTerm);
        Page<Member> members = memberRepository.findAll(spec, pageable);
        return members.map(memberMapper::toResponse);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate member's age
     * Business Rule: Must be between 18 and 70 years old
     *
     * @param dateOfBirth Member's date of birth
     * @throws IllegalArgumentException if age is invalid
     */

    private void validateAge(LocalDate dateOfBirth) {
        // STEP 1: Check if date of birth is null
        if (dateOfBirth == null) {
            log.error("Date of birth is null");
            throw new IllegalArgumentException("Date of birth is required");
        }

        log.debug("Validating date of birth: {}", dateOfBirth);

        // STEP 2: Check if date is in the future
        if (dateOfBirth.isAfter(LocalDate.now())) {
            log.error("Date of birth is in the future: {}", dateOfBirth);
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        // STEP 3: Calculate age using Period class (more accurate than simple year subtraction)
        // Period accounts for months and days, not just years
        LocalDate today = LocalDate.now();
        Period period = Period.between(dateOfBirth, today);
        int age = period.getYears();

        log.debug("Age calculation - Date of Birth: {}, Today: {}, Period: {} years {} months {} days, Age: {}",
                  dateOfBirth, today, period.getYears(), period.getMonths(), period.getDays(), age);

        // STEP 4: Check age range
        if (age < 18) {
            log.error("Age validation failed - Member is too young. Date of Birth: {}, Age: {}", dateOfBirth, age);
            throw new IllegalArgumentException(
                String.format("Member must be at least 18 years old. Date of Birth: %s, Current Age: %d years",
                              dateOfBirth, age));
        }
        if (age > 70) {
            log.error("Age validation failed - Member is too old. Date of Birth: {}, Age: {}", dateOfBirth, age);
            throw new IllegalArgumentException(
                String.format("Member must be at most 70 years old. Date of Birth: %s, Current Age: %d years",
                              dateOfBirth, age));
        }
        log.debug("Age validation successful. Member age: {}", age);
    }
}












