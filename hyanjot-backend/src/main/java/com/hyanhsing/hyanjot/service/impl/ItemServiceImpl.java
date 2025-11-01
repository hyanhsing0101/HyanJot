package com.hyanhsing.hyanjot.service.impl;

import com.hyanhsing.hyanjot.entity.Item;
import com.hyanhsing.hyanjot.repository.ItemRepository;
import com.hyanhsing.hyanjot.repository.TodoItemRepository;
import com.hyanhsing.hyanjot.repository.HabitItemRepository;
import com.hyanhsing.hyanjot.repository.ReminderItemRepository;
import com.hyanhsing.hyanjot.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private HabitItemRepository habitItemRepository;

    @Autowired
    private ReminderItemRepository reminderItemRepository;

    @Override
    public Item createItem(Item item) {
        // 设置默认值
        if (item.getStatus() == null) {
            item.setStatus("active");
        }
        if (item.getSortOrder() == null) {
            item.setSortOrder(0);
        }
        return itemRepository.save(item);
    }

    @Override
    public Optional<Item> findById(Long id) {
        Objects.requireNonNull(id, "ID不能为空");
        return itemRepository.findById(id);
    }

    @Override
    public Optional<Item> findByIdWithDetails(Long id) {
        Objects.requireNonNull(id, "ID不能为空");
        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            // 根据type加载对应的子表数据
            if ("TODO".equals(item.getType())) {
                todoItemRepository.findById(id).ifPresent(item::setTodoItem);
            } else if ("HABIT".equals(item.getType())) {
                habitItemRepository.findById(id).ifPresent(item::setHabitItem);
            } else if ("REMINDER".equals(item.getType())) {
                reminderItemRepository.findById(id).ifPresent(item::setReminderItem);
            }
            return Optional.of(item);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findByUserId(Long userId) {
        return itemRepository.findByUserIdOrderBySortOrderAsc(userId);
    }

    @Override
    public List<Item> findByUserIdAndType(Long userId, String type) {
        return itemRepository.findByUserIdAndTypeOrderBySortOrderAsc(userId, type);
    }

    @Override
    public List<Item> findByUserIdAndStatus(Long userId, String status) {
        return itemRepository.findByUserIdAndStatusOrderBySortOrderAsc(userId, status);
    }

    @Override
    public Item updateItem(Item item) {
        Objects.requireNonNull(item, "Item不能为空");
        return itemRepository.save(item);
    }

    @Override
    public void deleteItem(Long id) {
        Objects.requireNonNull(id, "ID不能为空");
        itemRepository.deleteById(id);
    }

    @Override
    public Item toggleStatus(Long id) {
        Objects.requireNonNull(id, "ID不能为空");
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("备忘项不存在"));
        // 切换状态：active <-> completed
        if ("active".equals(item.getStatus())) {
            item.setStatus("completed");
        } else {
            item.setStatus("active");
        }
        return itemRepository.save(item);
    }
}