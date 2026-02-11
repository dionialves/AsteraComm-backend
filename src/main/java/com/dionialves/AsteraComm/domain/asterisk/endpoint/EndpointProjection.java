package com.dionialves.AsteraComm.domain.asterisk.endpoint;

public interface EndpointProjection {
    String getId();

    String getCallerid();

    String getUsername();

    String getPassword();

    String getIp();

    String getRtt();

    Boolean getOnline();
}
