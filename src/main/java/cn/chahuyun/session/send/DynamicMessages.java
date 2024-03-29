package cn.chahuyun.session.send;

import cn.chahuyun.session.data.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * 用于识别匹配动态消息
 *
 * @author Moyuyanli
 * @date 2024/2/26 11:25
 */
public class DynamicMessages {

    private final String reply;

    @Setter
    @Getter
    private  BaseEntity messageSource;

    private final MessageEvent event;


    public DynamicMessages(String reply, MessageEvent event) {
        this.reply = reply;
        this.event = event;
    }

    public static boolean includeDynamic(String source) {
        return false;
    }

    /**
     * 替换动态消息回复
     * @return 替换后的结果
     */
    public String replace() {
        return reply;
    }

}
