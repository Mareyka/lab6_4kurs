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

/**
 * КЛАСС: AuthService (Сервис аутентификации)
 * НАЗНАЧЕНИЕ: Центральный сервис для управления авторизацией, регистрацией и сессиями пользователей
 * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Реализует пункты 5 и 6 - сервлет авторизации с поддержкой сессий
 */
public class AuthService {
    // DAO для работы с пользователями в БД
    private UserDAO userDAO;

    // Gson для преобразования Java объектов в JSON и обратно
    private Gson gson;

    // Логгер для записи событий аутентификации (требование лабораторной)
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    /**
     * КОНСТРУКТОР: инициализация сервиса
     * ВЫПОЛНЯЕТ: Подключение к БД, создание тестовых пользователей, настройку компонентов
     */
    public AuthService() {
        gson = new Gson();
        try {
            // Получаем соединение с БД через DBConnection
            Connection conn = DBConnection.getConnection();

            // Создаем DAO для работы с пользователями
            userDAO = new UserDAO(conn);

            // Создаем тестовых пользователей при первом запуске
            createTestUsers();

            // Логируем успешную инициализацию
            logger.info("AuthService успешно инициализирован с подключением к БД");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации AuthService: " + e.getMessage());
        }
    }

    /**
     * МЕТОД: createTestUsers - создание тестовых пользователей
     * ЦЕЛЬ: Обеспечить наличие пользователей для тестирования системы
     * ЛОГИКА: Создает пользователей только если они не существуют
     */
    private void createTestUsers() {
        try {
            // Создаем администратора, если не существует
            if (userDAO.findByLogin("admin").isEmpty()) {
                User admin = new User();
                admin.setLogin("admin");
                admin.setPassword("admin123");
                admin.setRole("admin");         // Роль администратора
                admin.setFullName("Администратор");
                admin.setEmail("admin@example.com");
                userDAO.create(admin);
                logger.info("Создан тестовый администратор: admin/admin123");
            }

            // Создаем обычного пользователя, если не существует
            if (userDAO.findByLogin("user").isEmpty()) {
                User user = new User();
                user.setLogin("user");
                user.setPassword("user123");
                user.setRole("user");           // Роль обычного пользователя
                user.setFullName("Обычный пользователь");
                user.setEmail("user@example.com");
                userDAO.create(user);
                logger.info("Создан тестовый пользователь: user/user123");
            }

        } catch (Exception e) {
            logger.warning("Ошибка при создании тестовых пользователей: " + e.getMessage());
        }
    }

    /**
     * МЕТОД: register - регистрация нового пользователя
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 5 - передача логина/пароля в теле запроса
     * ВОЗВРАЩАЕТ: JSON ответ с результатом операции
     */
    public String register(String login, String password, String fullName, String email) {
        // Используем Map для построения структурированного JSON ответа
        Map<String, Object> response = new HashMap<>();

        try {
            // Проверяем, существует ли пользователь с таким логином
            if (userDAO.findByLogin(login).isPresent()) {
                response.put("success", false);
                response.put("message", "Пользователь с таким логином уже существует");
                return gson.toJson(response);
            }

            // Создаем нового пользователя
            User user = new User();
            user.setLogin(login);
            user.setPassword(password);
            user.setRole("user");       // Все новые пользователи получают роль 'user'
            user.setFullName(fullName);
            user.setEmail(email);

            userDAO.create(user);

            // Формируем успешный ответ
            response.put("success", true);
            response.put("message", "Регистрация успешна");

            // Логируем регистрацию (требование лабораторной)
            logger.info("Зарегистрирован новый пользователь: " + login);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Ошибка при регистрации: " + e.getMessage());
        }

        // Преобразуем Map в JSON строку
        return gson.toJson(response);
    }

    /**
     * МЕТОД: login - аутентификация пользователя
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6 - поддержка сессий через HttpSession
     * ВОЗВРАЩАЕТ: JSON с результатом авторизации и данными пользователя
     */
    public String login(String login, String password, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Ищем пользователя по логину
            Optional<User> userOpt = userDAO.findByLogin(login);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Проверяем пароль
                if (user.getPassword().equals(password)) {
                    // СОХРАНЕНИЕ СЕССИИ - ключевая часть лабораторной работы
                    session.setAttribute("user", user);        // Полный объект пользователя
                    session.setAttribute("userId", user.getId()); // ID для быстрого доступа
                    session.setAttribute("userRole", user.getRole()); // Роль для проверки прав
                    session.setMaxInactiveInterval(30 * 60);   // Время жизни сессии - 30 минут

                    // Формируем успешный ответ
                    response.put("success", true);
                    response.put("message", "Авторизация успешна");
                    response.put("user", Map.of(              // Возвращаем данные пользователя
                            "id", user.getId(),
                            "login", user.getLogin(),
                            "role", user.getRole(),
                            "fullName", user.getFullName(),
                            "email", user.getEmail()
                    ));

                    // Логируем успешную авторизацию (требование лабораторной)
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

    /**
     * МЕТОД: logout - завершение сессии пользователя
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Управление сессиями
     * ВОЗВРАЩАЕТ: JSON с результатом выхода
     */
    public String logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (session != null) {
                String login = "unknown";
                if (session.getAttribute("user") != null) {
                    login = ((User) session.getAttribute("user")).getLogin();
                }
                // Уничтожаем сессию - все атрибуты очищаются
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

    /**
     * МЕТОД: checkAuth - проверка текущей аутентификации
     * ИСПОЛЬЗУЕТСЯ: Для проверки активной сессии на клиенте
     * ВОЗВРАЩАЕТ: JSON со статусом аутентификации и данными пользователя
     */
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
                    // Пользователь удален из БД - уничтожаем сессию
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