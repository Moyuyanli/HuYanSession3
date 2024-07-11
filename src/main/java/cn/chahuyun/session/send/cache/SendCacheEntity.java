package cn.chahuyun.session.send.cache;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Moyuyanli
 * @date 2024/7/11 10:35
 */
@Getter
@Setter
public class SendCacheEntity {


    private Integer msgId;
    private Date time = new Date();
    /**
     * 0-单一消息
     * 1-多词条消息
     * 2-定时消息
     */
    private Integer type;
    private Integer sessionId;
    private Integer sessionSonId;


}
