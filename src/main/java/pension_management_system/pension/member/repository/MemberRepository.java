package pension_management_system.pension.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;

import java.util.List;
import java.util.Optional;


/**
 * MemberRepository - Database operations for Member entity
 *
 * Spring Data JPA automatically implements these methods
 * No need to write SQL - Spring generates it for you!
 */

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Find member by business member ID
    Optional<Member> findByMemberId(String memberId);
    // Find member by email
    Optional<Member> findByEmail(String email);
    // Check if email exists
    boolean existsByEmail(String email);
    // Check if phone number exists
    boolean existsByPhoneNumber(String phoneNumber);

    // Find all members by status
    List<Member> findByMemberStatus(MemberStatus memberStatus);
    //Find all active members
    List<Member> findByActiveTrue();
    //Find members by employer
//    List<Member> findByEmployer_id(Long employerId);

    // Custom query: Find members eligible for retirement (age >= 60)
    @Query("SELECT m FROM Member m WHERE YEAR(CURRENT_DATE) - YEAR(m.dateOfBirth) >= 60")
    List<Member> findMembersEligibleForRetirement();

    // Count active members
    long countByActiveTrue();



}
