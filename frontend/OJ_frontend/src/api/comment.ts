import request from './request'
import type {
  BaseResponse,
  CommentVO,
  CommentAddRequest,
  CommentLikeRequest,
  CommentDeleteRequest,
} from '@/types'

export const addComment = (data: CommentAddRequest) =>
  request.post<BaseResponse<number>>('/comment/add', data)

export const getCommentList = (questionId: number) =>
  request.get<BaseResponse<CommentVO[]>>('/comment/list', { params: { questionId } })

export const toggleCommentLike = (data: CommentLikeRequest) =>
  request.post<BaseResponse<boolean>>('/comment/like', data)

export const deleteComment = (data: CommentDeleteRequest) =>
  request.post<BaseResponse<string>>('/comment/delete', data)
