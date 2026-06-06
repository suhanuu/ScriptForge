package com.scriptforge.model.schema;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 动作元素 —— 描述角色的行为或场景氛围，可用括号标注情绪。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("action")
public class ActionElement implements ContentElement {

    /** 动作描述文本，如 "李薇推开办公室的门" */
    private String text;
}
