package org.example.servlets;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentService {

    private ConcurrentMap<Integer, Student> students;
    private AtomicInteger key;

    public StudentService() {
        this.students = new ConcurrentHashMap<>();
        key = new AtomicInteger();
        this.addStudent(new Student(null, "Ivan", 10, "M"));
        this.addStudent(new Student(null, "Maria", 10, "A"));
        this.addStudent(new Student(null, "Sergey", 10, "K"));
    }

    public String findAllStudent() {
        List<Student> list = new ArrayList<>(this.students.values());
        return this.toJson(list);
    }

    public boolean createStudent(String jsonPayload) {
        if (jsonPayload == null) return false;
        Gson gson = new Gson();
        try {
            Student student = gson.fromJson(jsonPayload, Student.class);
            if (student != null) {
                return this.addStudent(student);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String toJson(Object list) {
        if (list == null) return null;
        Gson gson = new Gson();
        String json = null;
        try {
            json = gson.toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private boolean addStudent(Student student) {
        Integer id = key.incrementAndGet();
        student.setId(id);
        this.students.put(id, student);
        return true;
    }
}
