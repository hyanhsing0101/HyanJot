package com.hyanhsing.hyanjot.controller;

import com.hyanhsing.hyanjot.dto.TodoCreateDTO;
import com.hyanhsing.hyanjot.entity.Item;
import com.hyanhsing.hyanjot.entity.TodoItem;
import com.hyanhsing.hyanjot.service.ItemService;
import com.hyanhsing.hyanjot.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private TodoService todoService;

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
     * 根据ID获取备忘项（包含子表数据）
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findByIdWithDetails(id)
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

    // ==================== TODO专用API ====================

    /**
     * 创建TODO类型的备忘项
     */
    @PostMapping("/todo")
    public ResponseEntity<Item> createTodo(@RequestBody TodoCreateDTO dto) {
        Item created = todoService.createTodo(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * 进度+1（硬件端用）
     */
    @PutMapping("/{id}/progress/increment")
    public ResponseEntity<TodoItem> incrementProgress(@PathVariable Long id) {
        TodoItem updated = todoService.incrementProgress(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * 进度-1（硬件端用）
     */
    @PutMapping("/{id}/progress/decrement")
    public ResponseEntity<TodoItem> decrementProgress(@PathVariable Long id) {
        TodoItem updated = todoService.decrementProgress(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * 设置进度值（Web端用）
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<TodoItem> setProgress(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> data) {
        Integer current = data.get("current");
        Integer total = data.get("total");
        TodoItem updated = todoService.setProgress(id, current, total);
        return ResponseEntity.ok(updated);
    }

    /**
     * 切换子任务完成状态（硬件端用）
     */
    @PutMapping("/{id}/subtask/{index}/toggle")
    public ResponseEntity<TodoItem> toggleSubtask(
            @PathVariable Long id,
            @PathVariable Integer index) {
        TodoItem updated = todoService.toggleSubtask(id, index);
        return ResponseEntity.ok(updated);
    }

    /**
     * 添加子任务（Web端用）
     */
    @PostMapping("/{id}/subtask")
    public ResponseEntity<TodoItem> addSubtask(
            @PathVariable Long id,
            @RequestBody Map<String, String> data) {
        String text = data.get("text");
        TodoItem updated = todoService.addSubtask(id, text);
        return ResponseEntity.ok(updated);
    }

    /**
     * 更新子任务文本（Web端用）
     */
    @PutMapping("/{id}/subtask/{index}")
    public ResponseEntity<TodoItem> updateSubtask(
            @PathVariable Long id,
            @PathVariable Integer index,
            @RequestBody Map<String, String> data) {
        String text = data.get("text");
        TodoItem updated = todoService.updateSubtask(id, index, text);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除子任务（Web端用）
     */
    @DeleteMapping("/{id}/subtask/{index}")
    public ResponseEntity<TodoItem> deleteSubtask(
            @PathVariable Long id,
            @PathVariable Integer index) {
        TodoItem updated = todoService.deleteSubtask(id, index);
        return ResponseEntity.ok(updated);
    }
}