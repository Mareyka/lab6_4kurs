package org.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;

@WebServlet("/clients/*")
public class ClientServlet extends HttpServlet {

    private ClientService clientService;

    public ClientServlet() {
        this.clientService = new ClientService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String json = this.clientService.getAllClients();
        outputResponse(resp, json, HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        boolean res = clientService.createClient(body);
        outputResponse(resp, null, res ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        boolean res = clientService.updateClient(body);
        outputResponse(resp, null, res ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path != null && path.length() > 1) {
            try {
                int id = Integer.parseInt(path.substring(1));
                boolean res = clientService.deleteClient(id);
                outputResponse(resp, null, res ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
            } catch (NumberFormatException e) {
                outputResponse(resp, null, HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            outputResponse(resp, null, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void outputResponse(HttpServletResponse resp, String payload, int status) {
        try {
            resp.setStatus(status);
            resp.setContentType("application/json");
            if (payload != null) {
                OutputStream os = resp.getOutputStream();
                os.write(payload.getBytes());
                os.flush();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
