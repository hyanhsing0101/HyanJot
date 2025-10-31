package com.hyanhsing.hyanjot.service.impl;

import com.hyanhsing.hyanjot.entity.Item;
import com.hyanhsing.hyanjot.repository.ItemRepository;
import com.hyanhsing.hyanjot.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

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
        return itemRepository.findById(id);
    }

    @Override
    public List<Item> findByUserId(Long userId) {
        return itemRepository.findByUserId(userId);
    }

    @Override
    public List<Item> findByUserIdAndType(Long userId, String type) {
        return itemRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public List<Item> findByUserIdAndStatus(Long userId, String status) {
        return itemRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public Item updateItem(Item item) {
        return itemRepository.save(item);
    }

    @Override
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public Item toggleStatus(Long id) {
        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            // 切换状态：active <-> completed
            if ("active".equals(item.getStatus())) {
                item.setStatus("completed");
            } else {
                item.setStatus("active");
            }
            return itemRepository.save(item);
        }
        throw new RuntimeException("备忘项不存在");
    }
}