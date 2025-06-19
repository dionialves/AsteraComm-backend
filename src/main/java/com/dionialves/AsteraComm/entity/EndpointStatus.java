package com.dionialves.AsteraComm.entity;

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

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "asteracomm_endpoint_status_history")
public class EndpointStatus {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "endpoint", referencedColumnName = "id")
    private Endpoint endpoint;

    private boolean online;

    private String ip;
    private String rtt;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    public EndpointStatus(Endpoint endpoint, boolean online, String ip, String rtt, LocalDateTime checkedAt) {
        this.endpoint = endpoint;
        this.online = online;
        this.ip = ip;
        this.rtt = rtt;
        this.checkedAt = checkedAt;
    }

    public void test() {
        System.out.println();
    }
}
