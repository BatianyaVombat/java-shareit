package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CommentRepository {
    private final Map<Long, Comment> commentStorage = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Comment createComment(Comment comment) {
        Comment newComment = Comment.builder()
                .id(idCounter.getAndIncrement())
                .text(comment.getText())
                .item(comment.getItem())
                .author(comment.getAuthor())
                .created(comment.getCreated())
                .build();

        commentStorage.put(newComment.getId(), comment);
        return newComment;
    }

    public Collection<Comment> findAllByItemId(Long itemId) {
        return commentStorage.values().stream()
                .filter(comment -> comment.getItem().getId().equals(itemId))
                .collect(Collectors.toList());
    }

    public Collection<Comment> findAllByItemIdIn(Collection<Long> itemIds) {
        return commentStorage.values().stream()
                .filter(c -> itemIds.contains(c.getItem().getId()))
                .collect(Collectors.toList());
    }
}
