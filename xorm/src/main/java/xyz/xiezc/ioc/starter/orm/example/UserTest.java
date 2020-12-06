package xyz.xiezc.ioc.starter.orm.example;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class UserTest {

    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String name;

}
