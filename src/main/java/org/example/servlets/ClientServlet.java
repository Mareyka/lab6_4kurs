package org.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/clients/*")
public class ClientServlet extends HttpServlet {
    private ClientService clientService;

    @Override
    public void init() throws ServletException {
        clientService = new ClientService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String json;

        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                String idStr = pathInfo.substring(1);
                int id = Integer.parseInt(idStr);
                json = clientService.getClientById(id);

                if (json == null || json.isEmpty() || "null".equals(json)) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Client with id " + id + " not found");
                    return;
                }

            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id format");
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

        boolean res = clientService.createClient(fullName, contacts);
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
            boolean res = clientService.updateClient(id, fullName, contacts);
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
            if (payload != null)
                out.print(payload);
        }
    }
}