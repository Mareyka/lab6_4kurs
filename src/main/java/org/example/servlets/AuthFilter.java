package org.example.servlets;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * КЛАСС: AuthFilter (Фильтр аутентификации)
 * НАЗНАЧЕНИЕ: Перехватывает ВСЕ HTTP запросы для проверки авторизации и логирования
 * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6 - фильтр для проверки прав доступа и логгирования
 * АННОТАЦИЯ: @WebFilter("/*") - применяется ко всем URL приложения
 */
@WebFilter("/*")
public class AuthFilter implements Filter {
    // Логгер для записи действий пользователей (требование лабораторной)
    private static final Logger logger = Logger.getLogger(AuthFilter.class.getName());

    /**
     * МЕТОД: doFilter - основной метод фильтра, вызывается для КАЖДОГО запроса
     * ЦЕПОЧКА: Request → AuthFilter → Следующий фильтр/Сервлет → Response
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Приведение общих Servlet типов к HTTP-specific типам
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Получаем сессию БЕЗ создания новой (false параметр)
        HttpSession session = httpRequest.getSession(false);

        // Извлекаем путь запроса (без контекста приложения)
        // Пример: http://localhost:8080/lab6_4kurs/clients → "/clients"
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String method = httpRequest.getMethod(); // GET, POST, PUT, DELETE

        // ЛОГГИРОВАНИЕ ДЕЙСТВИЙ - соответствует требованию лабораторной
        String userInfo = "Неавторизованный пользователь";
        if (session != null && session.getAttribute("user") != null) {
            // Если пользователь авторизован - извлекаем его данные из сессии
            entity.User user = (entity.User) session.getAttribute("user");
            userInfo = "Пользователь: " + user.getLogin();
        }

        // Записываем лог в формате: "Действие: GET /clients | Пользователь: admin"
        logger.info(String.format("Действие: %s %s | %s", method, path, userInfo));

        // ПРОВЕРКА ДОСТУПА - основная бизнес-логика фильтра
        if (isProtectedPath(path, method)) {
            /**
             * СЦЕНАРИЙ 1: Неавторизованный пользователь
             * УСЛОВИЕ: Нет сессии или нет атрибута 'user' в сессии
             */
            if (session == null || session.getAttribute("user") == null) {
                // Неавторизованные - только GET запросы
                if (!"GET".equals(method)) {
                    /**
                     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 6
                     * "Неавторизованному пользователю разрешить выполнение только GET-запросов"
                     */
                    httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Требуется авторизация");
                    return; // ПРЕРЫВАЕМ цепочку фильтров - запрос не проходит дальше
                }
            }
            /**
             * СЦЕНАРИЙ 2: Авторизованный пользователь
             * УСЛОВИЕ: Есть сессия и атрибут 'user'
             * ДЕЙСТВИЕ: Разрешаем ВСЕ виды запросов (GET, POST, PUT, DELETE)
             * СООТВЕТСТВИЕ: "Авторизованному – всех видов запросов"
             */
            // Авторизованные - ВСЕ виды запросов (без дополнительных ограничений)
        }

        // Пропускаем запрос дальше по цепочке фильтров/сервлетов
        chain.doFilter(request, response);
    }

    /**
     * МЕТОД: isProtectedPath - определяет требует ли путь защиты
     * ЛОГИКА: Защищаем все операции с клиентами кроме GET /clients
     */
    private boolean isProtectedPath(String path, String method) {
        // Защищаем все операции с клиентами кроме GET без ID
        if (path.startsWith("/clients")) {
            /**
             * ИСКЛЮЧЕНИЕ: GET /clients доступен всем (включая неавторизованных)
             * ЦЕЛЬ: Позволить просматривать список клиентов без авторизации
             */
            if (path.equals("/clients") && "GET".equals(method)) {
                return false; // GET /clients доступен всем
            }
            return true; // Все остальные операции с клиентами защищены
        }
        return false; // Все остальные пути не защищены
    }

    // Методы жизненного цикла фильтра (не используются в данной реализации)
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}