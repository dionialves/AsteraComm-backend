package com.dionialves.AsteraComm.entity;

import org.springframework.data.aot.PublicMethodReflectiveProcessor;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class DID {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "circuit_id")
    private Circuit circuit;

    private String number;

    public DID(Long id, Circuit circuit, ) {

    }
}
