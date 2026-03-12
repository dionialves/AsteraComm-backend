package com.dionialves.AsteraComm.cdr;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Immutable
@Entity
@Table(name = "cdr")
public class CdrRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uniqueid", nullable = false)
    @JsonProperty("uniqueid")
    private String uniqueId;

    @Column(name = "calldate")
    private LocalDateTime calldate;

    @Column(name = "clid")
    private String clid;

    @Column(name = "src")
    private String src;

    @Column(name = "dst")
    private String dst;

    @Column(name = "dcontext")
    private String dcontext;

    @Column(name = "channel")
    private String channel;

    @Column(name = "dstchannel")
    private String dstchannel;

    @Column(name = "lastapp")
    private String lastapp;

    @Column(name = "lastdata")
    private String lastdata;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "billsec")
    private Integer billsec;

    @Column(name = "disposition")
    private String disposition;

    @Column(name = "amaflags")
    private Integer amaflags;

    @Column(name = "accountcode")
    private String accountcode;

    @Column(name = "userfield")
    private String userfield;
}
