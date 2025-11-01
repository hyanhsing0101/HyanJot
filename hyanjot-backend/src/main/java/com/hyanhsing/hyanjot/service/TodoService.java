package com.hyanhsing.hyanjot.service;

import com.hyanhsing.hyanjot.entity.Item;
import com.hyanhsing.hyanjot.entity.TodoItem;

/**
 * TODO类型备忘项的专用服务
 */
public interface TodoService {
    /**
     * 创建TODO（创建Item + TodoItem）
     */
    Item createTodo(com.hyanhsing.hyanjot.dto.TodoCreateDTO dto);

    /**
     * 进度+1（硬件端用）
     */
    TodoItem incrementProgress(Long itemId);

    /**
     * 进度-1（硬件端用，最小值0）
     */
    TodoItem decrementProgress(Long itemId);

    /**
     * 设置进度值（Web端用）
     */
    TodoItem setProgress(Long itemId, Integer current, Integer total);

    /**
     * 切换子任务完成状态（硬件端用）
     */
    TodoItem toggleSubtask(Long itemId, Integer index);

    /**
     * 添加子任务（Web端用）
     */
    TodoItem addSubtask(Long itemId, String text);

    /**
     * 更新子任务文本（Web端用）
     */
    TodoItem updateSubtask(Long itemId, Integer index, String text);

    /**
     * 删除子任务（Web端用）
     */
    TodoItem deleteSubtask(Long itemId, Integer index);

    /**
     * 检查并更新完成状态
     * 进度模式：如果progressCurrent >= progressTotal，标记完成
     * 子任务模式：如果所有子任务都完成，标记完成
     * 否则标记为未完成
     */
    void checkAndUpdateCompletionStatus(Long itemId);
}
