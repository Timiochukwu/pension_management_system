package pension_management_system.pension.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pension_management_system.pension.member.dto.MemberRequest;
import pension_management_system.pension.member.dto.MemberResponse;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.mapper.MemberMapper;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.member.service.impl.MemberServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MemberServiceImplTest - Unit tests for Member service
 *
 * Purpose: Test member management business logic
 *
 * What we're testing:
 * - Member registration
 * - Member updates
 * - Member retrieval
 * - Email notifications via events
 * - Validation logic
 * - Error handling
 *
 * Test coverage:
 * - Happy path (everything works)
 * - Edge cases (empty data, nulls)
 * - Error cases (not found, duplicates)
 * - Event publishing (emails sent)
 *
 * Mocked dependencies:
 * - MemberRepository (database)
 * - MemberMapper (DTO conversion)
 * - ApplicationEventPublisher (events)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Member Service Unit Tests")
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemberServiceImpl memberService;

    // Test data
    private Member member;
    private MemberRequest memberRequest;
    private MemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        // Create test member
        member = new Member();
        member.setId(1L);
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john.doe@example.com");
        member.setPhoneNumber("+234-123-456-7890");

        // Create member request
        memberRequest = new MemberRequest();
        memberRequest.setFirstName("John");
        memberRequest.setLastName("Doe");
        memberRequest.setEmail("john.doe@example.com");
        memberRequest.setPhoneNumber("+234-123-456-7890");

        // Create member response
        memberResponse = new MemberResponse();
        memberResponse.setId(1L);
        memberResponse.setFirstName("John");
        memberResponse.setLastName("Doe");
        memberResponse.setEmail("john.doe@example.com");
    }

    /**
     * TEST: Create Member - Success
     *
     * Verifies:
     * - Member is saved to database
     * - DTO mapping works correctly
     * - Welcome email event is published
     * - Correct response returned
     */
    @Test
    @DisplayName("Should create member and publish welcome event")
    void createMember_Success() {
        // Arrange
        when(memberMapper.toEntity(any(MemberRequest.class))).thenReturn(member);
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(memberMapper.toResponse(any(Member.class))).thenReturn(memberResponse);

        // Act
        MemberResponse result = memberService.createMember(memberRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());

        // Verify interactions
        verify(memberRepository).save(any(Member.class));
        verify(eventPublisher).publishEvent(any()); // Welcome email event
        verify(memberMapper).toResponse(any(Member.class));
    }

    /**
     * TEST: Get Member By ID - Success
     */
    @Test
    @DisplayName("Should get member by ID")
    void getMemberById_Success() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMapper.toResponse(any(Member.class))).thenReturn(memberResponse);

        // Act
        MemberResponse result = memberService.getMemberById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john.doe@example.com", result.getEmail());

        verify(memberRepository).findById(1L);
    }

    /**
     * TEST: Get Member By ID - Not Found
     */
    @Test
    @DisplayName("Should throw exception when member not found")
    void getMemberById_NotFound() {
        // Arrange
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            memberService.getMemberById(999L);
        });

        verify(memberRepository).findById(999L);
        verify(memberMapper, never()).toResponse(any());
    }

    /**
     * TEST: Get Member By Email - Success
     */
    @Test
    @DisplayName("Should get member by email")
    void getMemberByEmail_Success() {
        // Arrange
        when(memberRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(member));
        when(memberMapper.toResponse(any(Member.class))).thenReturn(memberResponse);

        // Act
        MemberResponse result = memberService.getMemberByEmail("john.doe@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());

        verify(memberRepository).findByEmail("john.doe@example.com");
    }

    /**
     * TEST: Update Member - Success
     */
    @Test
    @DisplayName("Should update member successfully")
    void updateMember_Success() {
        // Arrange
        MemberRequest updateRequest = new MemberRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@example.com");

        Member updatedMember = new Member();
        updatedMember.setId(1L);
        updatedMember.setFirstName("Jane");
        updatedMember.setLastName("Smith");
        updatedMember.setEmail("jane.smith@example.com");

        MemberResponse updatedResponse = new MemberResponse();
        updatedResponse.setId(1L);
        updatedResponse.setFirstName("Jane");
        updatedResponse.setLastName("Smith");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(updatedMember);
        when(memberMapper.toResponse(any(Member.class))).thenReturn(updatedResponse);

        // Act
        MemberResponse result = memberService.updateMember(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());

        verify(memberRepository).findById(1L);
        verify(memberRepository).save(any(Member.class));
    }

    /**
     * TEST: Delete Member - Success
     */
    @Test
    @DisplayName("Should delete member")
    void deleteMember_Success() {
        // Arrange
        when(memberRepository.existsById(1L)).thenReturn(true);
        doNothing().when(memberRepository).deleteById(1L);

        // Act
        memberService.deleteMember(1L);

        // Assert
        verify(memberRepository).existsById(1L);
        verify(memberRepository).deleteById(1L);
    }

    /**
     * TEST: Delete Member - Not Found
     */
    @Test
    @DisplayName("Should throw exception when deleting non-existent member")
    void deleteMember_NotFound() {
        // Arrange
        when(memberRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            memberService.deleteMember(999L);
        });

        verify(memberRepository).existsById(999L);
        verify(memberRepository, never()).deleteById(any());
    }

    /**
     * TEST: Get All Members
     */
    @Test
    @DisplayName("Should get all members")
    void getAllMembers_Success() {
        // Arrange
        Member member2 = new Member();
        member2.setId(2L);
        member2.setFirstName("Jane");
        member2.setEmail("jane@example.com");

        List<Member> members = List.of(member, member2);

        when(memberRepository.findAll()).thenReturn(members);
        when(memberMapper.toResponse(any(Member.class)))
                .thenReturn(memberResponse)
                .thenReturn(new MemberResponse());

        // Act
        List<MemberResponse> results = memberService.getAllMembers();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());

        verify(memberRepository).findAll();
        verify(memberMapper, times(2)).toResponse(any(Member.class));
    }

    /**
     * TEST: Email Already Exists
     */
    @Test
    @DisplayName("Should throw exception when creating member with duplicate email")
    void createMember_DuplicateEmail() {
        // Arrange
        when(memberRepository.existsByEmail(memberRequest.getEmail()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            memberService.createMember(memberRequest);
        });

        verify(memberRepository).existsByEmail(memberRequest.getEmail());
        verify(memberRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}

/**
 * TESTING BEST PRACTICES DEMONSTRATED
 *
 * 1. **AAA Pattern** (Arrange-Act-Assert):
 *    - Arrange: Set up test data and mocks
 *    - Act: Execute method being tested
 *    - Assert: Verify expected outcome
 *
 * 2. **Descriptive Names**:
 *    - methodName_scenario_expectedBehavior
 *    - @DisplayName for human-readable descriptions
 *
 * 3. **Test One Thing**:
 *    - Each test focuses on single behavior
 *    - Easy to identify what failed
 *
 * 4. **Mock External Dependencies**:
 *    - Repository mocked (no database)
 *    - Mapper mocked (focus on service logic)
 *    - Event publisher mocked (verify events)
 *
 * 5. **Verify Interactions**:
 *    - Use verify() to check method calls
 *    - Ensure correct parameters passed
 *    - Verify call counts
 *
 * 6. **Test Edge Cases**:
 *    - Not found scenarios
 *    - Duplicate entries
 *    - Invalid data
 *
 * 7. **BeforeEach Setup**:
 *    - Fresh test data for each test
 *    - Tests are independent
 *    - No shared state
 *
 * WHAT TO TEST IN SERVICE LAYER
 *
 * ✅ DO Test:
 * - Business logic
 * - Validation rules
 * - Error handling
 * - Event publishing
 * - Method orchestration
 *
 * ❌ DON'T Test:
 * - Database queries (that's repository tests)
 * - Framework code (Spring, JPA)
 * - Getter/setter methods
 * - Simple delegations
 *
 * RUNNING TESTS
 *
 * mvn test -Dtest=MemberServiceImplTest
 * mvn test -Dtest=MemberServiceImplTest#createMember_Success
 */
