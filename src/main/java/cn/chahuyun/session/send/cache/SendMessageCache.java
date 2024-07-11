package cn.chahuyun.session.send.cache;

import cn.chahuyun.session.HuYanSession;

/**
 * 发送消息缓存
 *
 * @author Moyuyanli
 * @date 2024/7/11 10:33
 */
public class SendMessageCache {

    private static final SendMessageCache INSTANCE = new SendMessageCache();

    /**
     * 发送消息缓存 消息id-发送缓存
     */
    private final ThreadSafeKeyValueQueue<Integer, SendCacheEntity> sendCacheMap;

    private SendMessageCache() {
        sendCacheMap = new ThreadSafeKeyValueQueue<>(HuYanSession.pluginConfig.getSendCacheQuantity());
    }

    public static SendMessageCache getInstance() {
        return INSTANCE;
    }

    public void addSendMessage(SendCacheEntity sendCacheEntity) {
        sendCacheMap.put(sendCacheEntity.getMsgId(), sendCacheEntity);
    }

    public SendCacheEntity getSendMessage(Integer msgId) {
        return sendCacheMap.containsKey(msgId) ? sendCacheMap.get(msgId) : null;
    }
}
