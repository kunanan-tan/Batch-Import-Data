package com.example.batch.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author kunanan.t
 */

@Entity
@Data
public class TestDb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String one;
    private Date two;
    private BigDecimal three;

    public TestDb(){

    }
}
