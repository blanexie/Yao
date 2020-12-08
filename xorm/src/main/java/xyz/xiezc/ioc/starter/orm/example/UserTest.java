package xyz.xiezc.ioc.starter.orm.example;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class UserTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

}
