package com.hyanhsing.hyanjot.repository;

import com.hyanhsing.hyanjot.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // 根据用户ID查找所有备忘项
    List<Item> findByUserIdOrderBySortOrderAsc(Long userId);

    // 根据用户ID和类型查找
    List<Item> findByUserIdAndTypeOrderBySortOrderAsc(Long userId, String type);  // ← 加 OrderBySortOrderAsc

    // 根据用户ID和状态查找
    List<Item> findByUserIdAndStatusOrderBySortOrderAsc(Long userId, String status);  // ← 加 OrderBySortOrderAsc
}