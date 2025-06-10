package com.dionialves.AsteraComm.projection;

public interface EndpointProjection {
    Long getId();

    String getCallerid();

    String getUsername();

    String getPassword();

    String getIp();

    Double getRtt();

    Boolean getOnline();
}
