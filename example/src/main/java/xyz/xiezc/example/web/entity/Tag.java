package xyz.xiezc.example.web.entity;

import lombok.Data;
import xyz.xiezc.ioc.starter.orm.annotation.Column;
import xyz.xiezc.ioc.starter.orm.annotation.Id;
import xyz.xiezc.ioc.starter.orm.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("t_tag")
public class Tag implements Serializable {
    @Id
    Integer id;

}
