package cn.chahuyun.session.entity;

import lombok.Data;

/**
 * 基础类
 *
 * @author Moyuyanli
 * @date 2024/1/3 15:26
 */
@Data
public class BaseEntity implements ScopeAcquisition {

    private String scopeMarker;


    /**
     * 获取所属作用域
     *
     * @return 作用域
     */
    @Override
    public Scope getScope() {
        return null;
    }
}
