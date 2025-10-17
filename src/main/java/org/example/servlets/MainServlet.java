package org.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class MainServlet extends HttpServlet {

    private StudentService studentService;

    public MainServlet() {
        this.studentService = new StudentService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String json = this.studentService.findAllStudent();
        outputResponse(resp, json, 200);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String reqBody = req.getReader()
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));

        int rc = HttpServletResponse.SC_OK;
        boolean res = this.studentService.createStudent(reqBody);

        if (!res) {
            rc = HttpServletResponse.SC_BAD_REQUEST;
        }

        this.outputResponse(resp, null, rc);
    }

    private void outputResponse(HttpServletResponse response, String payload, int status) {
       // response.setHeader("Content-Type", "application/json");

        try {
            response.setStatus(status);

            if (payload != null) {
                OutputStream outputStream = response.getOutputStream();
                outputStream.write(payload.getBytes());
                outputStream.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
