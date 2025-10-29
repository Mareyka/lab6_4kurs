package dao;

import entity.User;
import java.sql.*;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * КЛАСС: UserDAO (Data Access Object)
 * НАЗНАЧЕНИЕ: Обеспечивает взаимодействие с таблицей Users в базе данных
 * Data Access Object (DAO) - отделяет бизнес-логику от операций с БД
 */
public class UserDAO {
    // Подключение к базе данных - инжектируется через конструктор
    private final Connection conn;

    // Логгер для записи событий и ошибок (соответствует требованию лабораторной)
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    /**
     * КОНСТРУКТОР: принимает готовое подключение к БД
     * ПРИНЦИП: Dependency Injection - зависимость передается извне
     */
    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * МЕТОД: create - создание нового пользователя в БД
     * СООТВЕТСТВИЕ ЛАБЕ: Создание записей в таблице Users для регистрации
     */
    public void create(User user) throws SQLException {
        // SQL запрос с параметрами для защиты от SQL-инъекций
        String sql = "INSERT INTO Users (login, password, role, full_name, email) VALUES (?, ?, ?, ?, ?)";

        // try-with-resources автоматически закрывает PreparedStatement
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Установка параметров запроса (индексы начинаются с 1)
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());

            // Выполнение запроса и проверка результата
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                // Если ни одна запись не добавлена - исключение
                throw new SQLException("Создание пользователя не удалось, ни одна запись не добавлена.");
            }

            // Получение сгенерированного ID новой записи
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));  // Устанавливаем ID в объект User
                    // Логирование успешного создания (требование лабораторной)
                    logger.info("Создан пользователь с ID: " + user.getId());
                }
            }
        } catch (SQLException e) {
            // Логирование ошибок (требование лабораторной)
            logger.severe("Ошибка при создании пользователя: " + e.getMessage());
            throw e;  // Пробрасываем исключение дальше для обработки на уровне сервиса
        }
    }

    /**
     * МЕТОД: findByLogin - поиск пользователя по логину
     * ИСПОЛЬЗУЕТСЯ: При авторизации и проверке уникальности логина при регистрации
     * ВОЗВРАЩАЕТ: Optional<User> - современный подход для работы с возможным отсутствием результата
     */
    public Optional<User> findByLogin(String login) {
        String sql = "SELECT * FROM Users WHERE login = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);  // Установка параметра логина
            ResultSet rs = ps.executeQuery();

            // Если пользователь найден - создаем объект User
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("full_name"),
                        rs.getString("email")
                );
                return Optional.of(user);  // Возвращаем пользователя в Optional
            }
        } catch (SQLException e) {
            // Логирование ошибок при поиске
            logger.severe("Ошибка при поиске пользователя по логину: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();  // Пользователь не найден - пустой Optional
    }

    /**
     * МЕТОД: findById - поиск пользователя по ID
     * ИСПОЛЬЗУЕТСЯ: Для обновления данных сессии, проверки существования пользователя
     * ID: Уникальный идентификатор из БД (AUTO_INCREMENT)
     */
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);  // Установка параметра ID
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("full_name"),
                        rs.getString("email")
                );
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске пользователя по ID: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
}