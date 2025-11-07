import { useUserStore } from '@/store/user';

export function hasPermission(permissionCode) {
    const userStore = useUserStore();
    const permissions = userStore.permissions; 

    if (!permissions || permissions.length === 0) {
        return false;
    }
    
    return permissions.includes(permissionCode);
}