package cn.chahuyun.session.data.factory;

import cn.chahuyun.session.constant.Constant;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

/**
 * 数据工具
 *
 * @author Moyuyanli
 * @date 2024/1/3 13:17
 */
@Slf4j(topic = Constant.LOG_TOPIC)
public class DataFactory {

    /**
     *
     *  获取数据工厂实例
     *
     * @return 数据工厂
     */

    @Getter
    private static DataFactory instance;

    /**
     *
     *  获取数据服务
     *
     * @return DataService 数据服务
     */

    @Getter
    private final AbstractDataService dataService;

    private DataFactory(SessionFactory sessionFactory) {
        this.dataService = createDataService(sessionFactory);
    }

    /**
     * 加载数据工厂，传入hibernate的session即可
     *
     * @param sessionFactory session
     */
    public static void dataFactoryLoad(SessionFactory sessionFactory) {
        instance = new DataFactory(sessionFactory);
        log.debug("数据库服务初始化完成!");
    }

    /**
     * 创建数据服务，使用默认的数据服务
     *
     * @param sessionFactory session
     * @return DataService 数据服务
     */
    private AbstractDataService createDataService(SessionFactory sessionFactory) {
        return new DefaultDataService(sessionFactory);
    }

}
