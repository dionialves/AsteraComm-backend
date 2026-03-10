package com.dionialves.AsteraComm.circuit;

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
@Table(name = "asteracomm_circuits")
public class Circuit {

    @Id
    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String password;

    @Column(name = "trunk_name", nullable = false)
    private String trunkName;
}
