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

    // Получить всех клиентов
    public String getAllClients() {
        List<Client> clients = clientDAO.getAll();
        return gson.toJson(clients);
    }

    // Получить одного клиента по id
    public String getClientById(int id) {
        Client client = clientDAO.read(id);
        return gson.toJson(client);
    }

    // Создать клиента по параметрам
    public boolean createClient(String fullName, String contacts) {
        try {
            Client client = new Client();
            client.setFullName(fullName);
            client.setContacts(contacts);
            clientDAO.create(client);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Обновить клиента по параметрам
    public boolean updateClient(int id, String fullName, String contacts) {
        try {
            Client client = clientDAO.read(id);
            if (client == null) return false;
            client.setFullName(fullName);
            client.setContacts(contacts);
            clientDAO.update(client);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Удалить клиента по id
    public boolean deleteClient(int id) {
        try {
            clientDAO.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
