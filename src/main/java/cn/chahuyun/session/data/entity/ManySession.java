package cn.chahuyun.session.data.entity;

import cn.chahuyun.session.data.BaseEntity;
import cn.chahuyun.session.enums.MatchTriggerType;
import cn.hutool.core.util.RandomUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 多词条消息
 *
 * @author Moyuyanli
 * @date 2024/1/8 16:54
 */
@Entity
@Getter
@Setter
@Table(name = "session_many")
public class ManySession extends BaseEntity implements Session {

    /**
     * 触发词
     */
    @Column(name = "`trigger`")
    private String trigger;

    /**
     * 下一条消息的id
     */
    private Integer nextSub = 0;

    /**
     * 是否随机
     * true 随机
     */
    private Boolean random;

    /**
     * 概率触发
     */
    private Double probability;

    /**
     * 匹配方式
     */
    @Enumerated(EnumType.STRING)
    private MatchTriggerType matchType;


    /**
     * 多词条消息的子项
     * <p>
     * <li>cascade 关联类型:全部(保存，更新，删除)</li>
     * <li>orphanRemoval  当子项的父级被删除时，删除所有孤儿状态子项(无父级)</li>
     * <li>fetch  查询加载方式:立即加载</li>
     * <li>JoinColumn  添加外外键名称</li>
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "many_session_id")
    private List<ManySessionSubItem> child;

    public void setProbability(Double probability) {
        if (probability != null) {
            this.probability = Math.min(Math.max(probability, 0.0), 1.0);
        } else {
            this.probability = 1.0;
        }
    }


    public void add(ManySessionSubItem item) {
        if (child == null) {
            child = new ArrayList<>() {{
                add(item);
            }};
        } else {
            child.add(item);
        }
    }


    public void addAll(Collection<ManySessionSubItem> collection) {
        if (child == null) {
            child = new ArrayList<>(collection);
        } else {
            child.addAll(collection);
        }
    }

    /**
     * 根据多词条信息获取下一条消息
     *
     * @return 下一条消息
     */
    public ManySessionSubItem nextMessage() {
        if (child == null) throw new RuntimeException("多词条消息加载失败!");
        if (child.isEmpty()) return new ManySessionSubItem("多词条消息为空");
        if (random) {
            nextSub = RandomUtil.randomInt(0, child.size());
        }
        return child.get(nextSub++ % child.size());
    }


}
