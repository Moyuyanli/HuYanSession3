package cn.chahuyun.session.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 计时工具
 *
 * @author Moyuyanli
 * @date 2024/7/22 9:26
 */
public class TimingUtil {

    private static final Map<String, Long> timingMap = new HashMap<>(4);


    /**
     * 开始计时
     *
     * @param id 计时id
     */
    public synchronized static void startTiming(String id) {
        timingMap.put(id, System.nanoTime());
    }


    /**
     * 获取结果
     *
     * @param id 计时id
     * @return 执行纳秒
     */
    public synchronized static Integer getResults(String id) {
        long l = System.nanoTime();
        if (timingMap.containsKey(id)) {
            return Math.toIntExact(l - timingMap.get(id)) / 100;
        } else {
            throw new RuntimeException("id does not exist !");
        }
    }


    /**
     * 清除计时器
     *
     * @param id 计时id
     */
    public static void cleanTiming(String id) {
        timingMap.remove(id);
    }

}
