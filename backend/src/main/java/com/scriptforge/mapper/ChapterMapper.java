package com.scriptforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptforge.model.entity.Chapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** chapters 表数据访问 */
@Mapper
public interface ChapterMapper extends BaseMapper<Chapter> {

    @Select("SELECT * FROM chapters WHERE novel_id = #{novelId} ORDER BY chapter_number ASC")
    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);

    @Select("<script>" +
            "SELECT * FROM chapters WHERE novel_id = #{novelId} " +
            "<if test='chapterNumbers != null and chapterNumbers.size() > 0'>" +
            "AND chapter_number IN " +
            "<foreach item='n' collection='chapterNumbers' open='(' separator=',' close=')'>#{n}</foreach>" +
            "</if>" +
            "ORDER BY chapter_number ASC" +
            "</script>")
    List<Chapter> findByNovelIdAndChapterNumbers(Long novelId, List<Integer> chapterNumbers);
}
