package com.dionialves.AsteraComm.dto;

import java.util.Objects;

public class EndPointDTO {
    private String id;
    private String callerid;
    private String username;
    private String password;

    public EndPointDTO() {
    }

    public EndPointDTO(String id, String callerid, String username, String password) {
        this.id = id;
        this.callerid = callerid;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCallerid() {
        return callerid;
    }

    public void setCallerid(String callerid) {
        this.callerid = callerid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EndPointDTO that = (EndPointDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(callerid, that.callerid) && Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, callerid, username, password);
    }
}
