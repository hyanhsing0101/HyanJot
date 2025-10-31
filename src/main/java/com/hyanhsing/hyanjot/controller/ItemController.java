package com.hyanhsing.hyanjot.controller;

import com.hyanhsing.hyanjot.entity.Item;
import com.hyanhsing.hyanjot.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/item")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 创建备忘项
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        Item created = itemService.createItem(item);
        return ResponseEntity.ok(created);
    }

    /**
     * 获取用户的所有备忘项
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Item>> getItemsByUserId(@PathVariable Long userId) {
        List<Item> items = itemService.findByUserId(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * 根据类型获取备忘项
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Item>> getItemsByType(
            @PathVariable Long userId,
            @PathVariable String type) {
        List<Item> items = itemService.findByUserIdAndType(userId, type);
        return ResponseEntity.ok(items);
    }

    /**
     * 根据状态获取备忘项
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Item>> getItemsByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        List<Item> items = itemService.findByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(items);
    }

    /**
     * 根据ID获取备忘项
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新备忘项
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        item.setId(id);
        Item updated = itemService.updateItem(item);
        return ResponseEntity.ok(updated);
    }

    /**
     * 切换备忘项状态（完成/未完成）
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Item> toggleStatus(@PathVariable Long id) {
        Item updated = itemService.toggleStatus(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除备忘项
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteItem(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            itemService.deleteItem(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}