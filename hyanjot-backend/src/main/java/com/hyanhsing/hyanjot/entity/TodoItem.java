package com.hyanhsing.hyanjot.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

/**
 * TODO类型备忘项的专用字段
 */
@Data
@Entity
@Table(name = "todo_item")
public class TodoItem {
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "id")
    @MapsId  // 共享主键，id与Item表的id相同
    @JsonIgnore  // 避免序列化时的循环引用
    private Item item;

    /**
     * 截止日期（必填）
     */
    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    /**
     * 优先级：low, medium, high（必填）
     */
    @Column(length = 20, nullable = false)
    private String priority;

    /**
     * 模式：null=普通TODO, true=进度模式, false=子任务模式
     */
    @Column(name = "progress_mode", nullable = true)
    private Boolean progressMode;

    /**
     * 当前进度（进度模式使用）
     */
    @Column(name = "progress_current")
    private Integer progressCurrent = 0;

    /**
     * 总进度（进度模式使用）
     */
    @Column(name = "progress_total")
    private Integer progressTotal;

    /**
     * 子任务列表（子任务模式使用，JSON格式）
     * 格式：[{"text":"任务1","completed":false}, {"text":"任务2","completed":true}]
     */
    @Column(columnDefinition = "JSON")
    private String subtasks;
}
