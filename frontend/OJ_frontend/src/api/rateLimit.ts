import request from './request'

export interface RateLimitRule {
  id: number
  rule_key: string
  rule_name: string
  limit_count: number
  window_seconds: number
  is_enable: number
  description: string
  createTime: string
  updateTime: string
}

export interface RateLimitRuleUpdateRequest {
  rule_key: string
  limit_count: number
  window_seconds: number
  is_enable: number
}

export function listRateLimitRules() {
  return request.get<any>('/admin/rate-limit/rules')
}

export function updateRateLimitRule(data: RateLimitRuleUpdateRequest) {
  return request.post<any>('/admin/rate-limit/rule/update', data)
}

export function warmUpRateLimitCache() {
  return request.post<any>('/admin/rate-limit/cache/warm-up')
}
