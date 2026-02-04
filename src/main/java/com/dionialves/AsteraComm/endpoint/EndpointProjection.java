package com.dionialves.AsteraComm.endpoint;

public interface EndpointProjection {
    String getId();

    String getCallerid();

    String getUsername();

    String getPassword();

    String getIp();

    String getRtt();

    Boolean getOnline();
}
