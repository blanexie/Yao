package xyz.xiezc.example.web.mapper;

import org.apache.ibatis.annotations.Param;
import xyz.xiezc.example.web.entity.Album;
import xyz.xiezc.ioc.starter.orm.common.BaseMapper;

public interface AlbumMapper extends BaseMapper<Album> {

    Album queryById(@Param("id") Integer id);

}
