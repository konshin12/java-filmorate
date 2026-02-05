package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final ValidationService validationService;

    public User create(User user) {
        validationService.validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }
        validationService.validateUser(user);
        ensureUserExists(user.getId());
        return userStorage.update(user);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        validateIds(userId, friendId);

        User user = findById(userId);
        User friend = findById(friendId);

        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователь " + userId + " уже в друзьях у пользователя " + friendId);
        }

        user.addFriend(friendId);
        friend.addFriend(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        validateIds(userId, friendId);

        User user = findById(userId);
        User friend = findById(friendId);

        if (!user.getFriends().contains(friendId)) {
            // Меняем на более подходящее исключение
            throw new ValidationException("Пользователь " + userId + " не в друзьях у пользователя " + friendId);
        }

        user.removeFriend(friendId);
        friend.removeFriend(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        User user = findById(userId);
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        validateIds(userId, otherId);

        User user = findById(userId);
        User otherUser = findById(otherId);

        Set<Long> commonFriendIds = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonFriendIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    private void ensureUserExists(Long id) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    private void validateIds(Long id1, Long id2) {
        if (id1 == null || id1 <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        if (id2 == null || id2 <= 0) {
            throw new ValidationException("ID друга должен быть положительным числом");
        }
        if (id1.equals(id2)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }
    }
}