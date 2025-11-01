package com.hyanhsing.hyanjot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * REMINDER类型备忘项的专用字段
 */
@Data
@Entity
@Table(name = "reminder_item")
public class ReminderItem {
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "id")
    @MapsId  // 共享主键，id与Item表的id相同
    private Item item;

    /**
     * 提醒时间（必须建索引以优化定时查询性能）
     */
    @Column(name = "remind_time", nullable = false)
    private LocalDateTime remindTime;

    /**
     * 重复类型：none, daily, weekly, monthly
     */
    @Column(name = "repeat_type", length = 20)
    private String repeatType = "none";

    /**
     * 提前提醒分钟数
     */
    @Column(name = "advance_minutes")
    private Integer advanceMinutes = 0;

    /**
     * 是否已提醒
     */
    @Column(name = "notified")
    private Boolean notified = false;

    /**
     * 下次提醒时间（用于重复提醒）
     */
    @Column(name = "next_remind_time")
    private LocalDateTime nextRemindTime;
}
