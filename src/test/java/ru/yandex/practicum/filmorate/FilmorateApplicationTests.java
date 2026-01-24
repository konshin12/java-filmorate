package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmorateApplicationTests {

	@Autowired
	private ValidationService validationService;

	@Test
	public void contextLoads() {
		// Проверяем, что контекст Spring успешно загружается
		assertThat(validationService).isNotNull();
		System.out.println("Контекст Spring успешно загружен!");
	}

	@Test
	public void shouldThrowExceptionWhenFilmNameIsEmpty() {
		// Подготовка
		Film film = new Film();
		film.setName("");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateFilm(film));

		assertThat(exception.getMessage()).contains("Название");
	}

	@Test
	public void shouldThrowExceptionWhenFilmNameIsNull() {
		// Подготовка
		Film film = new Film();
		film.setName(null);
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateFilm(film));

		assertThat(exception.getMessage()).contains("Название");
	}

	@Test
	public void shouldThrowExceptionWhenFilmDescriptionIsTooLong() {
		// Подготовка
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("A".repeat(201)); // 201 символов
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateFilm(film));

		assertThat(exception.getMessage()).contains("200 символов");
	}

	@Test
	public void shouldAcceptFilmDescriptionWithMaxLength() {
		// Подготовка
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("A".repeat(200)); // Ровно 200 символов
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		// Действие и проверка
		assertDoesNotThrow(() -> validationService.validateFilm(film));
	}

	@Test
	public void shouldThrowExceptionWhenFilmReleaseDateIsTooEarly() {
		// Подготовка
		Film film = new Film();
		film.setName("Test Film");
		film.setReleaseDate(LocalDate.of(1895, 12, 27)); // На день раньше
		film.setDuration(120);

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateFilm(film));

		assertThat(exception.getMessage()).contains("28 декабря 1895");
	}

	@Test
	public void shouldAcceptFilmWithEarliestReleaseDate() {
		// Подготовка
		Film film = new Film();
		film.setName("Test Film");
		film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Самая ранняя допустимая дата
		film.setDuration(120);

		// Действие и проверка
		assertDoesNotThrow(() -> validationService.validateFilm(film));
	}

	@Test
	public void shouldThrowExceptionWhenFilmDurationIsNegative() {
		// Подготовка
		Film film = new Film();
		film.setName("Test Film");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(-10);

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateFilm(film));

		assertThat(exception.getMessage()).contains("положительным");
	}

	@Test
	public void shouldThrowExceptionWhenFilmDurationIsZero() {
		// Подготовка
		Film film = new Film();
		film.setName("Test Film");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(0);

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateFilm(film));

		assertThat(exception.getMessage()).contains("положительным");
	}

	@Test
	public void shouldUseLoginAsNameWhenNameIsEmpty() {
		// Подготовка
		User user = new User();
		user.setEmail("test@email.com");
		user.setLogin("testlogin");
		user.setName("");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		// Действие
		validationService.validateUser(user);

		// Проверка
		assertEquals("testlogin", user.getName());
	}

	@Test
	public void shouldUseLoginAsNameWhenNameIsNull() {
		// Подготовка
		User user = new User();
		user.setEmail("test@email.com");
		user.setLogin("testlogin");
		user.setName(null);
		user.setBirthday(LocalDate.of(1990, 1, 1));

		// Действие
		validationService.validateUser(user);

		// Проверка
		assertEquals("testlogin", user.getName());
	}

	@Test
	public void shouldThrowExceptionWhenUserEmailIsEmpty() {
		// Подготовка
		User user = new User();
		user.setEmail("");
		user.setLogin("testlogin");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateUser(user));

		assertThat(exception.getMessage()).contains("Электронная почта");
	}

	@Test
	public void shouldThrowExceptionWhenUserEmailIsInvalid() {
		// Подготовка
		User user = new User();
		user.setEmail("invalid-email"); // Нет символа @
		user.setLogin("testlogin");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateUser(user));

		assertThat(exception.getMessage()).contains("символ @");
	}

	@Test
	public void shouldThrowExceptionWhenUserLoginIsEmpty() {
		// Подготовка
		User user = new User();
		user.setEmail("test@email.com");
		user.setLogin("");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateUser(user));

		assertThat(exception.getMessage()).contains("Логин");
	}

	@Test
	public void shouldThrowExceptionWhenUserLoginHasSpaces() {
		// Подготовка
		User user = new User();
		user.setEmail("test@email.com");
		user.setLogin("test login"); // Пробел в логине
		user.setBirthday(LocalDate.of(1990, 1, 1));

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateUser(user));

		assertThat(exception.getMessage()).contains("пробелы");
	}

	@Test
	public void shouldThrowExceptionWhenUserBirthdayIsInFuture() {
		// Подготовка
		User user = new User();
		user.setEmail("test@email.com");
		user.setLogin("testlogin");
		user.setBirthday(LocalDate.now().plusDays(1)); // Завтра

		// Действие и проверка
		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateUser(user));

		assertThat(exception.getMessage()).contains("будущем");
	}

	@Test
	public void shouldAcceptUserWithCurrentDateBirthday() {
		// Подготовка
		User user = new User();
		user.setEmail("test@email.com");
		user.setLogin("testlogin");
		user.setBirthday(LocalDate.now()); // Сегодня

		// Действие и проверка
		assertDoesNotThrow(() -> validationService.validateUser(user));
	}
}