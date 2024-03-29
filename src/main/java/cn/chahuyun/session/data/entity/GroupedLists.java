package cn.chahuyun.session.data.entity;

import cn.chahuyun.session.data.BaseEntity;
import cn.hutool.core.util.ArrayUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 分组信息
 *
 * @author Moyuyanli
 * @date 2024/1/8 10:57
 */
@Entity
@Getter
@Setter
@Table(name = "grouped_lists")
public class GroupedLists extends BaseEntity {


    /**
     * 分组类型
     * <li>1.群分组</li>
     * <li>2.用户分组</li>
     * <li>3.群员分组</li>
     */
    private Integer type;

    /**
     * 各类id转为字符串的值
     */
    @Column(name = "`values`")
    private String values;

    /**
     * 分组名称
     */
    private String name;
    /**
     * 各类id的list
     */
    @Transient
    private List<Long> valueList = new ArrayList<>();

    public GroupedLists() {
    }

    /**
     * 构建分组实体<p/>
     * 分组类型 :
     * <li>1.群分组</li>
     * <li>2.用户分组</li>
     * <li>3.群员分组</li>
     *
     * @param type      分组类型
     * @param name      分组名称
     * @param valueList 分组合并的值
     */
    public GroupedLists(Integer type, String name, List<Long> valueList) {
        this.type = type;
        this.name = name;
        this.setValueList(valueList);
    }

    public List<Long> getValueList() {
        for (String s : values.split(",")) {
            valueList.add(Long.parseLong(s));
        }
        return valueList;
    }

    public void setValueList(List<Long> valueList) {
        this.valueList = valueList;
        this.values = ArrayUtil.join(valueList, ",");
    }
}
