package cn.chahuyun.session.utils;

import cn.chahuyun.session.enums.MessageConversionType;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * 转换工具
 *
 * @author Moyuyanli
 * @date 2024/4/1 17:29
 */
public class ConversionTool {


    private ConversionTool() {

    }


    public static String conversion(MessageConversionType type, MessageChain chain) {
        switch (type) {
            case MIRAI_CODE:
            default:
                return chain.serializeToMiraiCode();
            case STRING:
                return chain.toString();
            case CONTENT:
                return chain.contentToString();
            case JSON:
                return MessageChain.serializeToJsonString(chain);
        }
    }


}
