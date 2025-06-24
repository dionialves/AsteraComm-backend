package com.dionialves.AsteraComm.domain.endpoint.entity;

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

    @Column(name = "default_expiration")
    private Integer defaultExpiration;

    @Column(name = "max_contacts")
    private Integer maxContacts;

    @Column(name = "remove_existing")
    private String removeExisting;

    @Column(name = "qualify_frequency")
    private Integer qualifyFrequency;
}
