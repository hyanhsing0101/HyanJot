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
    public ResponseEntity<Map<String, Object>> getItemById(@PathVariable Long id) {
        return itemService.findByIdWithDetails(id)
                .map(item -> {
                    Map<String, Object> result = new HashMap<>();
                    // 基础Item字段
                    result.put("id", item.getId());
                    result.put("userId", item.getUserId());
                    result.put("type", item.getType());
                    result.put("title", item.getTitle());
                    result.put("content", item.getContent());
                    result.put("status", item.getStatus());
                    result.put("sortOrder", item.getSortOrder());
                    result.put("createdAt", item.getCreatedAt());
                    result.put("updatedAt", item.getUpdatedAt());
                    
                    // 根据类型添加子表数据
                    if ("TODO".equals(item.getType()) && item.getTodoItem() != null) {
                        Map<String, Object> todoData = new HashMap<>();
                        TodoItem todoItem = item.getTodoItem();
                        todoData.put("deadline", todoItem.getDeadline());
                        todoData.put("priority", todoItem.getPriority());
                        todoData.put("progressMode", todoItem.getProgressMode());
                        todoData.put("progressCurrent", todoItem.getProgressCurrent());
                        todoData.put("progressTotal", todoItem.getProgressTotal());
                        todoData.put("subtasks", todoItem.getSubtasks());
                        result.put("todoItem", todoData);
                    }
                    // TODO: 添加HABIT和REMINDER的处理
                    
                    return ResponseEntity.ok(result);
                })
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
        try {
            Item created = todoService.createTodo(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();  // 打印错误堆栈
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新TODO类型的备忘项
     */
    @PutMapping("/todo/{id}")
    public ResponseEntity<Item> updateTodo(@PathVariable Long id, @RequestBody TodoCreateDTO dto) {
        Item updated = todoService.updateTodo(id, dto);
        return ResponseEntity.ok(updated);
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
    public ResponseEntity<Map<String, Object>> addSubtask(
            @PathVariable Long id,
            @RequestBody Map<String, String> data) {
        if (data == null || !data.containsKey("text")) {
            return ResponseEntity.badRequest().build();
        }
        String text = data.get("text");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            TodoItem updated = todoService.addSubtask(id, text.trim());
            
            // 手动构建返回数据，避免序列化问题
            Map<String, Object> result = new HashMap<>();
            result.put("id", updated.getId());
            result.put("deadline", updated.getDeadline());
            result.put("priority", updated.getPriority());
            result.put("progressMode", updated.getProgressMode());
            result.put("progressCurrent", updated.getProgressCurrent());
            result.put("progressTotal", updated.getProgressTotal());
            result.put("subtasks", updated.getSubtasks());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // 返回错误信息
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", true);
            errorResult.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
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