package com.sysgears.counter.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "COUNTER")
public class Counter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "ID")
    private int id;

    @Column(name = "AMOUNT")
    private int amount;

    public Counter(int amount) {
        this.amount = amount;
    }

    public Counter increaseAmount(int amount) {
        this.amount += amount;
        return this;
    }
}