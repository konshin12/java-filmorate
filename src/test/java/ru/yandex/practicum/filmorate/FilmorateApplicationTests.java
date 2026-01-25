package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmorateApplicationTests {

	@Autowired
	private ValidationService validationService;

	@Test
	void contextLoads() {
		assertNotNull(validationService);
		System.out.println("Контекст Spring успешно загружен!");
	}

	@Test
	public void shouldCreateValidFilm() {
		Film film = new Film();
		film.setName("Интерстеллар");
		film.setDescription("Фантастический фильм");
		film.setReleaseDate(LocalDate.of(2014, 10, 26));
		film.setDuration(169);

		// Просто проверяем, что не падает
		assertDoesNotThrow(() -> validationService.validateFilm(film));
	}

	@Test
	public void shouldRejectFilmWithTooEarlyReleaseDate() {
		Film film = new Film();
		film.setName("Слишком старый фильм");
		film.setReleaseDate(LocalDate.of(1890, 1, 1));

		ValidationException exception = assertThrows(ValidationException.class,
				() -> validationService.validateFilm(film));

		assertTrue(exception.getMessage().contains("28 декабря 1895"));
	}

	@Test
	public void shouldAcceptEarliestPossibleReleaseDate() {
		Film film = new Film();
		film.setName("Первый фильм");
		film.setReleaseDate(LocalDate.of(1895, 12, 28));

		assertDoesNotThrow(() -> validationService.validateFilm(film));
	}

	@Test
	public void shouldSetLoginAsNameWhenNameIsEmpty() {
		User user = new User();
		user.setEmail("user@mail.com");
		user.setLogin("user123");
		user.setName("");

		validationService.validateUser(user);

		assertEquals("user123", user.getName(), "Должен использовать логин как имя");
	}

	@Test
	public void shouldSetLoginAsNameWhenNameIsNull() {
		User user = new User();
		user.setEmail("user@mail.com");
		user.setLogin("user123");
		user.setName(null);

		validationService.validateUser(user);

		assertEquals("user123", user.getName(), "Должен использовать логин как имя");
	}

	@Test
	public void shouldKeepNameWhenNameIsProvided() {
		User user = new User();
		user.setEmail("user@mail.com");
		user.setLogin("user123");
		user.setName("Иван Иванов"); // Имя указано

		validationService.validateUser(user);

		assertEquals("Иван Иванов", user.getName(), "Должен сохранить указанное имя");
	}

	@Test
	public void shouldHandleUserWithSpacesInName() {
		User user = new User();
		user.setEmail("user@mail.com");
		user.setLogin("user123");
		user.setName("Иван Петров Сидоров");

		validationService.validateUser(user);

		assertEquals("Иван Петров Сидоров", user.getName(), "Должен сохранить имя с пробелами");
	}

	@Test
	public void testCompleteUserCreation() {
		User user = new User();
		user.setEmail("john.doe@example.com");
		user.setLogin("johndoe");
		user.setBirthday(LocalDate.of(1990, 5, 15));

		// Проверяем что можно создать пользователя без имени
		validationService.validateUser(user);

		assertEquals("johndoe", user.getName());
		assertNotNull(user.getEmail());
		assertNotNull(user.getLogin());
	}

	@Test
	public void testCompleteFilmCreation() {
		Film film = new Film();
		film.setName("Матрица");
		film.setDescription("Научно-фантастический фильм");
		film.setReleaseDate(LocalDate.of(1999, 3, 31));
		film.setDuration(136);

		assertDoesNotThrow(() -> validationService.validateFilm(film));
	}

	@Test
	public void shouldHandleEdgeCases() {
		// Фильм с минимальной длительностью
		Film film1 = new Film();
		film1.setName("Короткий фильм");
		film1.setReleaseDate(LocalDate.of(2000, 1, 1));
		film1.setDuration(1); // Минимальная положительная длительность

		assertDoesNotThrow(() -> validationService.validateFilm(film1));

		// Фильм с сегодняшней датой релиза
		Film film2 = new Film();
		film2.setName("Новый фильм");
		film2.setReleaseDate(LocalDate.now());
		film2.setDuration(120);

		assertDoesNotThrow(() -> validationService.validateFilm(film2));
	}
}