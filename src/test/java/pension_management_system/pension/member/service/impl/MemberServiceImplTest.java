package pension_management_system.pension.member.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pension_management_system.pension.common.exception.MemberNotFoundException;
import pension_management_system.pension.member.dto.MemberRequest;
import pension_management_system.pension.member.dto.MemberResponse;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.mapper.MemberMapper;
import pension_management_system.pension.member.repository.MemberRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;
    private MemberRequest testRequest;
    private MemberResponse testResponse;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .memberId("MEM123456")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .memberStatus(MemberStatus.ACTIVE)
                .active(true)
                .build();

        testRequest = MemberRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        testResponse = MemberResponse.builder()
                .id(1L)
                .memberId("MEM123456")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    void registerMember_WithValidData_ShouldReturnMemberResponse() {
        // Arrange
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(memberMapper.toEntity(any(MemberRequest.class))).thenReturn(testMember);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(memberMapper.toResponse(any(Member.class))).thenReturn(testResponse);

        // Act
        MemberResponse result = memberService.registerMember(testRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        verify(memberRepository).save(any(Member.class));
        verify(memberMapper).toResponse(any(Member.class));
    }

    @Test
    void registerMember_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> memberService.registerMember(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");

        verify(memberRepository, never()).save(any());
    }

    @Test
    void getMemberById_WithValidId_ShouldReturnMember() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberMapper.toResponse(any(Member.class))).thenReturn(testResponse);

        // Act
        MemberResponse result = memberService.getMemberById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(memberRepository).findById(1L);
    }

    @Test
    void getMemberById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> memberService.getMemberById(999L))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    void deactivateMember_WithValidId_ShouldDeactivateMember() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // Act
        memberService.deactivateMember(1L);

        // Assert
        verify(memberRepository).findById(1L);
        verify(memberRepository).save(any(Member.class));
    }
}
