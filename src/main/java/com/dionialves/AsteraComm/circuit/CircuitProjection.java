package com.dionialves.AsteraComm.circuit;

public interface CircuitProjection {
    String getId();
    String getPassword();
    String getTrunkName();
    String getCustomerName();
    String getPlanName();
    String getIp();
    String getRtt();
    boolean isOnline();
}
