package pension_management_system.pension.member.service;

import pension_management_system.pension.common.exception.MemberNotFoundException;
import pension_management_system.pension.member.dto.MemberRequest;
import pension_management_system.pension.member.dto.MemberResponse;

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

}
