package dao;

import entity.User;
import java.sql.*;
import java.util.Optional;
import java.util.logging.Logger;

public class UserDAO {
    private final Connection conn;
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public void create(User user) throws SQLException {
        String sql = "INSERT INTO Users (login, password, role, full_name, email) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось, ни одна запись не добавлена.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                    logger.info("Создан пользователь с ID: " + user.getId());
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при создании пользователя: " + e.getMessage());
            throw e;
        }
    }

    public Optional<User> findByLogin(String login) {
        String sql = "SELECT * FROM Users WHERE login = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
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
            logger.severe("Ошибка при поиске пользователя по логину: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
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