<template>
    <div class="dashboard-container">
        <n-space vertical :size="20">
            <n-card :bordered="false" size="large" title="欢迎回来" style="border-radius: 8px;">
                <n-grid x-gap="12" :cols="4">
                    <n-gi>
                        <n-statistic label="今日访问量" :value="data.todayVisits" value-style="color: #409eff">
                            <template #suffix>次</template>
                        </n-statistic>
                    </n-gi>
                    <n-gi>
                        <n-statistic label="累计用户数" :value="data.totalUsers" value-style="color: #67c23a">
                            <template #suffix>人</template>
                        </n-statistic>
                    </n-gi>
                    <n-gi>
                        <n-statistic label="活跃租户数" :value="data.activeTenants" value-style="color: #e6a23c">
                            <template #suffix>个</template>
                        </n-statistic>
                    </n-gi>
                    <n-gi>
                        <n-statistic label="待处理任务" :value="data.pendingTasks" value-style="color: #f56c6c">
                            <template #suffix>项</template>
                        </n-statistic>
                    </n-gi>
                </n-grid>
            </n-card>

            <n-grid x-gap="20" :cols="2">
                <n-gi>
                    <n-card title="系统公告" :bordered="false" style="border-radius: 8px;">
                        <n-list hoverable clickable>
                            <n-list-item v-for="(notice, index) in data.announcements" :key="index">
                                <template #default>
                                    <n-tag :type="notice.type" size="small" round>{{ notice.tag }}</n-tag>
                                    {{ notice.content }}
                                </template>
                                <template #suffix>
                                    <n-time :time="notice.time" format="MM-dd" />
                                </template>
                            </n-list-item>
                        </n-list>
                    </n-card>
                </n-gi>
                <n-gi>
                    <n-card title="项目信息" :bordered="false" style="border-radius: 8px;">
                        <n-descriptions :column="1" label-placement="left" size="medium">
                            <n-descriptions-item label="项目名称">DDD-Admin</n-descriptions-item>
                            <n-descriptions-item label="前端技术栈">Vue 3 / Naive UI / Pinia / JS</n-descriptions-item>
                            <n-descriptions-item label="后端技术栈">Spring Boot / DDD / MyBatis</n-descriptions-item>
                            <n-descriptions-item label="项目地址">
                                <n-a href="https://github.com/your-repo/ddd-admin-front" target="_blank">
                                    GitHub 仓库
                                </n-a>
                            </n-descriptions-item>
                        </n-descriptions>
                    </n-card>
                </n-gi>
            </n-grid>
        </n-space>
    </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue';
import { NCard, NGrid, NGi, NStatistic, NSpace, NList, NListItem, NTag, NTime, NDescriptions, NDescriptionsItem, NA } from 'naive-ui';

const data = reactive({
    todayVisits: 125,
    totalUsers: 853,
    activeTenants: 12,
    pendingTasks: 3,
    announcements: [
        { type: 'info', tag: '通知', content: '系统V1.1版本功能升级计划已启动。', time: Date.now() - 3600000 },
        { type: 'success', tag: '发布', content: '用户管理模块新增重置密码功能。', time: Date.now() - 86400000 * 2 },
        { type: 'warning', tag: '提醒', content: '请所有管理员及时更新密码。', time: Date.now() - 86400000 * 5 },
        { type: 'error', tag: '故障', content: 'API网关偶发性连接超时，正在紧急修复。', time: Date.now() - 86400000 * 8 }
    ]
});

onMounted(() => {

});
</script>

<style scoped>
.dashboard-container {
    padding: 0;
}
</style>