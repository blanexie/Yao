package xyz.xiezc.example.web;

import lombok.Data;
import xyz.xiezc.ioc.starter.orm.annotation.Column;
import xyz.xiezc.ioc.starter.orm.annotation.Id;
import xyz.xiezc.ioc.starter.orm.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("t_album")
public class Album implements Serializable {
    @Id
    Integer id;
    @Column
    String title;
    @Column
    String publishTime;
    @Column
    String type;
    @Column
    LocalDateTime createTime;
    @Column
    Integer coverId;
    @Column
    Integer see;

}
