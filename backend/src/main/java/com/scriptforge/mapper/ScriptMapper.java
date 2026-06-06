package com.scriptforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptforge.model.entity.Script;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/** scripts 表数据访问 */
@Mapper
public interface ScriptMapper extends BaseMapper<Script> {

    @Select("SELECT * FROM scripts WHERE novel_id = #{novelId}")
    Script findByNovelId(Long novelId);

    @Delete("DELETE FROM scripts WHERE novel_id = #{novelId}")
    void deleteByNovelId(Long novelId);
}
