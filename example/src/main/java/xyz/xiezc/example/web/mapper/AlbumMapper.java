package xyz.xiezc.example.web.mapper;

import org.apache.ibatis.annotations.Param;
import xyz.xiezc.example.web.entity.AlbumDO;
import xyz.xiezc.ioc.orm.common.BaseMapper;

public interface AlbumMapper extends BaseMapper<AlbumDO> {

    AlbumDO queryById(@Param("id") Integer id);

}
