package ru.yandex.practicum.catsgram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    public Collection<Post> findAll(String sort, Integer size, Integer from) {
        if(!"asc".equals(sort) && !"desc".equals(sort) && !"descending".equals(sort)){
            throw new ParameterNotValidException(
                    String.format("%s", sort),
                    "Некорректное значение сортировки. Значение должно быть: asc или desc или descending"
            );
        }
        if(size <= 0){
            throw new ParameterNotValidException(
                    String.format("%s", size),
                    "Некорректный размер выборки. Размер должен быть больше нуля"
            );
        }
        if(from < 0){
            throw new ParameterNotValidException(
                    String.format("%s", from),
                    "Некорректное значение параметра from. Значение не может быть меньше нуля"
            );
        }
        List<Post> sortedPostList = posts.values().stream()
                .sorted(Comparator.comparing(Post::getPostDate))
                .collect(Collectors.toList());
        if (SortOrder.sortCategory(sort).equals(SortOrder.DESCENDING)) {
            Collections.reverse(sortedPostList);
        }
        if (from + size < sortedPostList.size()) {
            return sortedPostList.stream()
                    .skip(from)
                    .collect(Collectors.toList());
        } else {
            sortedPostList.subList(from, size).clear();
            return sortedPostList;
        }
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        if (userService.findUserById(post.getAuthorId()).isEmpty()) {
            throw new ConditionsNotMetException("Автор с id = " + post.getAuthorId() + " не найден");
        }
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Post findPostById(Long id) {
        return posts.values()
                .stream()
                .filter(post -> post.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ConditionsNotMetException("Пост с id " + id + " не найден"));
    }

    public enum SortOrder {
        ASCENDING, DESCENDING;

        // Преобразует строку в элемент перечисления
        public static SortOrder sortCategory(String order) {
            return switch (order.toLowerCase()) {
                case "descending", "desc" -> DESCENDING;
                default -> ASCENDING;
            };
        }
    }

}

