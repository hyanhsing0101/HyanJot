package com.hyanhsing.hyanjot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建TODO的DTO
 */
@Data
public class TodoCreateDTO {
    // Item基础字段
    private Long userId;
    private String title;
    private String content;
    private Integer sortOrder;

    // TodoItem专用字段
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;  // 必填
    private String priority;      // 必填：low/medium/high
    private Boolean progressMode; // true=进度模式, false=子任务模式, null=普通模式

    // 进度模式字段
    private Integer progressTotal; // 如果progressMode=true，必填

    // 子任务模式字段
    private List<SubtaskDTO> subtasks; // 如果progressMode=false，必填（至少一个）

    @Data
    public static class SubtaskDTO {
        private String text;
        private Boolean completed = false;
    }
}
