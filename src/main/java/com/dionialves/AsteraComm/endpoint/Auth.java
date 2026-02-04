package com.dionialves.AsteraComm.endpoint;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ps_auths")
public class Auth {

    @Id
    @Column(name = "id")
    private String id;

    private String userpass;
    private String username;
    private String password;

}
