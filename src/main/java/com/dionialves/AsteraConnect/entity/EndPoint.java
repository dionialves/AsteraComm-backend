package com.dionialves.AsteraConnect.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "ps_endpoints")
public class EndPoint {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "auth", referencedColumnName = "id")
    private Auth auth;

    private String callerid;

    public EndPoint() {}

    public EndPoint(String id, Auth auth, String callerid) {
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
        EndPoint endpoint = (EndPoint) o;
        return Objects.equals(id, endpoint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}