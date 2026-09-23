package ru.practicum.shareit.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NoDataFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapping.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@Transactional
@SpringBootTest()
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepositoryMock;

    private User testUser1;
    private User testUser2;
    private final UserService service;
    private final UserMapper userMapper;

    @BeforeEach
    void settingBeforeEach() {
        testUser1 = new User();
        testUser1.setName("Роман");

        testUser1.setEmail("kosticin@test.ru");

        testUser2 = new User();
        testUser2.setName("Тест");
        testUser2.setEmail("test@test.ru");
    }

    @Test
    public void testFindAllCorrect() {
        UserService userService = new UserServiceImpl(userMapper, userRepositoryMock);
        Mockito
                .when(userRepositoryMock.findAll())
                .thenReturn(List.of(testUser1, testUser2));

        Collection<UserDTO> listUserDTO = userService.getAllUsers();
        Assertions.assertEquals(2, listUserDTO.size());
    }

    @Test
    public void testFindByIdCorrect() {
        UserService userService = new UserServiceImpl(userMapper, userRepositoryMock);
        Mockito
                .when(userRepositoryMock.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(testUser1));
        UserDTO userDTO = userService.getUser(1L);
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getName().equals("Роман"));
        assertThat(userDTO.getEmail().equals("kosticin@test.ru"));
    }

    @Test
    public void testFindByIdError_whenUserNotExist() {
        UserService userService = new UserServiceImpl(userMapper, userRepositoryMock);
        Mockito
                .when(userRepositoryMock.findById(9999999L))
                .thenThrow(new NoDataFoundException("Пользователь с id 9999999 не найден"));

        final NoDataFoundException notFoundException = Assertions.assertThrows(NoDataFoundException.class,
                () -> userService.getUser(9999999L));

        Assertions.assertEquals("Пользователь с id 9999999 не найден", notFoundException.getMessage());
    }

    @Test
    void testInsertUserCorrect() {
        UserDTO result = service.insertUser(userMapper.userToUserDTO(testUser1));
        MatcherAssert.assertThat(result.getId(), notNullValue());

        UserDTO userDTO = service.getUser(result.getId());

        MatcherAssert.assertThat(testUser1.getName(), equalTo(userDTO.getName()));
        MatcherAssert.assertThat(testUser1.getEmail(), equalTo(userDTO.getEmail()));
    }


    @Test
    void testInsertUserError_whenEmailIsDuplicate() {
        UserService userService = new UserServiceImpl(userMapper, userRepositoryMock);

        User incorrectUser = new User();
        incorrectUser.setName("TEST");
        incorrectUser.setEmail("kosticin@test.ru");

        Mockito.when(userRepositoryMock.save(Mockito.any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        assertThrows(ConflictException.class, () ->
                userService.insertUser(userMapper.userToUserDTO(incorrectUser))
        );
    }

    @Test
    void testUpdateUserCorrect() {
        UserDTO result = service.insertUser(userMapper.userToUserDTO(testUser1));
        UserDTO changedUser = UserDTO.builder()
                .name("TEST 2")
                .email("test2@test.ru")
                .build();
        UserDTO updatedUser = service.updateUser(result.getId(), changedUser);

        assertThat(updatedUser.getName()).isEqualTo("TEST 2");
        assertThat(updatedUser.getEmail()).isEqualTo("test2@test.ru");
    }

    @Test
    void testUpdateUserError_whenIncorrectEmail() {
        UserDTO result = service.insertUser(userMapper.userToUserDTO(testUser1));
        UserDTO incorrectUser = UserDTO.builder()
                .name("TEST ")
                .email("54654645645")
                .build();

        assertThrows(ValidationException.class, () -> service.updateUser(result.getId(), incorrectUser));
    }

    @Test
    void testDeleteUserCorrect() {
        UserDTO result = service.insertUser(userMapper.userToUserDTO(testUser1));
        service.deleteUser(result.getId());
        assertThrows(NoDataFoundException.class, () -> service.getUser(result.getId()));
    }
}
