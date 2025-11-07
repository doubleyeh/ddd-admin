<template>
    <n-config-provider :theme-overrides="themeOverrides" abstract>
        <n-message-provider>
            <n-dialog-provider>
                <router-view />
            </n-dialog-provider>
        </n-message-provider>
    </n-config-provider>
</template>

<script setup>
import { NConfigProvider, NMessageProvider, NDialogProvider } from 'naive-ui';

const themeOverrides = {
    common: {
        primaryColor: '#2080f0',
        primaryColorHover: '#409eff'
    },
    Layout: {
        headerHeight: '60px'
    }
};
</script>

<style>
#app {
    height: 100vh;
    width: 100vw;
    overflow: hidden;
}
</style>