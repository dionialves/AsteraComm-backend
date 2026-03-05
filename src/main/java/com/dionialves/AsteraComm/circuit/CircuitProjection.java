package com.dionialves.AsteraComm.circuit;

public interface CircuitProjection {
    String getId();
    String getPassword();
    String getIp();
    String getRtt();
    boolean isOnline();
}
