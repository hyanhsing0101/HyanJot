package com.hyanhsing.hyanjot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * HABIT类型备忘项的专用字段
 */
@Data
@Entity
@Table(name = "habit_item")
public class HabitItem {
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "id")
    @MapsId  // 共享主键，id与Item表的id相同
    private Item item;

    /**
     * 重复规则：daily, weekly, monthly, custom
     */
    @Column(name = "repeat_rule", length = 50)
    private String repeatRule;

    /**
     * 连续打卡天数
     */
    @Column(name = "streak_days")
    private Integer streakDays = 0;

    /**
     * 目标天数
     */
    @Column(name = "target_days")
    private Integer targetDays;

    /**
     * 最后打卡日期
     */
    @Column(name = "last_check_date")
    private LocalDate lastCheckDate;

    /**
     * 打卡历史记录（JSON格式，存储日期列表）
     */
    @Column(name = "check_history", columnDefinition = "JSON")
    private String checkHistory;
}
