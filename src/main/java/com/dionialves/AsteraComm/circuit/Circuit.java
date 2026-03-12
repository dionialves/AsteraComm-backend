package com.dionialves.AsteraComm.circuit;

import com.dionialves.AsteraComm.customer.Customer;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
