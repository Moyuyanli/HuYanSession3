package cn.chahuyun.session.send;

import cn.chahuyun.session.data.entity.*;
import cn.chahuyun.session.enums.SendType;
import cn.chahuyun.session.send.api.SendMessage;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.List;

/**
 * 发送消息
 *
 * @author Moyuyanli
 * @date 2024/2/26 11:24
 */
public class DefaultSendMessage implements SendMessage {

    private final ManySession manySession;
    private final TimingSession timingSession;
    private final MessageEvent messageEvent;
    private final SendType sendType;
    private final SingleSession singleSession;

    public DefaultSendMessage(SingleSession singleSession, MessageEvent messageEvent) {
        this.singleSession = singleSession;
        this.messageEvent = messageEvent;
        this.timingSession = null;
        this.manySession = null;
        this.sendType = SendType.SING;
    }

    public DefaultSendMessage(ManySession manySession, MessageEvent messageEvent) {
        this.manySession = manySession;
        this.messageEvent = messageEvent;
        this.timingSession = null;
        this.singleSession = null;
        this.sendType = SendType.MANY;
    }

    public DefaultSendMessage(TimingSession timingSession, MessageEvent messageEvent) {
        this.timingSession = timingSession;
        this.messageEvent = messageEvent;
        this.singleSession = null;
        this.manySession = null;
        this.sendType = SendType.TIMING;
    }

    public static <T extends Session> DefaultSendMessage create(T session, MessageEvent messageEvent) {
        if (session instanceof SingleSession) {
            SingleSession singleSession = (SingleSession) session;
            return new DefaultSendMessage(singleSession, messageEvent);
        }
        if (session instanceof TimingSession) {
            TimingSession timingSession = (TimingSession) session;
            return new DefaultSendMessage(timingSession, messageEvent);
        }
        if (session instanceof ManySession) {
            ManySession manySession = (ManySession) session;
            return new DefaultSendMessage(manySession, messageEvent);
        }
        throw new RuntimeException("创建默认发送消息集失败!");
    }


    /**
     * 发送消息，根据发送类型
     */
    @Override
    public void send() {
        switch (sendType) {
            case SING:
                sendSingMessage();
                break;
            case MANY:
                sendManyMessage();
                break;
            case TIMING:
                sendTimingMessage();
                break;
        }
    }

    /**
     * 发送单一消息
     */
    @SuppressWarnings("ConstantConditions")
    private void sendSingMessage() {
        String reply = singleSession.getReply();
        MessageChain replyMessageChain = MessageChain.deserializeFromJsonString(reply);

        if (singleSession.isDynamic()) {
            MessageChainBuilder chainBuilder = new MessageChainBuilder();

            DynamicMessages dynamicMessages = new DynamicMessages(reply, messageEvent);
            dynamicMessages.setMessageSource(singleSession);
            chainBuilder.append(dynamicMessages.replace());

            replyMessageChain = chainBuilder.build();
        }

        if (singleSession.isLocal()) {
            //todo 本地缓存
        }

        if (singleSession.getProbability() == 1.0 ||
                RandomUtil.randomInt(1, 100) <= singleSession.getProbability() * 100) {
            messageEvent.getSubject().sendMessage(replyMessageChain);
        }
    }

    /**
     * 发送多词条消息
     */
    @SuppressWarnings("ConstantConditions")
    private void sendManyMessage() {
        List<ManySessionSubItem> child = manySession.getChild();
        ManySessionSubItem sessionSubItem = manySession.nextMessage();

        String reply = sessionSubItem.getReply();
        MessageChain replyMessageChain = MessageChain.deserializeFromJsonString(reply);

        if (sessionSubItem.isDynamic()) {
            MessageChainBuilder chainBuilder = new MessageChainBuilder();

            DynamicMessages dynamicMessages = new DynamicMessages(reply, messageEvent);
            dynamicMessages.setMessageSource(manySession);
            chainBuilder.append(dynamicMessages.replace());

            replyMessageChain = chainBuilder.build();
        }

        if (sessionSubItem.isLocal()) {
            //todo 本地缓存
        }

        if (manySession.getProbability() == 1.0 ||
                RandomUtil.randomInt(1, 100) <= manySession.getProbability() * 100) {
            messageEvent.getSubject().sendMessage(replyMessageChain);
        }
    }

    private void sendTimingMessage() {

    }


}
