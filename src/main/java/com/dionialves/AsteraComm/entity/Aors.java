package com.dionialves.AsteraComm.entity;

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
@Table(name = "ps_aors")
class Aors {

    @Id
    @Column(name = "id")
    private String id;

    @Column(columnDefinition = "TEXT")
    private String contact;

    @Column(name = "default_expiration")
    private int defaultExpiration;

    @Column(name = "max_contacts")
    private int maxContacts;

    @Column(name = "minimum_expiration")
    private int minimumExpiration;

    @Column(name = "remove_existing")
    private String removeExisting;

    @Column(name = "qualify_frequency")
    private int qualifyFrequency;
}
