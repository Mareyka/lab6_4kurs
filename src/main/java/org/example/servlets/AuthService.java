package org.example.servlets;

import com.google.gson.Gson;
import dao.UserDAO;
import dao.DBConnection;
import entity.User;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;


// сервлет авторизации с поддержкой сессий

public class AuthService {

    private UserDAO userDAO;
    private Gson gson;
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    public AuthService() {
        gson = new Gson();
        try {
            Connection conn = DBConnection.getConnection();
            userDAO = new UserDAO(conn);
            createTestUsers();

            logger.info("AuthService успешно инициализирован с подключением к БД");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации AuthService: " + e.getMessage());
        }
    }

    private void createTestUsers() {
        try {
            if (userDAO.findByLogin("admin").isEmpty()) {
                User admin = new User();
                admin.setLogin("admin");
                admin.setPassword("admin123");
                admin.setRole("admin");
                admin.setFullName("Администратор");
                admin.setEmail("admin@example.com");
                userDAO.create(admin);
                logger.info("Создан тестовый администратор: admin/admin123");
            }

            if (userDAO.findByLogin("user").isEmpty()) {
                User user = new User();
                user.setLogin("user");
                user.setPassword("user123");
                user.setRole("user");
                user.setFullName("Обычный пользователь");
                user.setEmail("user@example.com");
                userDAO.create(user);
                logger.info("Создан тестовый пользователь: user/user123");
            }

        } catch (Exception e) {
            logger.warning("Ошибка при создании тестовых пользователей: " + e.getMessage());
        }
    }

    public String register(String login, String password, String fullName, String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDAO.findByLogin(login).isPresent()) {
                response.put("success", false);
                response.put("message", "Пользователь с таким логином уже существует");
                return gson.toJson(response);
            }

            User user = new User();
            user.setLogin(login);
            user.setPassword(password);
            user.setRole("user");
            user.setFullName(fullName);
            user.setEmail(email);

            userDAO.create(user);

            response.put("success", true);
            response.put("message", "Регистрация успешна");

            logger.info("Зарегистрирован новый пользователь: " + login);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Ошибка при регистрации: " + e.getMessage());
        }

        return gson.toJson(response);
    }

    public String login(String login, String password, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOpt = userDAO.findByLogin(login);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (user.getPassword().equals(password)) {
                    // СОХРАНЕНИЕ СЕССИИ
                    session.setAttribute("user", user);
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("userRole", user.getRole());
                    session.setMaxInactiveInterval(30 * 60);

                    response.put("success", true);
                    response.put("message", "Авторизация успешна");
                    response.put("user", Map.of(
                            "id", user.getId(),
                            "login", user.getLogin(),
                            "role", user.getRole(),
                            "fullName", user.getFullName(),
                            "email", user.getEmail()
                    ));

                    logger.info("Пользователь авторизован: " + login + ", роль: " + user.getRole());

                } else {
                    response.put("success", false);
                    response.put("message", "Неверный пароль");
                    logger.warning("Неудачная попытка входа для пользователя: " + login);
                }
            } else {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Ошибка сервера при авторизации");
        }

        return gson.toJson(response);
    }

    public String logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (session != null) {
                String login = "unknown";
                if (session.getAttribute("user") != null) {
                    login = ((User) session.getAttribute("user")).getLogin();
                }
                // Уничтожаем сессию
                session.invalidate();
                response.put("success", true);
                response.put("message", "Выход выполнен");
                logger.info("Пользователь вышел: " + login);
            } else {
                response.put("success", false);
                response.put("message", "Сессия не найдена");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Ошибка при выходе");
        }

        return gson.toJson(response);
    }

    public String checkAuth(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (session != null && session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");

                // ОБНОВЛЕНИЕ ДАННЫХ: Получаем актуальные данные из БД
                Optional<User> currentUser = userDAO.findById(user.getId());
                if (currentUser.isPresent()) {
                    user = currentUser.get();
                    session.setAttribute("user", user); // Обновляем сессию актуальными данными

                    response.put("authenticated", true);
                    response.put("user", Map.of(
                            "id", user.getId(),
                            "login", user.getLogin(),
                            "role", user.getRole(),
                            "fullName", user.getFullName(),
                            "email", user.getEmail()
                    ));
                } else {
                    session.invalidate();
                    response.put("authenticated", false);
                }
            } else {
                response.put("authenticated", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("authenticated", false);
        }

        return gson.toJson(response);
    }
}