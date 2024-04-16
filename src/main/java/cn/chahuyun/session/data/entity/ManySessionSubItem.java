package cn.chahuyun.session.data.entity;

import cn.chahuyun.session.data.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 多词条消息子信息
 *
 * @author Moyuyanli
 * @date 2024/1/8 16:54
 */
@Entity
@Getter
@Setter
@Table(name = "session_many_sub")
public class ManySessionSubItem extends BaseEntity {

    /**
     * 回复内容
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

    public ManySessionSubItem() {
    }

    public ManySessionSubItem(String reply) {
        this.reply = reply;
    }
}
