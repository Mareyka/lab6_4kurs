package entity;

public class User {
    private int id;
    private String login;
    private String password;
    private String role;
    private String fullName;
    private String email;

    public User() {}

    public User(int id, String login, String password, String role, String fullName, String email) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}