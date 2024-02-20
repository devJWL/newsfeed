package com.ptjcoding.nbcampspringnewsfeed.domain.member.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestDtoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Nested
    @DisplayName("성공 테스트")
    class LoginRequsetDtoSuccess{
        @Test
        @DisplayName("이메일 검증")
        void validateEmail() {
            LoginRequestDto loginRequestDto = new LoginRequestDto();
            final String vaildatedPassword = "abAB123456";

            ReflectionTestUtils.setField(loginRequestDto, "email", "emailemail.com");
            ReflectionTestUtils.setField(loginRequestDto, "password", vaildatedPassword);
            Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(loginRequestDto);
            for (ConstraintViolation<LoginRequestDto> violation : violations) {
                System.out.println("메세지 : " + violation.getMessage());
            }
            assertEquals(1, violations.size());
        }

        @Test
        @DisplayName("패스워드 검증")
        void validatePassword() {
            LoginRequestDto loginRequestDto = new LoginRequestDto();
            final String vaildatedEmail = "email@email.com";

            ReflectionTestUtils.setField(loginRequestDto, "email", vaildatedEmail);
            ReflectionTestUtils.setField(loginRequestDto, "password", "asdfsfA1");

            Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(loginRequestDto);
            for (ConstraintViolation<LoginRequestDto> violation : violations) {
                System.out.println("메세지 : " + violation.getMessage());
            }
            assertTrue(violations.isEmpty());
        }
    }
}
