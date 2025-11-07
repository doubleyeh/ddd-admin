<template>
    <n-card :bordered="false" size="small">
        <n-space vertical>
            <n-form inline :model="queryForm" label-placement="left" label-width="60">
                <n-form-item label="用户名">
                    <n-input v-model:value="queryForm.username" clearable placeholder="请输入用户名" />
                </n-form-item>
                <n-form-item label="昵称">
                    <n-input v-model:value="queryForm.nickname" clearable placeholder="请输入昵称" />
                </n-form-item>
                <n-form-item label="状态">
                    <n-select v-model:value="queryForm.state" clearable :options="stateOptions" style="width: 120px" />
                </n-form-item>
                <n-form-item>
                    <n-button type="primary" @click="handleSearch">查询</n-button>
                </n-form-item>
                <n-form-item>
                    <n-button @click="resetQuery">重置</n-button>
                </n-form-item>
            </n-form>
            
            <n-space justify="space-between">
                <n-button 
                    v-if="hasPermission('user:create')"
                    type="primary" 
                    @click="handleAdd"
                >
                    新增用户
                </n-button>
            </n-space>

            <n-data-table
                :columns="columns"
                :data="tableData"
                :loading="loading"
                :pagination="paginationReactive"
                :remote="true"
                @update:page="handlePageChange"
                @update:page-size="handlePageSizeChange"
            />
        </n-space>
    </n-card>
</template>

<script setup>
import { h, reactive, ref, onMounted } from 'vue';
import { NButton, NInput, NSelect, NForm, NFormItem, NSpace, NDataTable, NCard, NTag, useMessage, useDialog } from 'naive-ui';
import { getPage, deleteUser, resetPassword } from '@/api/user';
import { hasPermission } from '@/utils/permission';

const message = useMessage();
const dialog = useDialog();

const queryForm = reactive({
    username: null,
    nickname: null,
    state: null,
});

const tableData = ref([]);
const loading = ref(false);

const stateOptions = [
    { label: '启用', value: 1 },
    { label: '禁用', value: 0 },
];

const paginationReactive = reactive({
    page: 1,
    pageSize: 10,
    showSizePicker: true,
    pageSizes: [10, 20, 50],
    itemCount: 0,
    prefix ({ itemCount }) {
        return `总计 ${itemCount} 条`;
    }
});

const createColumns = () => [
    { title: 'ID', key: 'id', width: 80 },
    { title: '用户名', key: 'username' },
    { title: '昵称', key: 'nickname' },
    { title: '租户ID', key: 'tenantId' },
    { 
        title: '状态', 
        key: 'state',
        render (row) {
            return h(
                NTag,
                { type: row.state === 1 ? 'success' : 'error', size: 'small' },
                { default: () => row.state === 1 ? '启用' : '禁用' }
            );
        }
    },
    { title: '创建时间', key: 'createdAt' },
    {
        title: '操作',
        key: 'actions',
        width: 280,
        render (row) {
            return h(NSpace, null, {
                default: () => [
                    h(
                        NButton,
                        { 
                            size: 'small', 
                            type: 'primary', 
                            text: true,
                            disabled: !hasPermission('user:update'),
                            onClick: () => handleEdit(row) 
                        },
                        { default: () => '编辑' }
                    ),
                    h(
                        NButton,
                        { 
                            size: 'small', 
                            type: 'warning', 
                            text: true,
                            disabled: !hasPermission('user:update'),
                            onClick: () => handleResetPassword(row.id) 
                        },
                        { default: () => '重置密码' }
                    ),
                    h(
                        NButton,
                        { 
                            size: 'small', 
                            type: 'error', 
                            text: true,
                            disabled: !hasPermission('user:delete'),
                            onClick: () => handleDelete(row.id) 
                        },
                        { default: () => '删除' }
                    )
                ]
            });
        }
    }
];

const columns = createColumns();

const fetchUsers = async () => {
    loading.value = true;
    try {
        const params = {
            ...queryForm,
            page: paginationReactive.page - 1,
            size: paginationReactive.pageSize,
        };
        const response = await getPage(params);
        tableData.value = response.data.content;
        paginationReactive.itemCount = response.data.totalElements;
    } catch (error) {
        message.error('获取用户列表失败');
    } finally {
        loading.value = false;
    }
};

const handleSearch = () => {
    paginationReactive.page = 1;
    fetchUsers();
};

const resetQuery = () => {
    queryForm.username = null;
    queryForm.nickname = null;
    queryForm.state = null;
    handleSearch();
};

const handlePageChange = (page) => {
    paginationReactive.page = page;
    fetchUsers();
};

const handlePageSizeChange = (pageSize) => {
    paginationReactive.pageSize = pageSize;
    paginationReactive.page = 1;
    fetchUsers();
};

const handleAdd = () => {
    message.info('打开新增用户弹窗');
};

const handleEdit = (row) => {
    message.info(`编辑用户: ${row.username}`);
};

const handleResetPassword = (id) => {
    dialog.warning({
        title: '警告',
        content: '确定要重置该用户的密码吗？',
        positiveText: '确定重置',
        negativeText: '取消',
        onPositiveClick: async () => {
            try {
                const response = await resetPassword(id);
                message.success(`重置成功！新密码为: ${response.data}`);
            } catch (error) {
                message.error('重置密码失败');
            }
        }
    });
};

const handleDelete = (id) => {
    dialog.error({
        title: '删除',
        content: '确定要删除该用户吗？',
        positiveText: '确定删除',
        negativeText: '取消',
        onPositiveClick: async () => {
            try {
                await deleteUser(id);
                message.success('删除成功');
                await fetchUsers();
            } catch (error) {
                message.error('删除失败');
            }
        }
    });
};

onMounted(() => {
    fetchUsers();
});
</script>