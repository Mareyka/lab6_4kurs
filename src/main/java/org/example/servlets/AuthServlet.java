package org.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;


@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String json;

        if (pathInfo != null && pathInfo.equals("/register")) {

            // параметры Извлекаются из тела запроса (x-www-form-urlencoded)
            // передача в теле для поддержки HTTPS
            String login = req.getParameter("login");
            String password = req.getParameter("password");
            String fullName = req.getParameter("fullName");
            String email = req.getParameter("email");

            json = authService.register(login, password, fullName, email);

        }
        else if (pathInfo != null && pathInfo.equals("/login")) {

            String login = req.getParameter("login");
            String password = req.getParameter("password");

            // Создаем или получаем существующую сессию
            // req.getSession() с параметром true создает сессию если ее нет
            HttpSession session = req.getSession();

            json = authService.login(login, password, session);

        }
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        outputResponse(resp, json, HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String json;

        if (pathInfo != null && pathInfo.equals("/check")) {

            HttpSession session = req.getSession(false); // false = не создавать новую сессию
            json = authService.checkAuth(session);

        }
        else if (pathInfo != null && pathInfo.equals("/logout")) {

            HttpSession session = req.getSession(false);
            json = authService.logout(session);

        }
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        outputResponse(resp, json, HttpServletResponse.SC_OK);
    }

    private void outputResponse(HttpServletResponse resp, String payload, int status) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write(payload);
    }
}