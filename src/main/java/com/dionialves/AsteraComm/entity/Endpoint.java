package com.dionialves.AsteraComm.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "ps_endpoints")
public class Endpoint {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "auth", referencedColumnName = "id")
    private Auth auth;

    private String callerid;

    public Endpoint() {}

    public Endpoint(String id, Auth auth, String callerid) {
        this.id = id;
        this.auth = auth;
        this.callerid = callerid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public String getCallerid() {
        return callerid;
    }

    public void setCallerid(String callerid) {
        this.callerid = callerid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(id, endpoint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}