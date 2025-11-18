package pension_management_system.pension.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pension_management_system.pension.common.exception.MemberNotFoundException;
import pension_management_system.pension.member.dto.MemberRequest;
import pension_management_system.pension.member.dto.MemberResponse;
import pension_management_system.pension.member.entity.MemberStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * MemberService Interface
 *
 * Purpose: Defines the contract (methods) for member management operations
 * This is an interface - it only declares methods, implementation is in MemberServiceImpl
 *
 * Why use interface?
 * - Loose coupling: Controller depends on interface, not concrete implementation
 * - Easy to mock for testing
 * - Can have multiple implementations if needed
 */

public interface MemberService {
    /**
     * Register a new member in the pension system
     *
     * Process:
     * 1. Validate request data (age, email format, etc.)
     * 2. Check if email/phone already exists
     * 3. Generate unique member ID
     * 4. Save to database
     * 5. Return response with created member details
     *
     * @param request Member registration data
     * @return MemberResponse with created member details
     * @throws IllegalArgumentException if validation fails
     * @throws DuplicateEmailException if email already exists
     */
    MemberResponse registerMember(MemberRequest request);

    /**
     * Update existing member's information
     *
     * @param id Member's database ID
     * @param request Updated member data
     * @return MemberResponse with updated details
     * @throws MemberNotFoundException if member doesn't exist
     */
    MemberResponse updateMember(Long id, MemberRequest request);

    /**
     * Get existing member information by Id
     * @param id Member's database ID
     * @return MemberResponse with updated details
     * @throws MemberNotFoundException if member doesnt exist
     */
    MemberResponse getMemberById(Long id);

    /**
     * Get existing member information by Id
     * @string memberId Member's database MemberId
     * @return MemberResponse with updated details
     * @throws MemberNotFoundException if member doesnt exist
     */
    MemberResponse getMemberByMemberId(String memberId);


    /**
     * Get all active members
     *
     * @return List of all active members
     */
    List<MemberResponse> getAllActiveMembers();

    /**
     * Get all active members with pagination
     *
     * @param pageable Pagination parameters
     * @return Page of all active members
     */
    Page<MemberResponse> getAllActiveMembersWithPagination(Pageable pageable);



    /**
     * Soft delete a member
     * Does not physically delete from database
     * Just marks as deleted and inactive
     *
     * @param id Member's database ID
     * @throws MemberNotFoundException if member doesn't exist
     */
    void sofDeleteMember(Long id);

    /**
     * Deactivate a member (temporary)
     * Member can be reactivated later
     *
     * @param id Member's database ID
     * @throws MemberNotFoundException if member doesn't exist
     */
    void deactivateMember(Long id);

    /**
     * Reactivate a previously deactivated member
     *
     * @param id Member's database ID
     * @throws MemberNotFoundException if member doesn't exist
     */
    void reactivateMember(Long id);

    /**
     * Search and filter members with pagination
     *
     * @param memberId Filter by member ID
     * @param firstName Filter by first name
     * @param lastName Filter by last name
     * @param email Filter by email
     * @param phoneNumber Filter by phone number
     * @param status Filter by member status
     * @param active Filter by active status
     * @param employerId Filter by employer ID
     * @param dateOfBirthFrom Filter by date of birth from
     * @param dateOfBirthTo Filter by date of birth to
     * @param city Filter by city
     * @param state Filter by state
     * @param country Filter by country
     * @param pageable Pagination parameters
     * @return Page of members matching the criteria
     */
    Page<MemberResponse> searchMembers(
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
    );

    /**
     * Quick search members by keyword
     *
     * @param searchTerm Search term to match against multiple fields
     * @param pageable Pagination parameters
     * @return Page of members matching the search term
     */
    Page<MemberResponse> quickSearch(String searchTerm, Pageable pageable);

}
