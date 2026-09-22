package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.comment.dto.CommentsDTO;
import ru.practicum.shareit.comment.mapping.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class CommentTest {
    private CommentMapper commentMapper;
    private User testUser;
    private Item testItem;

    @BeforeEach
    void settingBeforeEach() {
        commentMapper = new CommentMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Роман");

        testItem = new Item();
        testItem.setId(10L);
        testItem.setName("Дрель");
    }

    @Test
    public void testToCommentCorrect() {
        CommentsDTO dto = new CommentsDTO();
        dto.setText("Прекрасный инструмент!");

        Comment comment = CommentMapper.toComment(dto, testItem, testUser);


        Assertions.assertNotNull(comment);
        Assertions.assertEquals("Прекрасный инструмент!", comment.getText());
        Assertions.assertEquals(testItem, comment.getItem());
        Assertions.assertEquals(testUser, comment.getAuthor());
    }


    @Test
    public void testToCommentWithNullValues() {
        CommentsDTO dto = new CommentsDTO(); // Текст null по умолчанию

        Comment comment = CommentMapper.toComment(dto, null, null);

        Assertions.assertNotNull(comment);
        Assertions.assertNull(comment.getText());
        Assertions.assertNull(comment.getItem());
        Assertions.assertNull(comment.getAuthor());
    }

    @Test
    public void testToCommentsDTOCorrect() {
        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment();
        comment.setId(5L);
        comment.setText("Все отлично работает");
        comment.setAuthor(testUser);
        comment.setCreationDate(now);

        CommentsDTO dto = commentMapper.toCommentsDTO(comment);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(5L, dto.getId());
        Assertions.assertEquals("Все отлично работает", dto.getText());
        Assertions.assertEquals(now, dto.getCreated());
        Assertions.assertEquals("Роман", dto.getAuthorName());
    }

    @Test
    public void testToCommentsDTOWithNullAuthor() {
        Comment comment = new Comment();
        comment.setId(5L);
        comment.setText("Анонимный отзыв");
        comment.setAuthor(null); // Автор null

        CommentsDTO dto = commentMapper.toCommentsDTO(comment);

        // Проверяем, что имя автора осталось null и метод не выбросил NullPointerException
        Assertions.assertNotNull(dto);
        Assertions.assertNull(dto.getAuthorName());
        Assertions.assertEquals("Анонимный отзыв", dto.getText());
    }

}
