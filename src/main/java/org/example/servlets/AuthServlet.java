package org.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * КЛАСС: AuthServlet (Сервлет аутентификации)
 * НАЗНАЧЕНИЕ: HTTP endpoint для обработки запросов авторизации, регистрации и управления сессиями
 * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 5 - создание сервлета регистрации-авторизации
 * АННОТАЦИЯ: @WebServlet("/auth/*") - маппинг URL с поддержкой path parameters
 */
@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
    // Сервисный слой - содержит бизнес-логику аутентификации
    private AuthService authService;

    /**
     * МЕТОД: init - инициализация сервлета
     * ВЫЗЫВАЕТСЯ: Контейнером сервлетов при первом обращении к сервлету
     * ЦЕЛЬ: Инициализация зависимостей (AuthService)
     */
    @Override
    public void init() throws ServletException {
        // Создаем экземпляр AuthService который сам инициализирует подключение к БД
        authService = new AuthService();
    }

    /**
     * МЕТОД: doPost - обработка POST запросов
     * ОБРАБАТЫВАЕТ: Регистрацию и авторизацию (требуют передачи данных в теле)
     * СООТВЕТСТВИЕ ЛАБОРАТОРНОЙ: Пункт 5 - логин/пароль передаются в теле запроса
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Получаем часть URL после "/auth/" - определяет конкретную операцию
        String pathInfo = req.getPathInfo();
        String json; // Переменная для JSON ответа

        // Обработка регистрации нового пользователя
        if (pathInfo != null && pathInfo.equals("/register")) {
            /**
             * РЕГИСТРАЦИЯ: POST /auth/register
             * ПАРАМЕТРЫ: Извлекаются из тела запроса (x-www-form-urlencoded)
             * СООТВЕТСТВИЕ: Пункт 5 - передача в теле для поддержки HTTPS
             */
            String login = req.getParameter("login");
            String password = req.getParameter("password");
            String fullName = req.getParameter("fullName");
            String email = req.getParameter("email");

            // Делегируем бизнес-логику сервисному слою
            json = authService.register(login, password, fullName, email);

        }
        // Обработка авторизации пользователя
        else if (pathInfo != null && pathInfo.equals("/login")) {
            /**
             * АВТОРИЗАЦИЯ: POST /auth/login
             * ПАРАМЕТРЫ: Логин и пароль из тела запроса
             * СЕССИЯ: Создается новая сессия для аутентифицированного пользователя
             */
            String login = req.getParameter("login");
            String password = req.getParameter("password");

            // Создаем или получаем существующую сессию
            // req.getSession() с параметром true создает сессию если ее нет
            HttpSession session = req.getSession();

            // Передаем сессию в сервис для установки атрибутов аутентификации
            json = authService.login(login, password, session);

        }
        // Если путь не распознан - возвращаем 404
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Отправляем успешный JSON ответ
        outputResponse(resp, json, HttpServletResponse.SC_OK);
    }

    /**
     * МЕТОД: doGet - обработка GET запросов
     * ОБРАБАТЫВАЕТ: Проверку авторизации и выход (не требуют передачи данных в теле)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String json;

        // Проверка текущего статуса аутентификации
        if (pathInfo != null && pathInfo.equals("/check")) {
            /**
             * ПРОВЕРКА АВТОРИЗАЦИИ: GET /auth/check
             * СЕССИЯ: req.getSession(false) - не создает новую сессию если ее нет
             * ВОЗВРАЩАЕТ: { "authenticated": true/false, "user": {...} }
             */
            HttpSession session = req.getSession(false); // false = не создавать новую сессию
            json = authService.checkAuth(session);

        }
        // Завершение сессии пользователя
        else if (pathInfo != null && pathInfo.equals("/logout")) {
            /**
             * ВЫХОД: GET /auth/logout
             * СЕССИЯ: Инвалидируется (уничтожается)
             * COOKIE: JSESSIONID становится невалидным
             */
            HttpSession session = req.getSession(false); // Берем существующую сессию если есть
            json = authService.logout(session);

        }
        // Если путь не распознан - возвращаем 404
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        outputResponse(resp, json, HttpServletResponse.SC_OK);
    }

    /**
     * ВСПОМОГАТЕЛЬНЫЙ МЕТОД: outputResponse - формирование HTTP ответа
     * УНИФИКАЦИЯ: Все ответы имеют одинаковый формат (JSON)
     * СТАНДАРТИЗАЦИЯ: Единый Content-Type и кодировка
     */
    private void outputResponse(HttpServletResponse resp, String payload, int status) throws IOException {
        resp.setStatus(status);                     // Устанавливаем HTTP статус
        resp.setContentType("application/json");    // Указываем тип содержимого
        resp.getWriter().write(payload);            // Записываем JSON в тело ответа
    }
}