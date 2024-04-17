package cn.chahuyun.session.utils;

import cn.chahuyun.session.constant.Constant;
import cn.chahuyun.session.event.EventRegister;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * 消息工具
 *
 * @author Moyuyanli
 * @date 2024/4/16 10:08
 */
@Slf4j(topic = Constant.LOG_TOPIC)
public class MessageTool {

    private MessageTool() {
    }

    /**
     * 获取用户的下一条消息
     * <p/>
     * 3分钟超时时间
     * <p/>
     *
     * @param subject 载体
     * @param user    用户
     * @return MessageEvent 下一条消息
     * 可能为空，请做null判断
     */
    public static MessageEvent nextUserMessage(Contact subject, User user) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<MessageEvent> result = new AtomicReference<>();
        EventRegister.getGlobalEvent()
                .filterIsInstance(MessageEvent.class)
                .filter(filter -> filter.getSubject().getId() == subject.getId() && filter.getSender().getId() == user.getId())
                .subscribeOnce(MessageEvent.class, event -> {
                    result.set(event);
                    latch.countDown();
                });
        try {
            if (latch.await(3, TimeUnit.MINUTES)) {
                return result.get();
            } else {
                log.debug("获取用户下一条消息超时");
                return null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("获取用户下一条消息失败");
        }
    }


    /**
     * 是否退出
     * 消息事件为空也视为退出
     *
     * @param event 消息事件
     * @return true 退出
     */
    public static boolean isQuit(MessageEvent event) {
        if (event == null) return true;
        String content = event.getMessage().contentToString();
        return Pattern.matches("[!！]{3}", content);
    }

    /**
     * 是否保存
     *
     * @param event 消息事件
     * @return true 结束循环
     */
    public static boolean isSave(@NotNull MessageEvent event) {
        String content = event.getMessage().contentToString();
        return Pattern.matches("[!！]{2}", content);
    }

}
