package org.example.servlets;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

@WebFilter("/*")
public class AuthFilter implements Filter {
    private static final Logger logger = Logger.getLogger(AuthFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Приведение общих Servlet типов к HTTP-specific типам
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Получаем сессию БЕЗ создания новой (false параметр)
        HttpSession session = httpRequest.getSession(false);

        // Извлекаем путь запроса
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String method = httpRequest.getMethod();

        String userInfo = "Неавторизованный пользователь";
        if (session != null && session.getAttribute("user") != null) {
            // Если пользователь авторизован - извлекаем его данные из сессии
            entity.User user = (entity.User) session.getAttribute("user");
            userInfo = "Пользователь: " + user.getLogin();
        }

        logger.info(String.format("Действие: %s %s | %s", method, path, userInfo));

        // ПРОВЕРКА ДОСТУПА
        if (isProtectedPath(path, method)) {

            // Нет сессии или нет атрибута user в сессии
            if (session == null || session.getAttribute("user") == null) {
                if (!"GET".equals(method)) {
                    httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Требуется авторизация");
                    return;
                }
            }
            // Авторизованные - ВСЕ виды запросов (без дополнительных ограничений)
        }
        chain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path, String method) {
        if (path.startsWith("/clients")) {

            if (path.equals("/clients") && "GET".equals(method)) {
                return false; // GET /clients доступен всем
            }
            return true;
        }
        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}