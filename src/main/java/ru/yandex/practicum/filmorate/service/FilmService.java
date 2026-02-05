package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final ValidationService validationService;
    private final UserService userService;

    public Film create(Film film) {
        validationService.validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID фильма не может быть null");
        }
        validationService.validateFilm(film);
        ensureFilmExists(film.getId());
        return filmStorage.update(film);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("ID фильма должен быть положительным числом");
        }
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        validateFilmAndUserIds(filmId, userId);

        Film film = findById(filmId);
        userService.findById(userId); // Проверяем, что пользователь существует

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        film.addLike(userId);
        filmStorage.update(film);

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateFilmAndUserIds(filmId, userId);

        Film film = findById(filmId);

        if (!film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }

        film.removeLike(userId);
        filmStorage.update(film);

        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count != null && count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }

        int limit = (count == null) ? 10 : count;

        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void ensureFilmExists(Long id) {
        if (!filmStorage.existsById(id)) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
    }

    private void validateFilmAndUserIds(Long filmId, Long userId) {
        if (filmId == null || filmId <= 0) {
            throw new ValidationException("ID фильма должен быть положительным числом");
        }
        if (userId == null || userId <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
    }
}