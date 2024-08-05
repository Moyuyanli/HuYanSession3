package cn.chahuyun.session.data.factory;

import cn.chahuyun.api.data.api.DataSpecification;
import cn.chahuyun.session.data.cache.CacheFactory;
import cn.chahuyun.session.data.entity.*;
import lombok.val;
import org.hibernate.SessionFactory;

/**
 * 抽象数据操作
 *
 * @author Moyuyanli
 * @Date 2024/2/24 13:14
 */
public abstract class AbstractDataService implements DataSpecification {

    protected final SessionFactory sessionFactory;

    protected AbstractDataService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * 获取分组信息，通过分组名称
     *
     * @param name 分组名称
     * @return 分组信息
     */
    public GroupedLists getGroupedLists(String name) {
        GroupedLists groupedLists = selectResultEntity(GroupedLists.class, "from GroupedLists where name = '%s'", name);
        if (groupedLists == null) {
            throw new RuntimeException("根据分组名查询分组信息错误:结果为空!");
        }
        return groupedLists;
    }


    public void refresh() {
        val singleSessions = selectListEntity(SingleSession.class, "from SingleSession");
        val manySessions = selectListEntity(ManySession.class, "from ManySession");
        val timingSessions = selectListEntity(TimingSession.class, "from TimingSession");
        val permissions = selectListEntity(Permission.class, "from Permission");

        val cacheService = CacheFactory.getInstall().getCacheService();

        singleSessions.forEach(cacheService::putSession);
        manySessions.forEach(cacheService::putSession);
        timingSessions.forEach(cacheService::putSession);
        permissions.forEach(cacheService::putPermission);
    }

}
