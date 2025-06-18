package com.dionialves.AsteraComm.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "asteracomm_client")
public class Client {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int numberContractIxcSoft;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "client")
    private List<Circuit> circuits = new ArrayList<>();

    public Client(String name, int numberContractIxcSoft, LocalDateTime createdAt,
            LocalDateTime updateAt, List<Circuit> circuits) {
        this.name = name;
        this.numberContractIxcSoft = numberContractIxcSoft;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.circuits = circuits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberContractIxcSoft() {
        return numberContractIxcSoft;
    }

    public void setNumberContractIxcSoft(int numberContractIxcSoft) {
        this.numberContractIxcSoft = numberContractIxcSoft;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Circuit> getCircuits() {
        return circuits;
    }

    public void setCircuits(List<Circuit> circuits) {
        this.circuits = circuits;
    }
}
