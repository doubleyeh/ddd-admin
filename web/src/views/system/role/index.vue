<template>
    <n-card :bordered="false" size="small">
        <n-space vertical>
            <n-form inline :model="queryForm" label-placement="left" label-width="60">
                <n-form-item label="角色名">
                    <n-input v-model:value="queryForm.name" clearable placeholder="请输入角色名" />
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
                    v-if="hasPermission('role:create')"
                    type="primary" 
                    @click="handleAdd"
                >
                    新增角色
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
import { NButton, NInput, NForm, NFormItem, NSpace, NDataTable, NCard, useMessage, useDialog } from 'naive-ui';
import { getPage, deleteRole, getRoleMenus } from '@/api/role';
import { hasPermission } from '@/utils/permission';

const message = useMessage();
const dialog = useDialog();

const queryForm = reactive({
    name: null,
});

const tableData = ref([]);
const loading = ref(false);

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
    { title: '角色名称', key: 'name' },
    { title: '角色编码', key: 'code' },
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
                            type: 'info', 
                            text: true,
                            disabled: !hasPermission('role:update'),
                            onClick: () => handleMenuAssign(row) 
                        },
                        { default: () => '分配菜单' }
                    ),
                    h(
                        NButton,
                        { 
                            size: 'small', 
                            type: 'primary', 
                            text: true,
                            disabled: !hasPermission('role:update'),
                            onClick: () => handleEdit(row) 
                        },
                        { default: () => '编辑' }
                    ),
                    h(
                        NButton,
                        { 
                            size: 'small', 
                            type: 'error', 
                            text: true,
                            disabled: !hasPermission('role:delete'),
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

const fetchRoles = async () => {
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
        message.error('获取角色列表失败');
    } finally {
        loading.value = false;
    }
};

const handleSearch = () => {
    paginationReactive.page = 1;
    fetchRoles();
};

const resetQuery = () => {
    queryForm.name = null;
    handleSearch();
};

const handlePageChange = (page) => {
    paginationReactive.page = page;
    fetchRoles();
};

const handlePageSizeChange = (pageSize) => {
    paginationReactive.pageSize = pageSize;
    paginationReactive.page = 1;
    fetchRoles();
};

const handleAdd = () => {
    message.info('打开新增角色弹窗');
};

const handleEdit = (row) => {
    message.info(`编辑角色: ${row.name}`);
};

const handleMenuAssign = (row) => {
    message.info(`分配菜单给角色: ${row.name}`);
    getRoleMenus(row.id);
};

const handleDelete = (id) => {
    dialog.error({
        title: '删除',
        content: '确定要删除该角色吗？',
        positiveText: '确定删除',
        negativeText: '取消',
        onPositiveClick: async () => {
            try {
                await deleteRole(id);
                message.success('删除成功');
                await fetchRoles();
            } catch (error) {
                message.error('删除失败');
            }
        }
    });
};

onMounted(() => {
    fetchRoles();
});
</script>