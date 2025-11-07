import router, { asyncRoutes } from './router';
import { useUserStore } from './store/user';
import { getToken } from './utils/auth';
import NProgress from 'nprogress';

const whiteList = ['/login', '/404']; 

router.beforeEach(async (to, from, next) => {
    NProgress.start();
    const token = getToken();
    const message = window.$message; 

    if (token) {
        if (to.path === '/login') {
            next({ path: '/' });
            NProgress.done();
        } else {
            const userStore = useUserStore();
            const roles = userStore.roles;
            if (roles && roles.length === 0) {
                try {
                    await userStore.getInfo(); 
                    
                    const accessedRoutes = filterAsyncRoutes(asyncRoutes, userStore.permissions);
                    
                    accessedRoutes.forEach(route => {
                        router.addRoute(route);
                    });

                    next({ ...to, replace: true });

                } catch (error) {
                    await userStore.logout();
                    message.error('获取用户信息失败，请重新登录');
                    next(`/login?redirect=${to.path}`);
                    NProgress.done();
                }
            } else {
                next();
            }
        }
    } else {
        if (whiteList.includes(to.path)) {
            next(); 
        } else {
            next(`/login?redirect=${to.path}`);
            NProgress.done();
        }
    }
});

router.afterEach(() => {
    NProgress.done();
});

function filterAsyncRoutes(routes, permissions) {
    const res = [];

    routes.forEach(route => {
        const tmp = { ...route };
        
        const requiredPermissions = tmp.meta?.roles;

        if (requiredPermissions && requiredPermissions.length > 0) {
            if (requiredPermissions.some(role => permissions.includes(role))) {
                if (tmp.children) {
                    tmp.children = filterAsyncRoutes(tmp.children, permissions);
                }
                res.push(tmp);
            }
        } else {
            if (tmp.children) {
                tmp.children = filterAsyncRoutes(tmp.children, permissions);
            }
            res.push(tmp);
        }
    });

    return res;
}