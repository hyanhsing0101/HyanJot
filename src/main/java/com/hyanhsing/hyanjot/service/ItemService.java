package com.hyanhsing.hyanjot.service;

import com.hyanhsing.hyanjot.entity.Item;
import java.util.List;
import java.util.Optional;

public interface ItemService {
    // 创建备忘项
    Item createItem(Item item);

    // 根据ID查找
    Optional<Item> findById(Long id);

    // 获取用户的所有备忘项
    List<Item> findByUserId(Long userId);

    // 根据类型查找
    List<Item> findByUserIdAndType(Long userId, String type);

    // 根据状态查找
    List<Item> findByUserIdAndStatus(Long userId, String status);

    // 更新备忘项
    Item updateItem(Item item);

    // 删除备忘项
    void deleteItem(Long id);

    // 完成/激活备忘项（切换状态）
    Item toggleStatus(Long id);
}