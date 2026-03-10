package com.dionialves.AsteraComm.circuit;

public interface CircuitProjection {
    String getId();
    String getPassword();
    String getTrunkName();
    String getIp();
    String getRtt();
    boolean isOnline();
}
