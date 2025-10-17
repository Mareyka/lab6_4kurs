package org.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.servlets.ClientService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/clients")
public class ClientServlet extends HttpServlet {

    private ClientService clientService;

    @Override
    public void init() throws ServletException {
        clientService = new ClientService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        String json;
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                json = clientService.getClientById(id); // метод нужно добавить
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
                return;
            }
        } else {
            json = clientService.getAllClients();
        }
        outputResponse(resp, json, HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String contacts = req.getParameter("contacts");
        boolean res = clientService.createClient(fullName, contacts); // метод нужно добавить
        resp.setStatus(res ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        String fullName = req.getParameter("fullName");
        String contacts = req.getParameter("contacts");

        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            boolean res = clientService.updateClient(id, fullName, contacts); // метод нужно добавить
            resp.setStatus(res ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            boolean res = clientService.deleteClient(id);
            resp.setStatus(res ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
        }
    }

    private void outputResponse(HttpServletResponse resp, String payload, int status) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            if (payload != null) out.print(payload);
        }
    }
}
