package org.javamaster.b2c.core.entity;

public class MicrowebsiteExamUser {
    private String examCode;

    private String username;

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode == null ? null : examCode.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }
}