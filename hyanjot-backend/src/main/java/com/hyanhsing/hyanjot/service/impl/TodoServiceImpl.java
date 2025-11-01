package com.hyanhsing.hyanjot.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyanhsing.hyanjot.dto.TodoCreateDTO;
import com.hyanhsing.hyanjot.entity.Item;
import com.hyanhsing.hyanjot.entity.TodoItem;
import com.hyanhsing.hyanjot.repository.ItemRepository;
import com.hyanhsing.hyanjot.repository.TodoItemRepository;
import com.hyanhsing.hyanjot.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Item createTodo(TodoCreateDTO dto) {
        // 创建Item
        Item item = new Item();
        item.setUserId(dto.getUserId());
        item.setType("TODO");
        item.setTitle(dto.getTitle());
        item.setContent(dto.getContent());
        item.setStatus("active");
        item.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        item = itemRepository.save(item);

        // 创建TodoItem
        TodoItem todoItem = new TodoItem();
        todoItem.setItem(item);
        todoItem.setDeadline(dto.getDeadline());
        todoItem.setPriority(dto.getPriority());
        todoItem.setProgressMode(dto.getProgressMode());

        if (dto.getProgressMode()) {
            // 进度模式
            todoItem.setProgressCurrent(0);
            todoItem.setProgressTotal(dto.getProgressTotal());
            todoItem.setSubtasks(null);
        } else {
            // 子任务模式
            todoItem.setProgressCurrent(null);
            todoItem.setProgressTotal(null);
            // 将子任务列表转为JSON
            try {
                String subtasksJson = objectMapper.writeValueAsString(dto.getSubtasks());
                todoItem.setSubtasks(subtasksJson);
            } catch (Exception e) {
                throw new RuntimeException("子任务JSON转换失败", e);
            }
        }

        todoItemRepository.save(todoItem);
        return item;
    }

    @Override
    @Transactional
    public TodoItem incrementProgress(Long itemId) {
        Objects.requireNonNull(itemId, "ID不能为空");
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (!todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是进度模式");
        }

        if (todoItem.getProgressCurrent() < todoItem.getProgressTotal()) {
            todoItem.setProgressCurrent(todoItem.getProgressCurrent() + 1);
            todoItem = todoItemRepository.save(todoItem);
            checkAndUpdateCompletionStatus(itemId);
        }

        return todoItem;
    }

    @Override
    @Transactional
    public TodoItem decrementProgress(Long itemId) {
        Objects.requireNonNull(itemId, "ID不能为空");
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (!todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是进度模式");
        }

        if (todoItem.getProgressCurrent() > 0) {
            todoItem.setProgressCurrent(todoItem.getProgressCurrent() - 1);
            todoItem = todoItemRepository.save(todoItem);
            checkAndUpdateCompletionStatus(itemId);
        }

        return todoItem;
    }

    @Override
    @Transactional
    public TodoItem setProgress(Long itemId, Integer current, Integer total) {
        Objects.requireNonNull(itemId, "ID不能为空");
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (!todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是进度模式");
        }

        if (current < 0) {
            current = 0;
        }
        if (current > total) {
            current = total;
        }

        todoItem.setProgressCurrent(current);
        if (total != null) {
            todoItem.setProgressTotal(total);
        }
        todoItem = todoItemRepository.save(todoItem);
        checkAndUpdateCompletionStatus(itemId);

        return todoItem;
    }

    @Override
    @Transactional
    public TodoItem toggleSubtask(Long itemId, Integer index) {
        Objects.requireNonNull(itemId, "ID不能为空");
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是子任务模式");
        }

        List<TodoCreateDTO.SubtaskDTO> subtasks = getSubtasks(todoItem);
        if (index < 0 || index >= subtasks.size()) {
            throw new RuntimeException("子任务索引超出范围");
        }

        // 切换完成状态
        subtasks.get(index).setCompleted(!subtasks.get(index).getCompleted());

        // 保存回JSON
        try {
            todoItem.setSubtasks(objectMapper.writeValueAsString(subtasks));
            todoItem = todoItemRepository.save(todoItem);
            checkAndUpdateCompletionStatus(itemId);
        } catch (Exception e) {
            throw new RuntimeException("子任务JSON转换失败", e);
        }

        return todoItem;
    }

    @Override
    @Transactional
    public TodoItem addSubtask(Long itemId, String text) {
        Objects.requireNonNull(itemId, "ID不能为空");
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是子任务模式");
        }

        List<TodoCreateDTO.SubtaskDTO> subtasks = getSubtasks(todoItem);
        TodoCreateDTO.SubtaskDTO newSubtask = new TodoCreateDTO.SubtaskDTO();
        newSubtask.setText(text);
        newSubtask.setCompleted(false);
        subtasks.add(newSubtask);

        try {
            todoItem.setSubtasks(objectMapper.writeValueAsString(subtasks));
            todoItem = todoItemRepository.save(todoItem);
            checkAndUpdateCompletionStatus(itemId);
        } catch (Exception e) {
            throw new RuntimeException("子任务JSON转换失败", e);
        }

        return todoItem;
    }

    @Override
    @Transactional
    public TodoItem updateSubtask(Long itemId, Integer index, String text) {
        Objects.requireNonNull(itemId, "ID不能为空");
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是子任务模式");
        }

        List<TodoCreateDTO.SubtaskDTO> subtasks = getSubtasks(todoItem);
        if (index < 0 || index >= subtasks.size()) {
            throw new RuntimeException("子任务索引超出范围");
        }

        subtasks.get(index).setText(text);

        try {
            todoItem.setSubtasks(objectMapper.writeValueAsString(subtasks));
            todoItem = todoItemRepository.save(todoItem);
        } catch (Exception e) {
            throw new RuntimeException("子任务JSON转换失败", e);
        }

        return todoItem;
    }

    @Override
    @Transactional
    public TodoItem deleteSubtask(Long itemId, Integer index) {
        Objects.requireNonNull(itemId, "ID不能为空");
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是子任务模式");
        }

        List<TodoCreateDTO.SubtaskDTO> subtasks = getSubtasks(todoItem);
        if (index < 0 || index >= subtasks.size()) {
            throw new RuntimeException("子任务索引超出范围");
        }

        subtasks.remove((int) index);

        try {
            todoItem.setSubtasks(objectMapper.writeValueAsString(subtasks));
            todoItem = todoItemRepository.save(todoItem);
            checkAndUpdateCompletionStatus(itemId);
        } catch (Exception e) {
            throw new RuntimeException("子任务JSON转换失败", e);
        }

        return todoItem;
    }

    @Override
    @Transactional
    public void checkAndUpdateCompletionStatus(Long itemId) {
        Objects.requireNonNull(itemId, "ID不能为空");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("备忘项不存在"));

        if (!"TODO".equals(item.getType())) {
            return;
        }

        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        boolean shouldBeCompleted = false;

        if (todoItem.getProgressMode()) {
            // 进度模式：progressCurrent >= progressTotal
            if (todoItem.getProgressCurrent() != null && todoItem.getProgressTotal() != null) {
                shouldBeCompleted = todoItem.getProgressCurrent() >= todoItem.getProgressTotal();
            }
        } else {
            // 子任务模式：所有子任务都完成
            List<TodoCreateDTO.SubtaskDTO> subtasks = getSubtasks(todoItem);
            if (!subtasks.isEmpty()) {
                shouldBeCompleted = subtasks.stream()
                        .allMatch(subtask -> Boolean.TRUE.equals(subtask.getCompleted()));
            }
        }

        // 更新状态
        if (shouldBeCompleted) {
            item.setStatus("completed");
        } else {
            item.setStatus("active");
        }
        itemRepository.save(item);
    }

    /**
     * 解析子任务JSON
     */
    private List<TodoCreateDTO.SubtaskDTO> getSubtasks(TodoItem todoItem) {
        if (todoItem.getSubtasks() == null || todoItem.getSubtasks().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(todoItem.getSubtasks(),
                    new TypeReference<List<TodoCreateDTO.SubtaskDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException("子任务JSON解析失败", e);
        }
    }
}
