package com.dionialves.AsteraComm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "asteracomm_endpoint_status_history")
public class EndpointStatus {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "endpoint", referencedColumnName = "id")
    private Endpoint endpoint;

    private boolean online;

    private String ip;
    private String rtt;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    public EndpointStatus() {
    }

    public EndpointStatus(Endpoint endpoint, boolean online, String ip, String rtt) {
        this.endpoint = endpoint;
        this.online = online;
        this.ip = ip;
        this.rtt = rtt;
    }

    public EndpointStatus(Integer id, Endpoint endpoint, boolean online, String ip, String rtt,
            LocalDateTime checkedAt) {
        this.id = id;
        this.endpoint = endpoint;
        this.online = online;
        this.ip = ip;
        this.rtt = rtt;
        this.checkedAt = checkedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
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

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        EndpointStatus that = (EndpointStatus) o;
        return online == that.online && Objects.equals(id, that.id) && Objects.equals(ip, that.ip)
                && Objects.equals(rtt, that.rtt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, online, ip, rtt);
    }
}
