package cn.chahuyun.session.event.session;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.constant.Constant;
import cn.chahuyun.session.data.ParameterSet;
import cn.chahuyun.session.data.Scope;
import cn.chahuyun.session.data.cache.Cache;
import cn.chahuyun.session.data.cache.CacheFactory;
import cn.chahuyun.session.data.entity.ManySession;
import cn.chahuyun.session.data.entity.ManySessionSubItem;
import cn.chahuyun.session.data.factory.AbstractDataService;
import cn.chahuyun.session.data.factory.DataFactory;
import cn.chahuyun.session.enums.MatchTriggerType;
import cn.chahuyun.session.send.DynamicMessages;
import cn.chahuyun.session.utils.AnswerTool;
import cn.chahuyun.session.utils.MessageTool;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.QuoteReply;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateRecorder;
import xyz.cssxsh.mirai.hibernate.entry.MessageRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * 多词条控制
 *
 * <p>构建时间: 2024/3/1 20:13</p>
 *
 * @author Moyuyanli
 */
@Slf4j(topic = Constant.LOG_TOPIC)
public class ManySessionControl {

    public static final ManySessionControl INSTANCE = new ManySessionControl();

    private ManySessionControl() {
    }

    public void studyManySession(MessageChain messages, Contact subject, User sender) {
        String trigger = messages.serializeToMiraiCode();

        subject.sendMessage("请输入参数:");
        MessageEvent event = MessageTool.nextUserMessage(subject, sender);
        if (MessageTool.isQuit(event)) {
            return;
        }

        String[] params = event.getMessage().contentToString().split(" ");
        ParameterSet parameterSet = new ParameterSet(Scope.global(), subject, params);

        Scope scope = parameterSet.getScope();

        Cache cacheService = CacheFactory.getInstall().getCacheService();
        List<ManySession> manySessions = cacheService.getManySession(scope);

        ManySession manySession = new ManySession();

        for (ManySession session : manySessions) {
            if (session.getTrigger().equals(trigger)) {
                manySession = session;
                if (parameterSet.isRewrite()) {
                    manySession.setProbability(parameterSet.getProbability());
                    manySession.setMatchType(parameterSet.getMatchTriggerType());
                    manySession.setScope(parameterSet.getScope());
                    manySession.setRandom(parameterSet.isRandom());
                }
            }
        }

        if (manySession.getId() == null) {
            manySession.setProbability(parameterSet.getProbability());
            manySession.setMatchType(parameterSet.getMatchTriggerType());
            manySession.setScope(parameterSet.getScope());
            manySession.setRandom(parameterSet.isRandom());
        }


        List<ManySessionSubItem> items = new ArrayList<>();

        while (true) {
            subject.sendMessage("请输入回复内容(双‘!’保存，三‘！’退出！)");
            event = MessageTool.nextUserMessage(subject, sender);
            if (MessageTool.isQuit(event)) return;
            if (MessageTool.isSave(event)) break;

            ManySessionSubItem manySessionSubItem = new ManySessionSubItem();

            String jsonString = MessageChain.serializeToJsonString(event.getMessage());
            manySessionSubItem.setReply(jsonString);

            String string = event.getMessage().contentToString();
            boolean dynamic = DynamicMessages.includeDynamic(string);

            manySessionSubItem.setDynamic(dynamic);

            if (HuYanSession.pluginConfig.getLocalCache()) {
                //todo  图片缓存
            } else {
                manySessionSubItem.setLocal(false);
            }

            items.add(manySessionSubItem);
        }

        manySession.addAll(items);

        AbstractDataService dataService = DataFactory.getInstance().getDataService();

        boolean add = manySession.getId() == null;

        if (dataService.mergeEntityStatus(manySession)) {
            cacheService.putSession(manySession);
            if (add) {
                subject.sendMessage(AnswerTool.getAnswer(HuYanSession.answerConfig.getStudySuccess()));
            } else {
                subject.sendMessage(AnswerTool.getAnswer(HuYanSession.answerConfig.getUpdateSuccess()));
            }
        } else {
            subject.sendMessage(AnswerTool.getAnswer(HuYanSession.answerConfig.getStudyFailed()));
        }
    }

    /**
     * 群典添加法
     *
     * @param messages 消息
     * @param subject  载体
     * @param sender   发送着
     */
    public void  addGroupClassic(MessageChain messages, Contact subject, User sender) {
        QuoteReply quoteReply = messages.get(QuoteReply.Key);

        List<MessageRecord> messageRecords;
        if (quoteReply != null) {
            messageRecords = MiraiHibernateRecorder.INSTANCE.get(quoteReply.getSource());
        } else {
            log.error("群典功能获取引用消息失败");
            return;
        }

        if (messageRecords.isEmpty()) {
            log.error("群典功能获取引用消息失败");
            return;
        }

        MessageChain originalMessage = messageRecords.get(0).toMessageChain();

        String reply = MessageChain.serializeToJsonString(originalMessage);
        boolean dynamic = DynamicMessages.includeDynamic(quoteReply.contentToString());

        ManySessionSubItem manySessionSubItem = new ManySessionSubItem();
        manySessionSubItem.setReply(reply);
        manySessionSubItem.setLocal(false);
        manySessionSubItem.setDynamic(dynamic);

        ManySession manySession = new ManySession();
        Scope scope = Scope.group(subject);

        Cache cacheService = CacheFactory.getInstall().getCacheService();
        List<ManySession> manySessionList = cacheService.getManySession(scope);

        for (ManySession session : manySessionList) {
            if (session.getTrigger().equals("群典")) {
                manySession = session;
            }
        }

        if (manySession.getId() == null) {
            manySession.setScope(scope);
            manySession.setTrigger("群典");
            manySession.setProbability(1.0);
            manySession.setRandom(false);
            manySession.setMatchType(MatchTriggerType.PRECISION);
        }

        manySession.add(manySessionSubItem);

        AbstractDataService dataService = DataFactory.getInstance().getDataService();

        if (dataService.mergeEntityStatus(manySession)) {
            cacheService.putSession(manySession);
            subject.sendMessage("入典成功!");
        } else {
            subject.sendMessage("入典失败!");
        }
    }

}
