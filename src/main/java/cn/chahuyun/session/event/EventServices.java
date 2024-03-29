package cn.chahuyun.session.event;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.constant.Constant;
import cn.chahuyun.session.data.PermUser;
import cn.chahuyun.session.data.Scope;
import cn.chahuyun.session.data.cache.Cache;
import cn.chahuyun.session.data.cache.CacheFactory;
import cn.chahuyun.session.data.entity.SingleSession;
import cn.chahuyun.session.event.api.EventHanding;
import cn.chahuyun.session.event.permissions.PermissionsControl;
import cn.chahuyun.session.event.session.ManySessionControl;
import cn.chahuyun.session.event.session.SingleSessionControl;
import cn.chahuyun.session.send.DefaultSendMessage;
import cn.chahuyun.session.utils.MatchingTool;
import cn.chahuyun.session.utils.PermTool;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 事件service
 *
 * <p>构建时间: 2024/2/25 13:51</p>
 *
 * @author Moyuyanli
 */
@Slf4j(topic = Constant.LOG_TOPIC)
public class EventServices extends SimpleListenerHost implements EventHanding {

    public EventServices(@NotNull CoroutineContext coroutineContext) {
        super(coroutineContext);
    }


    /**
     * 匹配消息
     *
     * @param messageEvent 消息事件
     */
    @Override
    @EventHandler
    public void messageMatching(MessageEvent messageEvent) {
        MessageChain messageChain = messageEvent.getMessage();
        Contact subject = messageEvent.getSubject();
        User sender = messageEvent.getSender();


        Cache cacheService = CacheFactory.getInstall().getCacheService();
        List<Scope> mateSessionScope = cacheService.getMatchSingSessionScope();
        for (Scope scope : mateSessionScope) {
            if (MatchingTool.matchScope(scope, subject, sender)) {
                List<SingleSession> singleSessions = cacheService.getSingSession(scope);
                for (SingleSession singleSession : singleSessions) {
                    if (MatchingTool.matchTrigger(singleSession, messageChain)) {
                        new DefaultSendMessage(singleSession, messageEvent).send();
                        if (!HuYanSession.pluginConfig.getMatchAll()) return;
                    }
                }
            }
        }
    }

    /**
     * 匹配指令
     *
     * @param messageEvent 消息事件
     */
    @Override
    @EventHandler
    public void commandMatching(MessageEvent messageEvent) {
        Contact subject = messageEvent.getSubject();
        User sender = messageEvent.getSender();
        MessageChain message = messageEvent.getMessage();
        String content = message.contentToString();

        boolean owner = sender.getId() == HuYanSession.pluginConfig.getOwner();
        PermUser permUser = new PermUser();

        Cache cacheService = CacheFactory.getInstall().getCacheService();
        List<Scope> matchPermScope = cacheService.getMatchPermScope();

        if (!owner) {
            for (Scope scope : matchPermScope) {
                if (MatchingTool.matchScope(scope, subject, sender)) {
                    PermTool.margePerm(scope, permUser);
                }
            }
            if (!permUser.isExist()) {
                return;
            }
        }


        boolean hh = owner || permUser.isAdmin() || permUser.isSession() || permUser.isHh();
        if (hh) {
            String studySimpleSession = "^xx( +\\S+){2,7}|^学习( +\\S+){2,7}";
            String removeSimpleSession = "^-xx( +\\S+){1,2}|^删除( +\\S+){1,2}";
            if (Pattern.matches(studySimpleSession, content)) {
                log.debug("简单学习指令");
                SingleSessionControl.INSTANCE.studySimpleSingleSession(message, subject, sender);
                return;
            } else if (Pattern.matches(removeSimpleSession, content)) {
                log.debug("简单删除指令");
                SingleSessionControl.INSTANCE.removeSimpleSingleSession(message, subject, sender);
                return;
            }
        }

        boolean dct = owner || permUser.isAdmin() || permUser.isSession() || permUser.isDct();
        if (dct) {
            String studyManySession = "^%dct|^学习多词条";
            if (Pattern.matches(studyManySession, content)) {
                ManySessionControl.INSTANCE.studyManySession(message, subject, sender);
                return;
            }
        }

        boolean admin = owner || permUser.isAdmin();
        if (admin) {
            String addPermissions = "^\\+((global|members?|list|user)?([-@]{0,2}((?=-)\\S+|(\\d+?)))?)( +\\S+)+|添加权限((global|members?|list|user)?([-@]{0,2}((?=-)\\S+|(\\d+?)))?)( +\\S+)+";
            String removePermissions = "^\\-((global|members?|list|user)?([-@]{0,2}((?=-)\\S+|(\\d+?)))?)( +\\S+)+|删除权限((global|members?|list|user)?([-@]{0,2}((?=-)\\S+|(\\d+?)))?)( +\\S+)+";
            if (Pattern.matches(addPermissions, content)) {
                PermissionsControl.INSTANCE.addPermissions(message, subject, sender);
                return;
            }
        }

        //todo 匹配指令
        if (sender.getId() == 572490972 && content.lastIndexOf("!String") == 0) {
            subject.sendMessage(message.toString());
        }else if (sender.getId() == 572490972 && content.lastIndexOf("!miraicode") == 0) {
            subject.sendMessage(message.serializeToMiraiCode());
        } else if (sender.getId() == 572490972 && content.lastIndexOf("!json") == 0) {
            subject.sendMessage(MessageChain.serializeToJsonString(message));
        } else if (sender.getId() == 572490972 && content.lastIndexOf("!content") == 0) {
            subject.sendMessage(message.contentToString());
        }

    }


}
