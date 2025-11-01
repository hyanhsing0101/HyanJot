<template>
  <div class="item-list-container">
    <!-- 顶部标题栏 -->
    <div class="header">
      <h1 class="title">HyanJot</h1>
      <el-button type="primary" @click="showAddDialog = true" circle>
        <span style="font-size: 20px;">+</span>
      </el-button>
    </div>

    <!-- 类型筛选 -->
    <div class="filter-tabs">
      <div
        class="tab-item"
        :class="{ active: currentFilter === 'ALL' }"
        @click="currentFilter = 'ALL'"
      >
        全部
      </div>
      <div
        class="tab-item"
        :class="{ active: currentFilter === 'TODO' }"
        @click="currentFilter = 'TODO'"
      >
        TODO
      </div>
      <div
        class="tab-item"
        :class="{ active: currentFilter === 'HABIT' }"
        @click="currentFilter = 'HABIT'"
      >
        习惯
      </div>
      <div
        class="tab-item"
        :class="{ active: currentFilter === 'REMINDER' }"
        @click="currentFilter = 'REMINDER'"
      >
        提醒
      </div>
    </div>

    <!-- 备忘项列表 -->
    <draggable
      v-model="items"
      item-key="id"
      class="item-list"
      @end="handleDragEnd"
    >
      <template #item="{ element: item }">
        <div
          v-if="shouldShowItem(item)"
          class="item-card"
          :class="`type-${item.type.toLowerCase()}`"
        >
          <div class="item-checkbox">
            <el-checkbox
              :model-value="item.status === 'completed'"
              @change="toggleItem(item.id!)"
            />
          </div>
          <div class="item-content">
            <div class="item-title" :class="{ completed: item.status === 'completed' }">
              {{item.title}}
            </div>
            <div class="item-meta">
              {{item.type}} · {{formatDate(item.createdAt)}}
            </div>
          </div>
          <div class="item-actions">
            <el-button
              type="danger"
              text
              @click="handleDelete(item.id!)"
            >
              删除
            </el-button>
          </div>
        </div>
      </template>
    </draggable>

    <!-- 添加对话框（暂时简化版） -->
    <el-dialog
      v-model="showAddDialog"
      title="添加备忘项"
      width="500px"
    >
      <el-form :model="newItem">
        <el-form-item label="标题">
          <el-input v-model="newItem.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="newItem.type" placeholder="请选择类型">
            <el-option label="TODO" value="TODO" />
            <el-option label="习惯" value="HABIT" />
            <el-option label="提醒" value="REMINDER" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="newItem.content"
            type="textarea"
            placeholder="请输入内容详情"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserItems, createItem, toggleItemStatus, deleteItem ,updateItem} from '@/api/itemService'
import type { Item } from '@/types/item'
import draggable from 'vuedraggable'

// 数据状态
const items = ref<Item[]>([])
const currentFilter = ref<'ALL' | 'TODO' | 'HABIT' | 'REMINDER'>('ALL')
const showAddDialog = ref(false)
const newItem = ref<Partial<Item>>({
  title: '',
  type: 'TODO',
  content: '',
  userId: 1
})

// 加载数据
const loadItems = async () => {
  try {
    const data = await getUserItems()
    items.value = data.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  } catch {  // ← 直接去掉参数
    ElMessage.error('加载失败')
  }
}

// 添加备忘项
const handleAdd = async () => {
  if (!newItem.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  try {
    await createItem(newItem.value as Item)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    newItem.value = { title: '', type: 'TODO', content: '', userId: 1 }
    loadItems()
  } catch {  // ← 直接去掉参数
    ElMessage.error('添加失败')
  }
}

// 切换完成状态
const toggleItem = async (id: number) => {
  try {
    await toggleItemStatus(id)
    loadItems()
  } catch {  // ← 直接去掉参数
    ElMessage.error('操作失败')
  }
}

// 删除备忘项
const handleDelete = async (id: number) => {
  try {
    await deleteItem(id)
    ElMessage.success('删除成功')
    loadItems()
  } catch {  // ← 直接去掉参数
    ElMessage.error('删除失败')
  }
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 判断是否应该显示该项（根据筛选条件）
const shouldShowItem = (item: Item) => {
  if (item.status === 'deleted') return false
  if (currentFilter.value === 'ALL') return true
  return item.type === currentFilter.value
}

// 拖拽结束后更新顺序
const handleDragEnd = async () => {
  try {
    // 更新每个 item 的 sortOrder
    const updatePromises = items.value.map((item, index) => {
      if (item.sortOrder !== index) {
        return updateItem(item.id!, { ...item, sortOrder: index })
      }
      return Promise.resolve()
    })
    await Promise.all(updatePromises)
    ElMessage.success('顺序已更新')
  } catch {
    ElMessage.error('更新顺序失败')
    loadItems() // 失败时重新加载
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.item-list-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  width: 100%;
}

.title {
  font-size: 32px;
  font-weight: 500;
  color: #1A1A1A;
  margin: 0;
}

.filter-tabs {
  display: flex;
  gap: 32px;
  margin-bottom: 40px;
  border-bottom: 1px solid #E8E8E8;
  width: 100%;
}

.tab-item {
  padding: 12px 0;
  font-size: 16px;
  color: #8C8C8C;
  cursor: pointer;
  transition: color 0.3s;
  position: relative;
}

.tab-item.active {
  color: #1A1A1A;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #1A1A1A;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.item-card {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  background: #FAFAFA;
  border-radius: 8px;
  transition: background 0.3s;
}

.item-card:hover {
  background: #F5F5F5;
}

.item-checkbox {
  margin-right: 16px;
  padding-top: 2px;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 16px;
  color: #1A1A1A;
  margin-bottom: 8px;
}

.item-title.completed {
  color: #8C8C8C;
  text-decoration: line-through;
}

.item-meta {
  font-size: 14px;
  color: #8C8C8C;
}

.item-actions {
  margin-left: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #8C8C8C;
  font-size: 16px;
}

.item-card.type-todo {
  border-left: 4px solid #FF9F40;
}

.item-card.type-habit {
  border-left: 4px solid #4CAF50;
}

.item-card.type-reminder {
  border-left: 4px solid #409EFF;
}
</style>
