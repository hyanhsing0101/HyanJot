package com.hyanhsing.hyanjot.repository;

import com.hyanhsing.hyanjot.entity.ReminderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderItemRepository extends JpaRepository<ReminderItem, Long> {
    /**
     * 查找到期的提醒（用于定时任务）
     */
    List<ReminderItem> findByRemindTimeLessThanEqualAndNotifiedFalse(LocalDateTime now);
}
