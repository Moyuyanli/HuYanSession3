package cn.chahuyun.session.send;

import cn.chahuyun.session.data.entity.ManySession;
import cn.chahuyun.session.data.entity.SingleSession;
import cn.chahuyun.session.data.entity.TimingSession;
import cn.chahuyun.session.enums.SendType;
import cn.chahuyun.session.send.api.SendMessage;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;

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
    private SingleSession singleSession;

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
        this.sendType = SendType.MANY;
    }

    public DefaultSendMessage(TimingSession timingSession, MessageEvent messageEvent) {
        this.timingSession = timingSession;
        this.messageEvent = messageEvent;
        this.singleSession = null;
        this.manySession = null;
        this.sendType = SendType.TIMING;
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
            MessageChainBuilder chainBuilder = new MessageChainBuilder();
            for (SingleMessage singleMessage : replyMessageChain) {
                Image image = singleMessage instanceof Image ? ((Image) singleMessage) : null;
                if (image == null) {
                    chainBuilder.append(singleMessage);
                    continue;
                }
                chainBuilder.append(new LocalMessage(image).replace());
            }
            replyMessageChain = chainBuilder.build();
        }

        if (singleSession.getProbability() == 1.0 ||
                RandomUtil.randomInt(1, 100) <= singleSession.getProbability() * 100) {
            messageEvent.getSubject().sendMessage(replyMessageChain);
        }
    }

    private void sendManyMessage() {

    }

    private void sendTimingMessage() {

    }

}
