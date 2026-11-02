package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTests {
    private static Validator validator;

    @BeforeAll
    static void setValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    //-----UserDto-----
    @Nested
    class UserDtoTests {
        @ParameterizedTest
        @EmptySource
        void shouldFailWhenNameIsBlank(String invalidName) {
            UserDto dto = UserDto.builder().name(invalidName).email("example@test.com").build();
            assertHasViolations(dto, 1);
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"user_name", "@test.com", "email"})
        void shouldFailWhenEmailIsIncorrectOrBlank(String invalidEmail) {
            UserDto dto = UserDto.builder().name("John").email(invalidEmail).build();
            assertHasViolations(dto, 1);
        }
    }

    //-----UpdateUserRequest-----
    @Nested
    class UpdateUserRequestTest {
        @ParameterizedTest
        @ValueSource(strings = {"user_name", "@test.com", "email"})
        void shouldFailWhenEmailIsIncorrect(String invalidEmail) {
            UpdateUserRequest dto = UpdateUserRequest.builder().name("John").email(invalidEmail).build();
            assertHasViolations(dto, 1);
        }
    }

    //-----ItemDto-----
    @Nested
    class ItemDtoTests {
        @ParameterizedTest
        @EmptySource
        void shouldFailWhenNameIsBlank(String emptyName) {
            ItemDto dto = ItemDto.builder().name(emptyName).description("some_text").available(true).build();
            assertHasViolations(dto, 1);
        }

        @ParameterizedTest
        @EmptySource
        void shouldFailWhenDescriptionIsBlank(String emptyDescription) {
            ItemDto dto = ItemDto.builder().name("Photo camera").description(emptyDescription).available(true).build();
            assertHasViolations(dto, 1);
        }

        @ParameterizedTest
        @NullSource
        void shouldFailWhenAvailableIsNull(Boolean nullParam) {
            ItemDto dto = ItemDto.builder().name("Photo camera").description("cool").available(nullParam).build();
            assertHasViolations(dto, 1);
        }

    }

    //-----NewCommentRequest-----
    @Nested
    class NewCommentRequestTests {
        @ParameterizedTest
        @EmptySource
        void shouldFailWhenCommentTextIsBlank(String emptyText) {
            NewCommentRequest dto = NewCommentRequest.builder().text(emptyText).build();
            assertHasViolations(dto, 1);
        }
    }

    //-----ItemRequestDto-----
    @Nested
    class ItemRequestDtoTest {
        @ParameterizedTest
        @EmptySource
        void shouldFailWhenDescriptionIsBlank(String emptyDescription) {
            ItemRequestDto dto = ItemRequestDto.builder().description(emptyDescription).build();
            assertHasViolations(dto, 1);
        }
    }

    //-----BookItemRequestDto-----
    @Nested
    class BookItemRequestDtoTests {
        @ParameterizedTest
        @MethodSource("provideInvalidTimeCombinations")
        void shouldFailWithInvalidTimeFields(LocalDateTime start, LocalDateTime end, String description,
                                             int violationsCount) {
            BookItemRequestDto dto = new BookItemRequestDto(1, start, end);
            assertHasViolations(dto, violationsCount);
        }

        //источник данных: генерирует неверные комбинации дат
        private static Stream<Arguments> provideInvalidTimeCombinations() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime validFutureStart = now.plusDays(1);
            LocalDateTime validFutureEnd = now.plusDays(2);

            return Stream.of(
                    Arguments.of(now.minusDays(1), validFutureEnd, "Поле 'start' в прошлом", 1),

                    Arguments.of(validFutureStart, now.minusDays(1), "Поле 'end' в прошлом", 1),

                    Arguments.of(validFutureStart, now, "Поле 'end' равно текущему времени (не в будущем)", 1),

                    Arguments.of(now.minusDays(5), now.minusDays(2), "Оба поля 'start' и 'end' в прошлом", 2)
            );
        }
    }

    private <T> void assertHasViolations(T object, int expectedCount) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        assertThat(violations)
                .as("Ожидалось %d нарушений валидации для %s", expectedCount, object.getClass()
                        .getSimpleName()).hasSize(expectedCount);
    }
}
