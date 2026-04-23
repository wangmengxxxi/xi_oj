<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { addComment, getCommentList, toggleCommentLike, deleteComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import type { CommentVO } from '@/types'

const props = defineProps<{ questionId: number | string }>()
const userStore = useUserStore()

const comments = ref<CommentVO[]>([])
const loading = ref(false)
const newContent = ref('')
const submitting = ref(false)
const replyTo = ref<{ id: number; userName: string } | null>(null)
const replyContent = ref('')
const replySubmitting = ref(false)

async function loadComments() {
  loading.value = true
  try {
    const res = await getCommentList(props.questionId)
    comments.value = res.data.data ?? []
  } catch (err: any) {
    Message.error(err?.message || '加载评论失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!newContent.value.trim()) {
    Message.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await addComment({ questionId: props.questionId, content: newContent.value.trim() })
    newContent.value = ''
    Message.success('评论成功')
    loadComments()
  } catch (err: any) {
    Message.error(err?.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

async function handleReply(parentId: number) {
  if (!replyContent.value.trim()) {
    Message.warning('请输入回复内容')
    return
  }
  replySubmitting.value = true
  try {
    await addComment({
      questionId: props.questionId,
      content: replyContent.value.trim(),
      parentId,
    })
    replyContent.value = ''
    replyTo.value = null
    Message.success('回复成功')
    loadComments()
  } catch (err: any) {
    Message.error(err?.message || '回复失败')
  } finally {
    replySubmitting.value = false
  }
}

async function handleLike(commentId: number) {
  try {
    await toggleCommentLike({ commentId })
    loadComments()
  } catch (err: any) {
    Message.error(err?.message || '操作失败')
  }
}

function handleDelete(commentId: number) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条评论吗？',
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      try {
        await deleteComment({ commentId })
        Message.success('删除成功')
        loadComments()
      } catch (err: any) {
        Message.error(err?.message || '删除失败')
      }
    },
  })
}

function canDelete(comment: CommentVO) {
  return comment.userId === userStore.loginUser.id || userStore.isAdmin()
}

function formatTime(time: string) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)} 天前`
  return time.slice(0, 10)
}

onMounted(loadComments)
</script>

<template>
  <div class="comment-section">
    <!-- 发表评论 -->
    <div class="comment-input-area">
      <a-textarea
        v-model="newContent"
        placeholder="发表评论..."
        :auto-size="{ minRows: 2, maxRows: 4 }"
      />
      <a-button
        type="primary"
        size="small"
        :loading="submitting"
        @click="handleSubmit"
      >
        提交
      </a-button>
    </div>

    <!-- 评论列表 -->
    <a-spin :loading="loading">
      <div v-if="comments.length === 0 && !loading" class="empty-hint">暂无评论</div>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-main">
          <div class="comment-header">
            <span class="comment-user">用户 {{ comment.userId }}</span>
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          </div>
          <div class="comment-body">{{ comment.content }}</div>
          <div class="comment-actions">
            <span class="like-btn" @click="handleLike(comment.id)">
              👍 {{ comment.likeNum || 0 }}
            </span>
            <span class="reply-btn" @click="replyTo = { id: comment.id, userName: `用户${comment.userId}` }">
              回复
            </span>
            <span v-if="canDelete(comment)" class="delete-btn" @click="handleDelete(comment.id)">
              删除
            </span>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyTo?.id === comment.id" class="reply-input-area">
            <a-textarea
              v-model="replyContent"
              :placeholder="`回复 ${replyTo.userName}...`"
              :auto-size="{ minRows: 1, maxRows: 3 }"
            />
            <a-space size="small">
              <a-button size="mini" @click="replyTo = null; replyContent = ''">取消</a-button>
              <a-button type="primary" size="mini" :loading="replySubmitting" @click="handleReply(comment.id)">回复</a-button>
            </a-space>
          </div>
        </div>

        <!-- 子回复 -->
        <div v-if="comment.replies?.length" class="replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <div class="comment-header">
              <span class="comment-user">用户 {{ reply.userId }}</span>
              <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
            </div>
            <div class="comment-body">{{ reply.content }}</div>
            <div class="comment-actions">
              <span class="like-btn" @click="handleLike(reply.id)">
                👍 {{ reply.likeNum || 0 }}
              </span>
              <span v-if="canDelete(reply)" class="delete-btn" @click="handleDelete(reply.id)">
                删除
              </span>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.comment-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-input-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.comment-input-area :deep(.arco-textarea-wrapper) {
  width: 100%;
}

.comment-item {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
}

.comment-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-user {
  font-size: 13px;
  font-weight: 500;
  color: #262626;
}

.comment-time {
  font-size: 12px;
  color: #8c8c8c;
}

.comment-body {
  font-size: 14px;
  color: #434343;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 16px;
  font-size: 12px;
}

.like-btn,
.reply-btn,
.delete-btn {
  cursor: pointer;
  color: #8c8c8c;
  transition: color 0.15s;
}

.like-btn:hover {
  color: #ffa116;
}

.reply-btn:hover {
  color: #165dff;
}

.delete-btn:hover {
  color: #ef4743;
}

.reply-input-area {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
  padding-left: 16px;
}

.reply-input-area :deep(.arco-textarea-wrapper) {
  width: 100%;
}

.replies {
  margin-left: 24px;
  border-left: 2px solid #f0f0f0;
  padding-left: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.reply-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.empty-hint {
  text-align: center;
  color: #8c8c8c;
  font-size: 13px;
  padding: 20px 0;
}
</style>
