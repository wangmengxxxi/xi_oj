<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import { getWrongQuestionList, reviewWrongQuestion } from '@/api/ai'
import { getQuestionVOById } from '@/api/question'
import { fetchSSE } from '@/utils/sse'
import type { WrongQuestionVO } from '@/types'
import MdViewer from '@/components/MdViewer.vue'

const router = useRouter()
const loading = ref(false)
const wrongList = ref<WrongQuestionVO[]>([])
const filterType = ref('')
const questionTitleMap = ref<Record<string, string>>({})

const JUDGE_RESULT_MAP: Record<string, { text: string; cls: string }> = {
  'Wrong Answer': { text: '答案错误', cls: 'status-wrong' },
  'Time Limit Exceeded': { text: '超时', cls: 'status-tle' },
  'Runtime Error': { text: '运行错误', cls: 'status-wrong' },
  'Compile Error': { text: '编译错误', cls: 'status-tle' },
  'Memory Limit Exceeded': { text: '内存超限', cls: 'status-tle' },
}

const filteredList = computed(() => {
  if (!filterType.value) return wrongList.value
  return wrongList.value.filter((w) => w.wrongJudgeResult === filterType.value)
})

async function loadList() {
  loading.value = true
  try {
    const res = await getWrongQuestionList()
    wrongList.value = res.data.data ?? []
    const uniqueIds = [...new Set(wrongList.value.map((w) => w.questionId).filter(Boolean))]
    for (const qid of uniqueIds) {
      if (!questionTitleMap.value[String(qid)]) {
        getQuestionVOById(qid).then((r) => {
          if (r.data.data?.title) {
            questionTitleMap.value[String(qid)] = r.data.data.title
          }
        }).catch(() => {})
      }
    }
  } catch (err: any) {
    Message.error(err?.message || '加载错题列表失败')
  } finally {
    loading.value = false
  }
}

// 分析弹窗
const analysisVisible = ref(false)
const analysisContent = ref('')
const analyzing = ref(false)
const currentWrong = ref<WrongQuestionVO | null>(null)
let sseController: AbortController | null = null

function openAnalysis(item: WrongQuestionVO) {
  currentWrong.value = item
  analysisContent.value = item.wrongAnalysis || ''
  analysisVisible.value = true

  if (!item.wrongAnalysis) {
    startAnalysis(item)
  }
}

function startAnalysis(item: WrongQuestionVO) {
  analysisContent.value = ''
  analyzing.value = true

  sseController = fetchSSE('/api/ai/wrong-question/analysis/stream', {
    wrongQuestionId: item.id,
  }, {
    onToken(token) {
      analysisContent.value += token
    },
    onDone() {
      analyzing.value = false
    },
    onError(msg) {
      analyzing.value = false
      if (!analysisContent.value) {
        analysisContent.value = `分析失败: ${msg}`
      }
    },
  })
}

async function handleReview(item: WrongQuestionVO) {
  try {
    await reviewWrongQuestion({ wrongQuestionId: item.id })
    item.isReviewed = 1
    item.reviewCount = (item.reviewCount || 0) + 1
    Message.success('已标记复习')
  } catch (err: any) {
    Message.error(err?.message || '标记失败')
  }
}

function goToQuestion(questionId: number | string) {
  router.push(`/view/question/${questionId}`)
}

function getJudgeDisplay(result: string) {
  return JUDGE_RESULT_MAP[result] ?? { text: result || '未知', cls: 'status-pending' }
}

onMounted(loadList)
onUnmounted(() => { if (sseController) sseController.abort() })
</script>

<template>
  <div class="wrong-question-page">
    <div class="page-header">
      <div class="page-title">
        <span class="title-text">AI 错题本</span>
        <span class="title-sub">自动收集判题失败的提交，AI 帮你分析错因</span>
      </div>
      <a-space>
        <a-select
          v-model="filterType"
          placeholder="筛选错误类型"
          allow-clear
          style="width: 160px"
        >
          <a-option value="Wrong Answer">答案错误</a-option>
          <a-option value="Time Limit Exceeded">超时</a-option>
          <a-option value="Runtime Error">运行错误</a-option>
          <a-option value="Compile Error">编译错误</a-option>
        </a-select>
        <a-button :loading="loading" @click="loadList">刷新</a-button>
      </a-space>
    </div>

    <a-spin :loading="loading">
      <div v-if="filteredList.length === 0" class="empty-hint">
        {{ loading ? '' : '暂无错题记录' }}
      </div>
      <div class="card-grid">
        <div v-for="item in filteredList" :key="item.id" class="wrong-card">
          <div class="card-top">
            <a-tag :color="getJudgeDisplay(item.wrongJudgeResult).cls === 'status-wrong' ? 'red' : 'orangered'" size="small">
              {{ getJudgeDisplay(item.wrongJudgeResult).text }}
            </a-tag>
            <span class="review-count">复习 {{ item.reviewCount ?? 0 }} 次</span>
            <a-tag v-if="item.isReviewed === 1" color="green" size="small">已复习</a-tag>
          </div>
          <div class="card-question" @click="goToQuestion(item.questionId)">
            {{ questionTitleMap[String(item.questionId)] || `题目 #${item.questionId}` }}
          </div>
          <div class="card-actions">
            <a-button type="text" size="small" @click="openAnalysis(item)">查看分析</a-button>
            <a-button type="text" size="small" @click="handleReview(item)">标记复习</a-button>
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 分析弹窗 -->
    <a-modal
      v-model:visible="analysisVisible"
      title="AI 错题分析"
      :width="720"
      :footer="false"
      @close="() => { if (sseController) { sseController.abort(); sseController = null } analyzing = false }"
    >
      <div class="analysis-modal-body">
        <div v-if="currentWrong" class="wrong-code-section">
          <div class="section-label">错误代码</div>
          <pre class="code-block">{{ currentWrong.wrongCode }}</pre>
        </div>
        <div class="analysis-section">
          <div class="section-label">
            AI 分析
            <a-button
              v-if="!analyzing && currentWrong"
              type="text"
              size="mini"
              @click="startAnalysis(currentWrong!)"
            >
              重新分析
            </a-button>
          </div>
          <a-spin v-if="analyzing && !analysisContent" />
          <MdViewer v-else-if="analysisContent" :content="analysisContent" />
          <div v-else class="empty-hint">暂无分析结果</div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.wrong-question-page {
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

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.wrong-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #ebebeb;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: box-shadow 0.15s;
}

.wrong-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-count {
  font-size: 12px;
  color: #8c8c8c;
  margin-left: auto;
}

.card-question {
  font-size: 14px;
  color: #165dff;
  cursor: pointer;
  font-weight: 500;
}

.card-question:hover {
  text-decoration: underline;
}

.card-actions {
  display: flex;
  gap: 4px;
  border-top: 1px solid #f5f5f5;
  padding-top: 8px;
}

.empty-hint {
  text-align: center;
  color: #8c8c8c;
  font-size: 14px;
  padding: 40px 0;
}

.analysis-modal-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 60vh;
  overflow-y: auto;
}

.wrong-code-section {
  border-bottom: 1px solid #ebebeb;
  padding-bottom: 12px;
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-block {
  background: #f7f8fa;
  border-radius: 6px;
  padding: 12px;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', monospace;
  overflow-x: auto;
  max-height: 200px;
  overflow-y: auto;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
