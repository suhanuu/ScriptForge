package com.scriptforge.model.schema;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 场景内容多态元素基类 —— 用 type 字段区分 action / dialogue / transition，
 * 按时间线顺序排列在 scene.content 数组中。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionElement.class, name = "action"),
        @JsonSubTypes.Type(value = DialogueElement.class, name = "dialogue"),
        @JsonSubTypes.Type(value = TransitionElement.class, name = "transition")
})
public interface ContentElement {
}
