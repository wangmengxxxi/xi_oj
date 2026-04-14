<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import {
  listRateLimitRules,
  updateRateLimitRule,
  warmUpRateLimitCache,
} from '@/api/rateLimit'
import type { RateLimitRule } from '@/api/rateLimit'

// 是否为同题冷却规则（该类型 limit_count 无意义）
const COOLDOWN_KEY = 'submit:user:question:cooldown'

const loading = ref(false)
const rules = ref<RateLimitRule[]>([])

async function loadRules() {
  loading.value = true
  try {
    const res = await listRateLimitRules()
    rules.value = res.data.data ?? []
  } catch (err: any) {
    Message.error(err?.message || '加载限流规则失败')
  } finally {
    loading.value = false
  }
}

// ===== 格式化时间窗口 =====
function formatSeconds(s: number): string {
  if (s >= 86400) return `${s / 86400} 天`
  if (s >= 3600) return `${s / 3600} 小时`
  if (s >= 60) return `${s / 60} 分钟`
  return `${s} 秒`
}

// ===== 编辑弹窗 =====
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({
  rule_key: '',
  rule_name: '',
  limit_count: 0,
  window_seconds: 0,
  is_enable: 1,
  isCooldown: false,
})

function openEdit(rule: RateLimitRule) {
  editForm.rule_key = rule.rule_key
  editForm.rule_name = rule.rule_name
  editForm.limit_count = rule.limit_count
  editForm.window_seconds = rule.window_seconds
  editForm.is_enable = rule.is_enable
  editForm.isCooldown = rule.rule_key === COOLDOWN_KEY
  editVisible.value = true
}

async function handleSave() {
  if (editForm.window_seconds <= 0) {
    Message.warning('时间窗口必须大于 0')
    return
  }
  if (!editForm.isCooldown && editForm.limit_count <= 0) {
    Message.warning('限制次数必须大于 0')
    return
  }
  saving.value = true
  try {
    await updateRateLimitRule({
      rule_key: editForm.rule_key,
      limit_count: editForm.isCooldown ? 1 : editForm.limit_count,
      window_seconds: editForm.window_seconds,
      is_enable: editForm.is_enable,
    })
    Message.success('保存成功，缓存已同步')
    editVisible.value = false
    loadRules()
  } catch (err: any) {
    Message.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// ===== 快速切换启用状态 =====
async function handleToggleEnable(rule: RateLimitRule) {
  const newEnable = rule.is_enable === 1 ? 0 : 1
  try {
    await updateRateLimitRule({
      rule_key: rule.rule_key,
      limit_count: rule.limit_count,
      window_seconds: rule.window_seconds,
      is_enable: newEnable,
    })
    rule.is_enable = newEnable
    Message.success(newEnable === 1 ? '已启用' : '已禁用')
  } catch (err: any) {
    Message.error(err?.message || '操作失败')
  }
}

// ===== 缓存预热 =====
const warmingUp = ref(false)
async function handleWarmUp() {
  warmingUp.value = true
  try {
    await warmUpRateLimitCache()
    Message.success('缓存预热完成')
  } catch (err: any) {
    Message.error(err?.message || '预热失败')
  } finally {
    warmingUp.value = false
  }
}

const columns = [
  { title: '规则名称', dataIndex: 'rule_name', width: 160 },
  { title: '规则说明', dataIndex: 'description', ellipsis: true },
  { title: '限制次数', slotName: 'limitCount', width: 100, align: 'center' as const },
  { title: '时间窗口 / 冷却时长', slotName: 'window', width: 150, align: 'center' as const },
  { title: '状态', slotName: 'status', width: 90, align: 'center' as const },
  { title: '操作', slotName: 'action', width: 80, align: 'center' as const },
]

onMounted(loadRules)
</script>

<template>
  <div class="manage-rate-limit">
    <!-- 页头 -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-text">提交限流规则</span>
        <span class="title-sub">管理各维度限流策略，修改后自动刷新 Redis 缓存</span>
      </div>
      <a-space>
        <a-button :loading="warmingUp" @click="handleWarmUp">
          缓存预热
        </a-button>
        <a-button :loading="loading" @click="loadRules">刷新</a-button>
      </a-space>
    </div>

    <!-- 限流规则说明 -->
    <a-alert type="info" class="tip-alert">
      <template #message>
        共 5 条限流规则，按 全局 → IP → 用户分钟 → 用户每日 → 同题冷却 顺序依次检查。
        禁用某条规则后该维度限流失效，不影响其余规则。
      </template>
    </a-alert>

    <!-- 规则表格 -->
    <a-table
      :data="rules"
      :columns="columns"
      :loading="loading"
      :pagination="false"
      row-key="id"
      class="rule-table"
    >
      <!-- 限制次数列 -->
      <template #limitCount="{ record }">
        <span v-if="record.rule_key === 'submit:user:question:cooldown'" class="text-muted">—</span>
        <span v-else class="count-badge">{{ record.limit_count }} 次</span>
      </template>

      <!-- 时间窗口列 -->
      <template #window="{ record }">
        <a-tag color="arcoblue" size="small">
          {{ formatSeconds(record.window_seconds) }}
        </a-tag>
      </template>

      <!-- 状态列 -->
      <template #status="{ record }">
        <a-switch
          :model-value="record.is_enable === 1"
          size="small"
          checked-color="#00b42a"
          @change="handleToggleEnable(record)"
        />
      </template>

      <!-- 操作列 -->
      <template #action="{ record }">
        <a-button type="text" size="small" @click="openEdit(record)">编辑</a-button>
      </template>
    </a-table>

    <!-- 编辑弹窗 -->
    <a-modal
      v-model:visible="editVisible"
      :title="`编辑规则：${editForm.rule_name}`"
      :ok-loading="saving"
      ok-text="保存"
      cancel-text="取消"
      @ok="handleSave"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="是否启用">
          <a-switch
            v-model="editForm.is_enable"
            :checked-value="1"
            :unchecked-value="0"
            checked-color="#00b42a"
          />
          <span class="switch-label">{{ editForm.is_enable === 1 ? '已启用' : '已禁用' }}</span>
        </a-form-item>

        <a-form-item v-if="!editForm.isCooldown" label="时间窗口内最大次数">
          <a-input-number
            v-model="editForm.limit_count"
            :min="1"
            :max="10000"
            style="width: 100%"
          >
            <template #suffix>次</template>
          </a-input-number>
        </a-form-item>

        <a-form-item :label="editForm.isCooldown ? '冷却时长（秒）' : '时间窗口大小（秒）'">
          <a-input-number
            v-model="editForm.window_seconds"
            :min="1"
            :max="86400"
            style="width: 100%"
          >
            <template #suffix>秒</template>
          </a-input-number>
          <div class="field-hint">
            {{ editForm.isCooldown
              ? '用户对同一题目提交后的冷却等待时间'
              : '滑动窗口大小，即统计"多少秒内"的提交次数' }}
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.manage-rate-limit {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.page-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-text {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.title-sub {
  font-size: 13px;
  color: #8c8c8c;
}

.tip-alert {
  border-radius: 6px;
}

.rule-table {
  background: #fff;
  border-radius: 8px;
}

.text-muted {
  color: #bfbfbf;
}

.count-badge {
  font-weight: 500;
  color: #262626;
}

.switch-label {
  margin-left: 8px;
  font-size: 13px;
  color: #595959;
}

.field-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.5;
}
</style>
