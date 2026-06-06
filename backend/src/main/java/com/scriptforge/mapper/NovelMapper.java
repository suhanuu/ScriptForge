package com.scriptforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptforge.model.entity.Novel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NovelMapper extends BaseMapper<Novel> {

    @Select("SELECT * FROM novels ORDER BY created_at DESC")
    List<Novel> findAllByOrderByCreatedAtDesc();

    @Select("SELECT * FROM novels WHERE uuid = #{uuid}")
    Novel findByUuid(String uuid);
}
