<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import { reportApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { formatDate } from '@/utils/format'
import type { PageResult, ReportRecord } from '@/types/api'

const ui = useUiStore()

const reports = ref<PageResult<ReportRecord> | null>(null)

async function loadReports(page = 0) {
  try {
    reports.value = await reportApi.getMyReports(page, 10)
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '举报记录加载失败')
  }
}

onMounted(() => loadReports())
</script>

<template>
  <section class="panel">
      <PageHeader
        eyebrow="History"
        title="我的举报记录"
        description="这里仅展示举报进度；发起举报请直接在歌曲、帖子和评论详情页中操作。"
      />

      <table class="table-like" v-if="reports?.content.length">
        <thead>
          <tr>
            <th>目标</th>
            <th>原因</th>
            <th>结果</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in reports.content" :key="`${item.targetType}-${item.targetId}-${item.createTime}`">
            <td>{{ item.targetType }} #{{ item.targetId }}</td>
            <td>{{ item.reason }}</td>
            <td>{{ item.result || '处理中' }}</td>
            <td>{{ formatDate(item.createTime) }}</td>
          </tr>
        </tbody>
      </table>

      <div v-else class="empty">你还没有举报记录</div>

      <PaginationBar
        :page="reports"
        @prev="loadReports((reports?.number ?? 1) - 1)"
        @next="loadReports((reports?.number ?? 0) + 1)"
      />
  </section>
</template>
