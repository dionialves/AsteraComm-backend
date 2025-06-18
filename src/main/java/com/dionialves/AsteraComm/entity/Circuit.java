package com.dionialves.AsteraComm.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

public class Circuit {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "circuit")
    private List<DID> DIDs = new ArrayList<>();

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "endpoint_id", unique = true)
    private Endpoint endpoint;

    private int billingDay;
    private String username;
    private String password;
}
