package cn.chahuyun.session.send;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.data.entity.Session;
import cn.chahuyun.session.data.entity.SingleSession;
import cn.chahuyun.session.enums.MatchTriggerType;
import cn.chahuyun.session.utils.ConversionTool;
import cn.hutool.core.date.DateUtil;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chahuyun.session.constant.Constant.DYNAMIC_PATTERN;
import static cn.chahuyun.session.constant.Constant.SINGLE_SESSION_DYNAMIC_PATTERN;

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
    private Session messageSource;

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
    public MessageChain replace() {
        if (messageSource instanceof SingleSession) {
            return MessageChain.deserializeFromJsonString(replace((SingleSession) messageSource));
        }
        return null;
    }


    private String replace(SingleSession singleSession) {
        if (!singleSession.isDynamic()) {
            return reply;
        }
        MessageChain messageChain = MessageChain.deserializeFromJsonString(reply);
        String content = ConversionTool.conversion(singleSession.getConversionType(), messageChain);

        Matcher matcher = Pattern.compile(DYNAMIC_PATTERN).matcher(content);
        MessageChainBuilder builder = new MessageChainBuilder();
        ArrayList<String> backflow = null;

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

        int nextStart = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String standard = matcher.group();
            String[] split = standard.replaceAll("[${}]", "").split("\\.");
            List<String> pattern = SINGLE_SESSION_DYNAMIC_PATTERN.get(split[0]);
            if (pattern == null) {
                continue;
            }
            for (String pt : pattern) {
                if (Pattern.matches(pt, split[1])) {
                    String substring = reply.substring(nextStart, start);
                    splicing(builder, substring, split[0], split[1], backflow);
                    nextStart = end;
                    break;
                }
            }
        }
        return MessageChain.serializeToJsonString(builder.build());
    }

    private void splicing(MessageChainBuilder builder, String reply, String prefix, String suffix, List<String> backflow) {
        builder.append(reply);
        User sender = event.getSender();
        Contact subject = event.getSubject();
        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        switch (prefix) {
            case "at":
                switch (suffix) {
                    case "this":
                        builder.append(new At(sender.getId()));
                        return;
                    case "all":
                        builder.append(AtAll.INSTANCE);
                        return;
                    default:
                        builder.append(new At(Long.parseLong(suffix)));
                        return;
                }
            case "user":
                switch (suffix) {
                    case "id":
                        builder.append(String.valueOf(sender.getId()));
                        return;
                    case "name":
                        builder.append(sender.getNick());
                        return;
                    case "avatar":
                        try {
                            builder.append(Contact.uploadImage(subject, new URL(sender.getAvatarUrl(AvatarSpec.MEDIUM)).openConnection().getInputStream()));
                        } catch (IOException e) {
                            builder.append("{头像获取错误}");
                        }
                        return;
                    case "title":
                        if (group != null) {
                            builder.append(Objects.requireNonNull(group.get(sender.getId())).getSpecialTitle());
                        } else {
                            builder.append("{当前环境无法匹配群}");
                        }
                        return;
                }
            case "group":
                if (group == null) {
                    builder.append("{当前环境无法匹配群}");
                    return;
                }
                switch (suffix) {
                    case "id":
                        builder.append(String.valueOf(group.getId()));
                        return;
                    case "name":
                        builder.append(group.getName());
                        return;
                    case "avatar":
                        builder.append(Contact.uploadImage(subject, new File(group.getAvatarUrl())));
                        return;
                    case "owner":
                        builder.append(group.getOwner().getNick());
                        return;
                }
            case "time":
                switch (suffix) {
                    case "now":
                        builder.append(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                        return;
                    case "timer":
                        builder.append(String.valueOf(System.currentTimeMillis()));
                        return;
                    default:
                        builder.append(DateUtil.format(new Date(), suffix));
                        return;
                }
            case "mate":
                if (backflow == null) {
                    builder.append("{正则回流匹配错误}");
                    return;
                }
                builder.append(backflow.get(Integer.parseInt(suffix)));
                return;
            case "message":
                MessageChain message = event.getMessage();
                switch (suffix) {
                    case "this":
                        builder.append(message);
                        return;
                    case "reverse":
                        Collections.reverse(message);
                        builder.append(message);
                        return;
                }
            case "owner":
                long owner = HuYanSession.pluginConfig.getOwner();
                Friend friend = event.getBot().getFriend(owner);
                if (friend == null) {
                    builder.append("{主人与机器人不是好友}");
                    return;
                }
                switch (suffix) {
                    case "id":
                        builder.append(String.valueOf(owner));
                        return;
                    case "name":
                        builder.append(friend.getNick());
                        return;
                    case "avatar":
                        builder.append(Contact.uploadImage(subject, new File(friend.getAvatarUrl())));
                }
        }

    }
}
