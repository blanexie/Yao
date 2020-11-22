package xyz.xiezc.example.web.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.xiezc.ioc.orm.annotation.Id;
import xyz.xiezc.ioc.orm.annotation.Table;

/**
 * 相册集表(Album)实体类
 *
 * @author xiezc
 * @since 2020-06-09 17:59:52
 */
@Setter
@Getter
@ToString
@Table("t_album")
public class AlbumDO implements Serializable {

    private static final long serialVersionUID = 172271691710292105L;

    @Id
    private Integer id;

    private String title;

    private String publishTime;

    private String type;

    private LocalDateTime createTime;
    /**
     * 第一个页面的url
     */
    private String coverUrl;

    private String mziId;

    private Integer see;

    private Integer fetchCount;

    private LocalDateTime updateTime;

}