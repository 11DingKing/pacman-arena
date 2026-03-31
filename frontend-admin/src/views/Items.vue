<template>
  <div class="items-page table-page">
    <div class="page-header">
      <h2>道具管理</h2>
      <el-button type="primary" @click="openDialog()">新增道具</el-button>
    </div>
    
    <el-card class="table-card">
      <div class="table-wrapper">
        <el-table :data="list" v-loading="loading" class="full-width-table" height="100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="displayIcon" label="图标" width="60" />
          <el-table-column prop="name" label="名称" width="120" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="price" label="价格" width="80">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="effectType" label="效果类型" width="120" />
          <el-table-column prop="effectValue" label="效果值" width="80" />
          <el-table-column prop="duration" label="持续(秒)" width="80" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑道具' : '新增道具'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="使用emoji，如 🚀" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="效果类型">
          <el-select v-model="form.effectType">
            <el-option label="加速" value="SPEED_UP" />
            <el-option label="无敌" value="INVINCIBLE" />
            <el-option label="双倍积分" value="DOUBLE_SCORE" />
            <el-option label="额外生命" value="EXTRA_LIFE" />
            <el-option label="磁铁" value="MAGNET" />
          </el-select>
        </el-form-item>
        <el-form-item label="效果值">
          <el-input-number v-model="form.effectValue" :min="0" />
        </el-form-item>
        <el-form-item label="持续时间">
          <el-input-number v-model="form.duration" :min="0" />
          <span style="margin-left: 8px">秒</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { itemApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({})

// 道具图标映射表 - 解决数据库emoji编码问题
const iconMap = {
  'SPEED_UP': '🚀',
  'INVINCIBLE': '🛡️',
  'DOUBLE_SCORE': '⭐',
  'EXTRA_LIFE': '❤️',
  'MAGNET': '🧲'
}

// 获取道具图标
function getItemIcon(item) {
  if (item.icon && item.icon.length <= 4 && !/[a-zA-Z0-9]/.test(item.icon)) {
    return item.icon
  }
  return iconMap[item.effectType] || '📦'
}

const defaultForm = {
  name: '', icon: '', description: '', price: 0,
  effectType: 'SPEED_UP', effectValue: 0, duration: 0, status: 1
}

function onSizeChange() {
  pageNum.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await itemApi.list({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = (res.data.list || []).map(item => ({
      ...item,
      displayIcon: getItemIcon(item)
    }))
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openDialog(row = null) {
  form.value = row ? { ...row } : { ...defaultForm }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.name) {
    ElMessage.warning('请输入道具名称')
    return
  }
  
  submitting.value = true
  try {
    if (form.value.id) {
      await itemApi.update(form.value)
      ElMessage.success('更新成功')
    } else {
      await itemApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除道具 ${row.name} 吗？`, '提示', { type: 'warning' })
    await itemApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => loadData())
</script>

