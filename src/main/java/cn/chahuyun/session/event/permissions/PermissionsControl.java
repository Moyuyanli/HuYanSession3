package cn.chahuyun.session.event.permissions;

import cn.chahuyun.session.constant.Constant;
import cn.chahuyun.session.data.ParameterSet;
import cn.chahuyun.session.data.Scope;
import cn.chahuyun.session.data.cache.Cache;
import cn.chahuyun.session.data.cache.CacheFactory;
import cn.chahuyun.session.data.entity.Permission;
import cn.chahuyun.session.data.factory.AbstractDataService;
import cn.chahuyun.session.data.factory.DataFactory;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

/**
 * 权限控制
 *
 * @author Moyuyanli
 * @date 2024/3/8 13:52
 */
public class PermissionsControl {

    public static final PermissionsControl INSTANCE = new PermissionsControl();


    private PermissionsControl() {
    }

    /**
     * 添加权限
     *
     * @param messages 消息
     * @param subject  发送载体
     * @param sender   发送者
     */
    public void addPermissions(MessageChain messages, Contact subject, User sender) {
        String content = messages.contentToString();
        String code = messages.serializeToMiraiCode();

        String[] split = code.split(" +");
        Scope scope = new Scope(Scope.Type.GLOBAL);
        ParameterSet parameterSet = new ParameterSet(scope, subject, split[0]);
        scope = parameterSet.getScope();

        Cache cacheService = CacheFactory.getInstall().getCacheService();


        MessageChainBuilder chainBuilder = new MessageChainBuilder();
        chainBuilder.append("为").append(scope.toString()).append("添加以下权限:\n");


        AbstractDataService dataService = DataFactory.getInstance().getDataService();


        int operation = 0;
        for (int i = 1; i < split.length; i++) {
            String s = split[i];
            if (Constant.HUYAN_SESSION_PERM_LIST.contains(s)) {
                Permission permission = new Permission();
                permission.setScope(scope);
                permission.setPermCode(s);
                if (dataService.mergeEntityStatus(permission)) {
                    cacheService.putPermission(permission);
                    chainBuilder.append(s).append("-成功\n");
                } else {
                    chainBuilder.append(s).append("-失败\n");
                }
                operation++;
            }
        }

        if (operation > 0) {
            subject.sendMessage(chainBuilder.build());
        } else {
            subject.sendMessage("没有识别的权限!");
        }

    }


}


