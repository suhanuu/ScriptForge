package com.scriptforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptforge.model.entity.Novel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NovelMapper extends BaseMapper<Novel> {
}
