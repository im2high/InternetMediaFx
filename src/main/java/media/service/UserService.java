package media.service;

import media.exception.BusinessException;
import media.model.User;
import media.dao.UserRepository;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    public void createUser(String username, String email, String role) throws SQLException {
        if (username == null || username.isBlank())
            throw new BusinessException("Логин не может быть пустым");
        if (email == null || email.isBlank())
            throw new BusinessException("Email не может быть пустым");
        if (role == null || role.isBlank())
            throw new BusinessException("Роль не может быть пустой");

        userRepository.create(new User(0, username, email, role));
    }

    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }

    public User getById(int id) throws SQLException {
        return userRepository.findById(id);
    }
}