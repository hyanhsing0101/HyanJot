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
        // 验证必填字段
        Objects.requireNonNull(dto.getDeadline(), "截止日期不能为空");
        Objects.requireNonNull(dto.getPriority(), "优先级不能为空");
        
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
        todoItem.setItem(item);  // @MapsId会自动从item中获取ID
        todoItem.setDeadline(dto.getDeadline());
        todoItem.setPriority(dto.getPriority() != null ? dto.getPriority() : "medium");
        
        // progressMode处理：
        // null = 普通TODO
        // true = 进度模式
        // false = 子任务模式
        Boolean progressMode = dto.getProgressMode();
        boolean hasSubtasks = dto.getSubtasks() != null && !dto.getSubtasks().isEmpty();

        if (progressMode == null) {
            // 普通TODO模式：保持progressMode为null，不设置进度和子任务
            todoItem.setProgressMode(null);
            todoItem.setProgressCurrent(null);
            todoItem.setProgressTotal(null);
            todoItem.setSubtasks(null);
        } else if (progressMode) {
            // 进度模式
            todoItem.setProgressMode(true);
            todoItem.setProgressCurrent(0);
            todoItem.setProgressTotal(dto.getProgressTotal());
            todoItem.setSubtasks(null);
        } else {
            // 子任务模式（false）
            todoItem.setProgressMode(false);
            todoItem.setProgressCurrent(null);
            todoItem.setProgressTotal(null);
            // 将子任务列表转为JSON
            if (hasSubtasks) {
                try {
                    String subtasksJson = objectMapper.writeValueAsString(dto.getSubtasks());
                    todoItem.setSubtasks(subtasksJson);
                } catch (Exception e) {
                    throw new RuntimeException("子任务JSON转换失败", e);
                }
            } else {
                // 子任务模式下如果没有子任务，设置空数组
                try {
                    String subtasksJson = objectMapper.writeValueAsString(new java.util.ArrayList<>());
                    todoItem.setSubtasks(subtasksJson);
                } catch (Exception e) {
                    throw new RuntimeException("子任务JSON转换失败", e);
                }
            }
        }

        todoItemRepository.save(todoItem);
        return item;
    }

    @Override
    @Transactional
    public Item updateTodo(Long itemId, TodoCreateDTO dto) {
        Objects.requireNonNull(itemId, "ID不能为空");
        // 更新Item（只更新标题和内容，保持其他字段不变）
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));
        if (dto.getTitle() != null) {
            item.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            item.setContent(dto.getContent());
        }
        @SuppressWarnings("null")
        Item savedItem = itemRepository.save(item);
        item = Objects.requireNonNull(savedItem, "保存Item失败");

        // 更新TodoItem（只更新deadline，保持progressMode等字段不变）
        TodoItem todoItem = todoItemRepository.findById(itemId).orElse(new TodoItem());
        if (todoItem.getId() == null) {
            todoItem.setId(Objects.requireNonNull(itemId));
            todoItem.setItem(item);
            // 如果是新建TodoItem，需要设置必填字段
            todoItem.setDeadline(dto.getDeadline());
            todoItem.setPriority(dto.getPriority() != null ? dto.getPriority() : "medium");
        } else {
            // 已存在的TodoItem，更新deadline和priority
            if (dto.getDeadline() != null) {
                todoItem.setDeadline(dto.getDeadline());
            }
            if (dto.getPriority() != null) {
                todoItem.setPriority(dto.getPriority());
            }
            // 保持原有的progressMode、progressTotal、progressCurrent、subtasks不变
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

        if (todoItem.getProgressMode() == null || !todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是进度模式");
        }

        if (todoItem.getProgressCurrent() < todoItem.getProgressTotal()) {
            todoItem.setProgressCurrent(todoItem.getProgressCurrent() + 1);
            todoItem = todoItemRepository.save(todoItem);
        }
        // 每次增加进度后都检查完成状态（即使已经满了也要检查，确保状态正确）
        checkAndUpdateCompletionStatus(itemId);

        return todoItem;
    }

    @Override
    @Transactional
    public TodoItem decrementProgress(Long itemId) {
        Objects.requireNonNull(itemId, "ID不能为空");
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (todoItem.getProgressMode() == null || !todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是进度模式");
        }

        if (todoItem.getProgressCurrent() > 0) {
            todoItem.setProgressCurrent(todoItem.getProgressCurrent() - 1);
            todoItem = todoItemRepository.save(todoItem);
        }
        // 每次减少进度后都检查完成状态
        checkAndUpdateCompletionStatus(itemId);

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
        Objects.requireNonNull(text, "子任务文本不能为空");
        
        TodoItem todoItem = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("TODO项不存在"));

        if (todoItem.getProgressMode() != null && todoItem.getProgressMode()) {
            throw new RuntimeException("该TODO不是子任务模式");
        }

        List<TodoCreateDTO.SubtaskDTO> subtasks = getSubtasks(todoItem);
        TodoCreateDTO.SubtaskDTO newSubtask = new TodoCreateDTO.SubtaskDTO();
        newSubtask.setText(text);
        newSubtask.setCompleted(false);
        subtasks.add(newSubtask);

        try {
            String subtasksJson = objectMapper.writeValueAsString(subtasks);
            todoItem.setSubtasks(subtasksJson);
            todoItem = todoItemRepository.save(todoItem);
            // 确保立即刷新，避免缓存问题
            todoItemRepository.flush();
            checkAndUpdateCompletionStatus(itemId);
            
            // 验证保存是否成功
            TodoItem saved = todoItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("保存后无法找到TODO项"));
            if (saved.getSubtasks() == null || !saved.getSubtasks().equals(subtasksJson)) {
                throw new RuntimeException("保存验证失败：数据不一致");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("子任务JSON转换失败: " + e.getMessage(), e);
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

        // 普通模式（progressMode为null）不自动完成
        if (todoItem.getProgressMode() == null) {
            return;
        }

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
