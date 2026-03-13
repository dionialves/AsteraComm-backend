package com.dionialves.AsteraComm.call;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "asteracomm_calls")
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_id", nullable = false, unique = true)
    private String uniqueId;

    @Column(name = "call_date", nullable = false)
    private LocalDateTime callDate;

    @Column(name = "caller_number")
    private String callerNumber;

    @Column(name = "dst", nullable = false)
    private String dst;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "bill_seconds", nullable = false)
    private Integer billSeconds;

    @Column(name = "disposition", nullable = false)
    private String disposition;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
