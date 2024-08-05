package cn.chahuyun.session.event;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.constant.Constant;
import cn.chahuyun.session.data.PermUser;
import cn.chahuyun.session.data.Scope;
import cn.chahuyun.session.data.cache.Cache;
import cn.chahuyun.session.data.cache.CacheFactory;
import cn.chahuyun.session.data.entity.ManySession;
import cn.chahuyun.session.data.entity.SingleSession;
import cn.chahuyun.session.event.api.EventHanding;
import cn.chahuyun.session.event.permissions.PermissionsControl;
import cn.chahuyun.session.event.session.ManySessionControl;
import cn.chahuyun.session.event.session.SingleSessionControl;
import cn.chahuyun.session.send.DefaultSendMessage;
import cn.chahuyun.session.utils.MatchingTool;
import cn.chahuyun.session.utils.PermTool;
import cn.chahuyun.session.utils.TimingUtil;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.QuoteReply;
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
        if (HuYanSession.pluginConfig.getDevTool()) {
            TimingUtil.startTiming(Thread.currentThread().getName()+"-m");
        }
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
                        if (HuYanSession.pluginConfig.getDevTool()) {
                            log.debug("单一消息匹配用时:{}ns", TimingUtil.getResults(Thread.currentThread().getName()+"-m"));
                        }
                        DefaultSendMessage.create(singleSession, messageEvent).send();
                        if (!HuYanSession.pluginConfig.getMatchAll()) return;
                    }
                }
            }
        }

        List<Scope> matchManySessionScope = cacheService.getMatchManySessionScope();
        for (Scope scope : matchManySessionScope) {
            if (MatchingTool.matchScope(scope, subject, sender)) {
                List<ManySession> manySessionList = cacheService.getManySession(scope);
                for (ManySession manySession : manySessionList) {
                    if (MatchingTool.matchTrigger(manySession, messageChain)) {
                        if (HuYanSession.pluginConfig.getDevTool()) {
                            log.debug("多词条消息匹配用时:{}ns", TimingUtil.getResults(Thread.currentThread().getName()+"-m"));
                        }
                        DefaultSendMessage.create(manySession, messageEvent).send();
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
        if (HuYanSession.pluginConfig.getDevTool()) {
            TimingUtil.startTiming(Thread.currentThread().getName());
        }

        Contact subject = messageEvent.getSubject();
        User sender = messageEvent.getSender();
        MessageChain message = messageEvent.getMessage();
        String content = message.contentToString();

        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            if (bot.getId() == sender.getId()) {
                return;
            }
        }

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
            String studySimpleSession = "^%xx( +\\S+){2,7}|^学习( +\\S+){2,7}";
            String studyDialogueSimpleSession = "^%xx( +\\S+)?|^学习对话( +\\S+)?";
            String removeSimpleSession = "^-xx( +\\S+){1,2}|^删除( +\\S+){1,2}";
            String refreshSimpleSession = "^%%xx|^刷新多词条";
            if (Pattern.matches(studySimpleSession, content)) {
                log.debug("简单学习指令");
                SingleSessionControl.INSTANCE.studySimpleSingleSession(message, subject, sender);
                return;
            } else if (Pattern.matches(removeSimpleSession, content)) {
                log.debug("简单删除指令");
                SingleSessionControl.INSTANCE.removeSimpleSingleSession(message, subject, sender);
                return;
            } else if (Pattern.matches(studyDialogueSimpleSession, content)) {
                log.debug("对话学习指令");
                SingleSessionControl.INSTANCE.studyDialogue(message, subject, sender);
                return;
            } else if (Pattern.matches(refreshSimpleSession, content)) {
                log.debug("刷新对话指令");
                SingleSessionControl.INSTANCE.refresh(subject);
                return;
            } else if (message.contains(QuoteReply.Key) && (content.contains("删除") || content.contains("sc"))) {
                log.debug("删除对话指令");
                SingleSessionControl.INSTANCE.removeSimpleSingleSessionFormQuery(message,subject);
            }
        }

        boolean dct = owner || permUser.isAdmin() || permUser.isSession() || permUser.isDct();
        if (dct) {
            String refreshManySession = "%%dct|刷新多词条";
            String studyManySession = "^%dct( +\\S+)?|^学习多词条( +\\S+)?";
            String removeManySession = "-dct( +\\S+)+|^删除多词条( +\\S+)+";
            if (Pattern.matches(studyManySession, content)) {
                log.debug("学习多词条指令");
                ManySessionControl.INSTANCE.studyManySession(message, subject, sender);
                return;
            } else if (HuYanSession.pluginConfig.getGroupClassic() && message.contains(QuoteReply.Key) && content.contains("批准入典")) {
                log.debug("群典指令");
                ManySessionControl.INSTANCE.addGroupClassic(message, subject, sender);
                return;
            } else if (Pattern.matches(removeManySession, content)) {
                log.debug("删除多词条");
                ManySessionControl.INSTANCE.removeManySession(message, subject, sender);
                return;
            } else if (Pattern.matches(refreshManySession, content)) {
                log.debug("刷新多词条指令");
                ManySessionControl.INSTANCE.refresh(subject);
                return;
            } else if (message.contains(QuoteReply.Key) && (content.contains("删除") || content.contains("sc"))) {
                log.debug("删除多词条指令");
                ManySessionControl.INSTANCE.removeManySessionFormQuery(message,subject);
            }

        }

        boolean admin = owner || permUser.isAdmin();
        if (admin) {
            String addPermissions = "^\\+((global|members?|list|user)?([-@]{0,2}((?=-)\\S+|(\\d+?)))?)(?<=[0-9a-z]{7,})( +\\S{3,})+|添加权限((global|members?|list|user)?([-@]{0,2}((?=-)\\S+|(\\d+?)))?)(?<=[0-9a-z]{6,})( +\\S{3,})+";
            String removePermissions = "^-((global|members?|list|user)?([-@]{0,2}((?=-)\\S+|(\\d+?)))?)(?<=[0-9a-z]{7,})( +\\S{3,})+|删除权限((global|members?|list|user)?([-@]{0,2}((?=-)\\S+|(\\d+?)))?)(?<=[0-9a-z]{6,})( +\\S{3,})+";
            if (Pattern.matches(addPermissions, content)) {
                log.debug("添加权限指令");
                PermissionsControl.INSTANCE.addPermissions(message, subject, sender);
                return;
            } else if (Pattern.matches(removePermissions, content)) {
                log.debug("删除权限指令");
                PermissionsControl.INSTANCE.removePermissions(message, subject, sender);
                return;
            }
        }


        boolean group = owner || permUser.isAdmin();
        if (group) {
            String addGroupList = "^+group +(.*?) +\\d|添加群组 +(.*?) +\\d";
            if (Pattern.matches(addGroupList, content)) {
                log.debug("添加群组指令");

            }

        }

        //todo 匹配指令
        if (sender.getId() == 572490972 && content.lastIndexOf("!String") == 0) {
            subject.sendMessage(message.toString());
        } else if (sender.getId() == 572490972 && content.lastIndexOf("!miraicode") == 0) {
            subject.sendMessage(message.serializeToMiraiCode());
        } else if (sender.getId() == 572490972 && content.lastIndexOf("!json") == 0) {
            subject.sendMessage(MessageChain.serializeToJsonString(message));
        } else if (sender.getId() == 572490972 && content.lastIndexOf("!content") == 0) {
            subject.sendMessage(message.contentToString());
        }

    }


}
