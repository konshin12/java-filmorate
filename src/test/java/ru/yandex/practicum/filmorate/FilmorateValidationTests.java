package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class FilmorateValidationTests {

        @Autowired
        private ValidationService validationService;


    @Test
    public void shouldValidateCorrectUser() {
        // Подготовка
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validlogin");
        user.setName("Valid User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // Действие и проверка
        assertDoesNotThrow(() -> validationService.validateUser(user));
    }

    @Test
    public void shouldValidateCorrectFilm() {
        // Подготовка
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description under 200 characters");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        // Действие и проверка (не должно быть исключений)
        assertDoesNotThrow(() -> validationService.validateFilm(film));
    }

    @Test
    public void shouldAcceptValidEmailWithSpecialCharacters() {
        // Подготовка
        User user = new User();
        user.setEmail("test.email+tag@domain.co.uk");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // Действие и проверка
        assertDoesNotThrow(() -> validationService.validateUser(user));
    }
}



