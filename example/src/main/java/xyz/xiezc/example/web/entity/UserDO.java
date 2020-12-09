package xyz.xiezc.example.web.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 *
 * @author xiezc
 * @since 2020-06-09 17:59:52
 */
@Data
@Entity
@Table(name = "t_user")
public class UserDO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    String name;
    @Column
    Integer age;

}