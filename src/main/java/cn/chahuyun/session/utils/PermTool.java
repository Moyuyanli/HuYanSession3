package cn.chahuyun.session.utils;

import cn.chahuyun.api.permission.api.HuYanPermissionService;
import cn.chahuyun.session.constant.Constant;
import cn.chahuyun.session.data.PermUser;
import cn.chahuyun.session.data.Scope;
import cn.chahuyun.session.perm.PermissionsServiceFactory;

/**
 * 权限工具
 *
 * @author Moyuyanli
 * @date 2024/3/22 16:35
 */
public class PermTool {

    private PermTool() {
    }

    public static void margePerm(Scope scope, PermUser permUser) {
        HuYanPermissionService permissionService = PermissionsServiceFactory.getInstance().getPermissionService();
        Constant.HUYAN_SESSION_PERM_LIST.forEach(it -> {
            if (permissionService.isPermissions(scope.getMarker(), it)) {
                mergePerm(it, permUser);
            }
        });
    }

    /**
     * 合并权限
     *
     * @param permCode 权限code
     * @param permUser 需要合并的权限
     */
    private static void mergePerm(String permCode, PermUser permUser) {
        switch (permCode) {
            case "admin":
                permUser.setAdmin(true);
                break;
            case "session":
                permUser.setSession(true);
                break;
            case "hh":
                permUser.setHh(true);
                break;
            case "dct":
                permUser.setDct(true);
                break;
            case "ds":
                permUser.setDs(true);
                break;
        }
    }

}
