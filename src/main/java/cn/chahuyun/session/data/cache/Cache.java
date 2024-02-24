package cn.chahuyun.session.data.cache;

import cn.chahuyun.session.data.entity.ManySession;
import cn.chahuyun.session.data.entity.Permission;
import cn.chahuyun.session.data.entity.SingleSession;
import cn.chahuyun.session.data.entity.TimingSession;

/**
 * 缓存api
 *
 * @author Moyuyanli
 * @date 2024/1/15 11:29
 */
public interface Cache {

    /**
     * 缓存单一消息
     *
     * @param session 单一消息
     */
    void putSession(SingleSession session);

    /**
     * 缓存多词条消息
     *
     * @param session 多词条消息
     */
    void putSession(ManySession session);

    /**
     * 缓存定时消息
     *
     * @param session 定时消息
     */
    void putSession(TimingSession session);

    /**
     * 缓存权限信息
     *
     * @param permission 权限信息
     */
    void putPermission(Permission permission);

    /**
     * 获取单一消息
     *
     * @param id 消息id
     * @return 单一消息
     */
    SingleSession getSingSession(Integer id);

    /**
     * 获取多词条消息
     *
     * @param id 消息id
     * @return 多词条消息
     */
    ManySession getManySession(Integer id);

    /**
     * 获取定时消息
     *
     * @param id 消息id
     * @return 定时消息
     */
    TimingSession getTimingSession(Integer id);

    /**
     * 获取权限信息
     *
     * @param id 权限信息id
     * @return 权限信息
     */
    Permission getPermissions(Integer id);

    /**
     * 删除单一消息
     *
     * @param id 消息id
     */
    void removeSingSession(Integer id);

    /**
     * 删除多词条消息
     *
     * @param id 消息id
     */
    void removeManySession(Integer id);

    /**
     * 删除定时消息
     *
     * @param id 消息id
     */
    void removeTimingSession(Integer id);

    /**
     * 删除权限信息
     *
     * @param id 权限信息
     */
    void removePermissions(Integer id);


}
