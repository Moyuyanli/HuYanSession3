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
import cn.chahuyun.session.send.LocalMessage;
import cn.chahuyun.session.utils.AnswerTool;
import cn.chahuyun.session.utils.MessageTool;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateRecorder;
import xyz.cssxsh.mirai.hibernate.entry.MessageRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 多词条控制
 *
 * <p>构建时间: 2024/3/1 20:13</p>
 *
 * @author Moyuyanli
 */
@val
@Slf4j(topic = Constant.LOG_TOPIC)
public class ManySessionControl {

    public static final ManySessionControl INSTANCE = new ManySessionControl();

    private ManySessionControl() {
    }

    public void studyManySession(MessageChain messages, Contact subject, User sender) {
        String[] split = messages.serializeToMiraiCode().split(" ");
        String trigger;
        if (split.length == 1) {
            subject.sendMessage("请输入触发词:");
            MessageEvent event = MessageTool.nextUserMessage(subject, sender);
            if (MessageTool.isQuit(event)) {
                return;
            }
            trigger = event.getMessage().serializeToMiraiCode();
        } else {
            trigger = split[1];
        }

        subject.sendMessage("请输入参数:");
        MessageEvent event = MessageTool.nextUserMessage(subject, sender);
        if (MessageTool.isQuit(event)) {
            return;
        }

        String[] params = event.getMessage().contentToString().split(" ");
        ParameterSet parameterSet = new ParameterSet(Scope.group(subject), subject, params);

        if (parameterSet.isException()) {
            subject.sendMessage(parameterSet.getExceptionMsg());
            return;
        }

        Scope scope = parameterSet.getScope();

        Cache cacheService = CacheFactory.getInstall().getCacheService();
        List<ManySession> manySessions = cacheService.getManySession(scope);

        ManySession manySession = new ManySession();
        manySession.setTrigger(trigger);

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
                if (!LocalMessage.localCacheImage(event.getMessage())) {
                    subject.sendMessage("添加失败，图片缓存失败,请重新添加！");
                    continue;
                }
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
    public void addGroupClassic(MessageChain messages, Contact subject, User sender) {
        if (!(subject instanceof Group)) {
            return;
        }
        Group group = (Group) subject;

        QuoteReply quoteReply = messages.get(QuoteReply.Key);

        List<MessageRecord> messageRecords;
        if (quoteReply != null) {
            MessageSource source = quoteReply.getSource();
            messageRecords = MiraiHibernateRecorder.INSTANCE.get(source);
        } else {
            log.error("群典功能获取引用消息失败");
            return;
        }

        if (messageRecords.isEmpty()) {
            log.error("群典功能获取引用消息失败");
            return;
        }

        MessageRecord messageRecord = messageRecords.get(0);

        String nick = Objects.requireNonNull(group.get(messageRecord.getFromId())).getNick();

        MessageChain originalMessage = messageRecord.toMessageChain();

        boolean isPlain = true;

        for (SingleMessage singleMessage : originalMessage) {
            if (!(singleMessage instanceof Face) &&
                    !(singleMessage instanceof PlainText) &&
                    !(singleMessage instanceof At)
            ) {
                isPlain = false;
                break;
            }
        }

        if (isPlain) {
            MessageChainBuilder builder = new MessageChainBuilder().append(new PlainText(String.format("%s说:", nick)));
            builder.addAll(originalMessage);
            originalMessage = builder.build();
        }

        String reply = MessageChain.serializeToJsonString(originalMessage);
        boolean dynamic = DynamicMessages.includeDynamic(quoteReply.contentToString());


        ManySessionSubItem manySessionSubItem = new ManySessionSubItem();
        manySessionSubItem.setReply(reply);

        if (HuYanSession.pluginConfig.getLocalCache()) {
            if (!LocalMessage.localCacheImage(originalMessage)) {
                subject.sendMessage("入典失败!图片缓存失败!");
                return;
            } else {
                manySessionSubItem.setLocal(true);
            }
        } else {
            manySessionSubItem.setLocal(false);
        }

        manySessionSubItem.setDynamic(dynamic);

        ManySession manySession = new ManySession();
        Scope scope = Scope.group(subject);

        Cache cacheService = CacheFactory.getInstall().getCacheService();
        List<ManySession> manySessionList = cacheService.getManySession(scope);

        String groupClassicName = HuYanSession.pluginConfig.getGroupClassicName();

        for (ManySession session : manySessionList) {
            if (session.getTrigger().equals(groupClassicName)) {
                manySession = session;
            }
        }

        manySession.add(manySessionSubItem);

        if (manySession.getId() == null) {
            manySession.setScope(scope);
            manySession.setTrigger(groupClassicName);
            manySession.setProbability(1.0);
            manySession.setRandom(false);
            manySession.setMatchType(MatchTriggerType.PRECISION);
        }

        AbstractDataService dataService = DataFactory.getInstance().getDataService();

        ManySession mergeEntity = dataService.mergeEntity(manySession);
        if (mergeEntity.getId() != null) {
            subject.sendMessage("入典成功!");
            cacheService.putSession(mergeEntity);
        } else {
            subject.sendMessage("入典失败!");
        }
    }

    /**
     * 删除多词条
     * -dct (trigger|id-) (id)?
     *
     * @param messages 消息
     * @param subject  载体
     * @param sender   发送者
     */
    public void removeManySession(MessageChain messages, Contact subject, User sender) {
        String code = messages.serializeToMiraiCode();
        String[] split = code.split(" +");

        int id;

        Cache cacheService = CacheFactory.getInstall().getCacheService();
        Scope scope = Scope.group(subject);

        Boolean type = null;

        ManySession manySession = null;
        if (split[1].contains("id-")) {
            id = Integer.parseInt(split[1].replace("id-", ""));
            manySession = cacheService.getManySession(id);
            type = false;
        } else {
            List<ManySession> manySessions = cacheService.getManySession(scope);
            for (ManySession session : manySessions) {
                if (session.getTrigger().equals(split[1])) {
                    manySession = session;
                    type = true;
                }
            }
            if (type == null) {
                subject.sendMessage("没有该触发词的多词条集!");
                return;
            }
        }

        AbstractDataService dataService = DataFactory.getInstance().getDataService();

        if (type || split.length == 2) {
            subject.sendMessage("请输入双‘!’确认删除多词条集!");
            MessageEvent nextUserMessage = MessageTool.nextUserMessage(subject, sender);
            String content;
            if (nextUserMessage != null) {
                content = nextUserMessage.getMessage().contentToString();
            } else {
                return;
            }
            if (content.equals("!!") || content.equals("！！")) {
                if (dataService.deleteEntity(manySession)) {
                    cacheService.removeManySession(manySession.getId());
                    subject.sendMessage(AnswerTool.getAnswer(HuYanSession.answerConfig.getRemoveSuccess()));
                } else {
                    subject.sendMessage(AnswerTool.getAnswer(HuYanSession.answerConfig.getRemoveFailed()));
                }
            }
            return;
        }


        val items = new ArrayList<ManySessionSubItem>();
        for (int i = 2; i < split.length; i++) {
            int index = i;
            manySession.getChild().forEach(item -> {
                if (item.getId() == Integer.parseInt(split[index]))
                    items.add(item);
            });
        }

        int success = 0;
        int failed = 0;
        for (ManySessionSubItem item : items) {
            if (dataService.deleteEntity(item)) {
                manySession.getChild().remove(item);
                cacheService.putSession(manySession);
                success++;
            } else {
                failed++;
            }
        }

        MessageChainBuilder builder = new MessageChainBuilder();
        builder.append("对于多词条集 '").append(manySession.getTrigger()).append("'子集进行删除:\n")
                .append("成功:").append(String.valueOf(success)).append("条\n")
                .append("失败:").append(String.valueOf(failed)).append("条");

        subject.sendMessage(builder.build());
    }


    /**
     * 刷新内存中的缓存
     *
     * @param subject 消息载体
     */
    public void refresh(Contact subject) {
        AbstractDataService dataService = DataFactory.getInstance().getDataService();
        Cache cacheService = CacheFactory.getInstall().getCacheService();
        List<ManySession> manySessions = dataService.selectListEntity(ManySession.class, "from ManySession");
        manySessions.forEach(cacheService::putSession);
        subject.sendMessage("多词条缓存刷新成功!");
    }

}
