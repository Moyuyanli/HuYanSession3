package cn.chahuyun.session.data.entity;

import cn.chahuyun.session.data.BaseEntity;
import cn.chahuyun.session.enums.MatchTriggerType;
import cn.chahuyun.session.enums.MessageConversionType;
import cn.chahuyun.session.enums.SessionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 会话信息
 *
 * @author Moyuyanli
 * @date 2024/1/3 15:25
 */
@Entity
@Getter
@Setter
@Table(name = "session_single")
public class SingleSession extends BaseEntity {

    /**
     * 触发词
     */
    @Column(name = "`trigger`")
    private String trigger;
    /**
     * 回复词
     */
    private String reply;
    /**
     * 是否为动态消息
     */
    private boolean dynamic;
    /**
     * 是否为本地缓存
     */
    private boolean local;
    /**
     * 概率触发
     */
    private Double probability;

    /**
     * 匹配方式
     */
    @Enumerated(EnumType.STRING)
    private MatchTriggerType triggerType;
    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    /**
     * 转换类型
     */
    @Enumerated(EnumType.STRING)
    private MessageConversionType conversionType;

    public SingleSession() {
    }


    public void setProbability(Double probability) {
        if (probability != null) {
            this.probability = Math.min(Math.max(probability, 0.0), 1.0);
        } else {
            this.probability = 1.0;
        }
    }

    public void setConversionType(MessageConversionType conversionType) {
        this.conversionType = Objects.requireNonNullElse(conversionType, MessageConversionType.MIRAI_CODE);
    }
}
