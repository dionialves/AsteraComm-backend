package com.dionialves.AsteraComm.domain.core.circuit.status;

import java.time.LocalDateTime;

import com.dionialves.AsteraComm.domain.asterisk.endpoint.Endpoint;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "circuits_status")
public class CircuitStatus {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "endpoint", referencedColumnName = "id")
    private Endpoint endpoint;

    private boolean online;

    private String ip;
    private String rtt;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    public CircuitStatus(Endpoint endpoint, boolean online, String ip, String rtt, LocalDateTime checkedAt) {
        this.endpoint = endpoint;
        this.online = online;
        this.rtt = rtt;
        this.checkedAt = checkedAt;
        this.ip = ip;
    }
}
