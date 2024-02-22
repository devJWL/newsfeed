package com.ptjcoding.nbcampspringnewsfeed.domain.member.service;

import com.ptjcoding.nbcampspringnewsfeed.domain.blacklist.repository.BlackListRepository;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.dto.LoginRequestDto;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.dto.NicknameUpdateRequestDto;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.dto.SignupRequestDto;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.infrastructure.entity.MemberEntity;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.model.Member;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.model.MemberRole;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.repository.MemberRepository;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.repository.dto.NicknameUpdateDto;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.service.dto.MemberResponseDto;
import com.ptjcoding.nbcampspringnewsfeed.domain.member.service.dto.NicknameChangeDto;
import com.ptjcoding.nbcampspringnewsfeed.global.exception.CustomRuntimeException;
import com.ptjcoding.nbcampspringnewsfeed.global.exception.GlobalErrorCode;
import com.ptjcoding.nbcampspringnewsfeed.global.jwt.JwtProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {
    @InjectMocks
    MemberServiceImpl memberService;
    @Mock
    MemberRepository memberRepository;
    @Mock
    BlackListRepository  blacklistRepository;
    @Mock
    JwtProvider jwtProvider;
    @Mock
    MemberEntity memberEntity;
    static final String validEmail = "valid@email.com";
    static final String validNickname = "validnickname";
    static final String validPassword = "validpasswordVP1234";

    static final String ACCESS_TOKEN = "testAccessToken";
    static final String REFRESH_TOKEN = "testRefreshToken";

    @Nested
    @DisplayName("회원 성공 테스트")
    class MemberSuccessTest {
        @Test
        @DisplayName("회원 가입 성공")
        void signupSuccessTest() {
            // given
            SignupRequestDto signupRequestDto = new SignupRequestDto();
            ReflectionTestUtils.setField(signupRequestDto, "email", validEmail);
            ReflectionTestUtils.setField(signupRequestDto, "nickname", validNickname);
            ReflectionTestUtils.setField(signupRequestDto, "password", validPassword);
            ReflectionTestUtils.setField(signupRequestDto, "checkPassword", validPassword);

            given(memberRepository.checkEmail(validEmail)).willReturn(false);

            // when
            MemberResponseDto memberResponseDto = memberService.signup(signupRequestDto);

            // then
            assertEquals(signupRequestDto.getNickname(), memberResponseDto.getNickname());
            assertEquals(signupRequestDto.getEmail(), memberResponseDto.getEmail());
        }

        @Test
        @DisplayName("로그인 성공")
        void loginSuccessTest() {
            // given
            LoginRequestDto loginRequestDto = new LoginRequestDto();
            ReflectionTestUtils.setField(loginRequestDto, "email", validEmail);
            ReflectionTestUtils.setField(loginRequestDto, "password", validPassword);
            MockHttpServletResponse response = new MockHttpServletResponse();
            given(blacklistRepository.checkEmail(loginRequestDto.getEmail())).willReturn(false);
            given(memberRepository.checkEmail(loginRequestDto.getEmail())).willReturn(true);
            given(memberRepository.checkPassword(loginRequestDto)).willReturn(
                    new Member(1L, validEmail, validNickname, validPassword, MemberRole.USER)
            );

            given(jwtProvider.generateAccessToken(1L, MemberRole.USER.getAuthority())).willReturn(ACCESS_TOKEN);
            given(jwtProvider.generateRefreshToken(1L, MemberRole.USER.getAuthority())).willReturn(REFRESH_TOKEN);

            // when
            memberService.login(loginRequestDto, response);

            // then
            then(jwtProvider).should(times(1)).addAccessTokenToCookie(ACCESS_TOKEN, response);
            then(jwtProvider).should(times(1)).addRefreshTokenToCookie(REFRESH_TOKEN, response);
        }

        @Test
        @DisplayName("회원 이름변경 성공")
        void updateMemberNameTest() {
            // given
            final String afterNickname = "afterNickname";
            Member member = new Member(1L, validEmail, validNickname, validPassword, MemberRole.USER);
            given(memberRepository.updateMember(eq(member.getId()), any(NicknameUpdateDto.class))).willReturn(
                    new Member(1L, validEmail, afterNickname, validPassword, MemberRole.USER)
            );
            // when
            NicknameChangeDto nicknameChangeDto = memberService.updateMemberName(member, new NicknameUpdateRequestDto());
            // then
            assertNotEquals(validNickname, nicknameChangeDto.getAfterName());
            assertEquals(afterNickname, nicknameChangeDto.getAfterName());
        }
    }

    @Nested
    @DisplayName("회원 실패 테스트")
    class MemberFailTest {
        @Test
        @DisplayName("회원 가입 실패")
        void signupFailTest() {
            // given
            SignupRequestDto signupRequestDto = new SignupRequestDto();
            ReflectionTestUtils.setField(signupRequestDto, "email", "test1@email.com");
            ReflectionTestUtils.setField(signupRequestDto, "nickname", "test");
            ReflectionTestUtils.setField(signupRequestDto, "password", "testTest1234");
            ReflectionTestUtils.setField(signupRequestDto, "checkPassword", "testTest1234");

            // when
            given(memberRepository.checkEmail("test1@email.com")).willReturn(true);

            // then
            assertThrows(Exception.class, () -> memberService.signup(signupRequestDto));
        }
    }

    @RepeatedTest(3)
    @DisplayName("로그인 실패")
    void loginFailTest(RepetitionInfo info) {
        // given
        boolean[] emailBooleans = {false, true, true};
        boolean[] passwordBooleans = {false ,true, false};
        int idx = info.getCurrentRepetition() - 1;
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        ReflectionTestUtils.setField(loginRequestDto, "email", validEmail);
        ReflectionTestUtils.setField(loginRequestDto, "password", validPassword);
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(blacklistRepository.checkEmail(loginRequestDto.getEmail())).willReturn(emailBooleans[idx]);
        lenient().when(memberRepository.checkEmail(loginRequestDto.getEmail())).thenReturn(passwordBooleans[idx]);

        // when - then
        assertThrows(CustomRuntimeException.class, () -> memberService.login(loginRequestDto, response));
    }

    @Test
    @DisplayName("회원 이름변경 실패")
    void updateMemberNameTest() {
        // given
        Member member = new Member(1L, validEmail, validNickname, validPassword, MemberRole.USER);
        given(memberRepository.updateMember(eq(member.getId()), any(NicknameUpdateDto.class)))
                .willThrow(new CustomRuntimeException(GlobalErrorCode.UNCHANGED));
        NicknameUpdateRequestDto nicknameUpdateRequestDto = new NicknameUpdateRequestDto();

        // when
        CustomRuntimeException e = assertThrows(CustomRuntimeException.class,
                () -> memberService.updateMemberName(member, nicknameUpdateRequestDto));

        // then
        assertEquals("[RUNTIME ERROR] " + GlobalErrorCode.UNCHANGED.getMessage(), e.getMessage());
    }
}