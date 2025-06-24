package com.dionialves.AsteraComm.domain.endpoint.dto;

import java.util.Objects;

public class EndpointDTO {
    private String id;
    private String callerid;
    private String username;
    private String password;
    private String ip;
    private String rtt;
    private Boolean online;

    public EndpointDTO() {
    }

    public EndpointDTO(String id, String callerid, String username, String password) {
        this.id = id;
        this.callerid = callerid;
        this.username = username;
        this.password = password;
    }

    public EndpointDTO(String id, String callerid, String username, String password, String ip, String rtt, Boolean online) {
        this.id = id;
        this.callerid = callerid;
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.rtt = rtt;
        this.online = online;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRtt() {
        return rtt;
    }

    public void setRtt(String rtt) {
        this.rtt = rtt;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EndpointDTO that = (EndpointDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(callerid, that.callerid) && Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, callerid, username, password);
    }
}
