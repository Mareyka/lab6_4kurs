package dao;

import entity.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {
    private final Connection conn;

    public ClientDAO(Connection conn) {
        this.conn = conn;
    }

    public void create(Client client) {
        String sql = "INSERT INTO Clients (full_name, contacts) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getContacts());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    client.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Client read(int id) {
        String sql = "SELECT * FROM Clients WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return new Client(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("contacts")
                );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(Client client) {
        String sql = "UPDATE Clients SET full_name=?, contacts=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getContacts());
            ps.setInt(3, client.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Clients WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Client> getAll() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM Clients";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                clients.add(new Client(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("contacts")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }
}
