package org.example.servlets;

import com.google.gson.Gson;
import dao.ClientDAO;
import dao.DBConnection;
import entity.Client;

import java.sql.Connection;
import java.util.List;

public class ClientService {
    private ClientDAO clientDAO;
    private Gson gson;

    public ClientService() {
        gson = new Gson();
        try {
            Connection conn = DBConnection.getConnection();
            clientDAO = new ClientDAO(conn);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации ClientService: " + e.getMessage());
        }
    }

    public String getAllClients() {
        List<Client> clients = clientDAO.getAll();
        return gson.toJson(clients);
    }

    public boolean createClient(String jsonPayload) {
        try {
            Client client = gson.fromJson(jsonPayload, Client.class);
            if (client != null) {
                clientDAO.create(client);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateClient(String jsonPayload) {
        try {
            Client client = gson.fromJson(jsonPayload, Client.class);
            if (client != null && client.getId() > 0) {
                clientDAO.update(client);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteClient(int id) {
        try {
            clientDAO.delete(id);
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}
