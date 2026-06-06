package com.scriptforge.model.schema;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对白元素 —— 单句台词，含说话人 ID、台词文本和括号语气提示。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("dialogue")
public class DialogueElement implements ContentElement {

    /** 说话人的角色 ID（引用 characters[].id） */
    private String character;

    /** 台词文本 */
    private String text;

    /** 括号中的语气提示，如 "哑着嗓子"、挂掉电话，声音低沉" */
    private String parenthetical;
}
