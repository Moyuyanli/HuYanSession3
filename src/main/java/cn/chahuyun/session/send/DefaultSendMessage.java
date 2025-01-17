package cn.chahuyun.session.send;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.constant.Constant;
import cn.chahuyun.session.data.entity.*;
import cn.chahuyun.session.enums.SendType;
import cn.chahuyun.session.send.api.SendMessage;
import cn.chahuyun.session.send.cache.SendCacheEntity;
import cn.chahuyun.session.send.cache.SendMessageCache;
import cn.chahuyun.session.utils.TimingUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;

import java.util.List;

/**
 * 发送消息
 *
 * @author Moyuyanli
 * @date 2024/2/26 11:24
 */
@Slf4j(topic = Constant.LOG_TOPIC)
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
    @SuppressWarnings("all")
    private void sendSingMessage() {
        Contact subject = messageEvent.getSubject();

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
            MessageChainBuilder chainBuilder = new MessageChainBuilder();

            for (SingleMessage singleMessage : replyMessageChain) {
                if (singleMessage instanceof Image) {
                    Image message = (Image) singleMessage;

                    LocalMessage localMessage = new LocalMessage(message, subject);
                    chainBuilder.append(localMessage.replace());

                } else {
                    chainBuilder.append(singleMessage);
                }
            }

            replyMessageChain = chainBuilder.build();
        }

        log.debug("单一消息id->" + singleSession.getId());

        if (singleSession.getProbability() == 1.0 ||
                RandomUtil.randomInt(1, 100) <= singleSession.getProbability() * 100) {
            MessageReceipt<Contact> message = subject.sendMessage(replyMessageChain);
            MessageSource messageSource = message.getSource();
            if (messageSource != null) {
                SendCacheEntity sendCacheEntity = new SendCacheEntity();
                sendCacheEntity.setType(0);
                sendCacheEntity.setMsgId(messageSource.getInternalIds()[0]);
                sendCacheEntity.setSessionId(singleSession.getId());
                SendMessageCache.getInstance().addSendMessage(sendCacheEntity);
            }
        }
        if (HuYanSession.pluginConfig.getDevTool()) {
            log.debug("单一消息发送用时:{}ns", TimingUtil.getResults(Thread.currentThread().getName() + "-m"));
            TimingUtil.cleanTiming(Thread.currentThread().getName() + "-m");
        }
    }

    /**
     * 发送多词条消息
     */
    @SuppressWarnings("all")
    private void sendManyMessage() {
        Contact subject = messageEvent.getSubject();

        List<ManySessionSubItem> child = manySession.getChild();
        ManySessionSubItem sessionSubItem = manySession.nextMessage();

        String reply = sessionSubItem.getReply();
        if (reply.equals("多词条消息为空")) return;
        MessageChain replyMessageChain = MessageChain.deserializeFromJsonString(reply);

        if (sessionSubItem.isDynamic()) {
            MessageChainBuilder chainBuilder = new MessageChainBuilder();

            DynamicMessages dynamicMessages = new DynamicMessages(reply, messageEvent);
            dynamicMessages.setMessageSource(manySession);
            chainBuilder.append(dynamicMessages.replace());

            replyMessageChain = chainBuilder.build();
        }

        if (sessionSubItem.isLocal()) {
            MessageChainBuilder chainBuilder = new MessageChainBuilder();

            for (SingleMessage singleMessage : replyMessageChain) {
                if (singleMessage instanceof Image) {
                    Image message = (Image) singleMessage;

                    LocalMessage localMessage = new LocalMessage(message, subject);
                    chainBuilder.append(localMessage.replace());

                } else {
                    chainBuilder.append(singleMessage);
                }
            }

            replyMessageChain = chainBuilder.build();
        }

        if (replyMessageChain.contains(ForwardMessage.Key)) {
            ForwardMessage forwardMessage = replyMessageChain.get(ForwardMessage.Key);
            List<ForwardMessage.Node> nodeList = forwardMessage.getNodeList();
            ForwardMessageBuilder forward = new ForwardMessageBuilder(subject, nodeList.size());
            for (ForwardMessage.INode iNode : nodeList) {
                forward.add(iNode);
            }
            forward.setDisplayStrategy(ForwardMessage.DisplayStrategy.Default);
            replyMessageChain = new MessageChainBuilder().append(forward.build()).build();
        }

        log.debug("多词条消息id->" + sessionSubItem.getId());

        if (manySession.getProbability() == 1.0 ||
                RandomUtil.randomInt(1, 100) <= manySession.getProbability() * 100) {
            MessageReceipt<Contact> message = subject.sendMessage(replyMessageChain);
            OnlineMessageSource.Outgoing source = message.getSource();
            if (source != null) {
                SendCacheEntity sendCacheEntity = new SendCacheEntity();
                sendCacheEntity.setType(1);
                sendCacheEntity.setMsgId(source.getIds()[0]);
                sendCacheEntity.setSessionId(manySession.getId());
                sendCacheEntity.setSessionSonId(sessionSubItem.getId());
                SendMessageCache.getInstance().addSendMessage(sendCacheEntity);
            }
        }
        if (HuYanSession.pluginConfig.getDevTool()) {
            log.debug("多词条消息发送用时:{}ns", TimingUtil.getResults(Thread.currentThread().getName() + "-m"));
            TimingUtil.cleanTiming(Thread.currentThread().getName() + "-m");
        }
    }

    private void sendTimingMessage() {

    }


}
