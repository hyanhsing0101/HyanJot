package com.hyanhsing.hyanjot.repository;

import com.hyanhsing.hyanjot.entity.HabitItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitItemRepository extends JpaRepository<HabitItem, Long> {
}
