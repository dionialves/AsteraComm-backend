package com.dionialves.AsteraComm.entity;

import java.util.Objects;

public class EndpointStatus {
    private boolean online;
    private String ip;
    private String rtt;

    public EndpointStatus() {
    }

    public EndpointStatus(boolean online, String ip, String rtt) {
        this.online = online;
        this.ip = ip;
        this.rtt = rtt;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EndpointStatus that = (EndpointStatus) o;
        return online == that.online && Objects.equals(ip, that.ip) && Objects.equals(rtt, that.rtt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(online, ip, rtt);
    }
}
