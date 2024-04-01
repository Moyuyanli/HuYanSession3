package cn.chahuyun.session.send;

import cn.chahuyun.session.data.BaseEntity;
import cn.chahuyun.session.data.entity.SingleSession;
import cn.chahuyun.session.enums.MatchTriggerType;
import cn.chahuyun.session.utils.ConversionTool;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chahuyun.session.constant.Constant.DYNAMIC_PATTERN;

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
    private BaseEntity messageSource;

    private final MessageEvent event;


    public DynamicMessages(String reply, MessageEvent event) {
        this.reply = reply;
        this.event = event;
    }

    /**
     * 是否包含动态消息
     *
     * @param source 源
     * @return true 包含动态消息
     */
    public static boolean includeDynamic(String source) {
        return Pattern.matches(DYNAMIC_PATTERN, source);
    }

    /**
     * 替换动态消息回复
     *
     * @return 替换后的结果
     */
    public String replace() {
        if (messageSource instanceof SingleSession) {
            return replace((SingleSession) messageSource);
        }
        return reply;
    }


    private String replace(SingleSession singleSession) {
        if (!singleSession.isDynamic()) {
            return reply;
        }
        MessageChain messageChain = MessageChain.deserializeFromJsonString(reply);
        String content = ConversionTool.conversion(singleSession.getConversionType(), messageChain);

        Matcher matcher = Pattern.compile(DYNAMIC_PATTERN).matcher(content);
        MessageChainBuilder builder = new MessageChainBuilder();
        ArrayList<String> backflow;

        // 正则回流
        if (singleSession.getMatchType() == MatchTriggerType.REGULAR) {
            backflow = new ArrayList<>();
            Matcher backflowMatcher = Pattern.compile(singleSession.getTrigger())
                    .matcher(ConversionTool.conversion(singleSession.getConversionType(), event.getMessage()));
            if (backflowMatcher.find()) {
                int count = backflowMatcher.groupCount();
                for (int i = 0; i <= count; i++) {
                    backflow.add(backflowMatcher.group(i));
                }
            }
        }


        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String standard = matcher.group();
            String[] split = standard.replaceAll("[${}]", "").split("\\.");


        }

    }


}
