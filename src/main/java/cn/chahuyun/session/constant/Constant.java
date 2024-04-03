package cn.chahuyun.session.constant;

import cn.chahuyun.session.data.Scope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 常量
 *
 * @author Moyuyanli
 * @date 2024/1/3 13:36
 */
public class Constant {
    /**
     * 日志前缀
     */
    public static final String LOG_TOPIC = "HuYanSession3";

    /**
     * 作用域类型的单参数集合
     */
    public static final List<Scope.Type> SCOPE_TYPE_SINGLE_PARAMETER = new ArrayList<>() {{
        add(Scope.Type.GLOBAL_USER);
        add(Scope.Type.GROUP);
        add(Scope.Type.LIST);
        add(Scope.Type.USERS);
    }};

    /**
     * 作用域类型的单参数集合<br>
     * 单参类型为 long
     */
    public static final List<Scope.Type> SCOPE_TYPE_SINGLE_PARAMETER_LONG = new ArrayList<>() {{
        add(Scope.Type.GLOBAL_USER);
        add(Scope.Type.GROUP);
    }};
    /**
     * 作用域类型的单参数集合<br>
     * 单参类型为 String
     */
    public static final List<Scope.Type> SCOPE_TYPE_SINGLE_PARAMETER_STRING = new ArrayList<>() {{
        add(Scope.Type.LIST);
        add(Scope.Type.USERS);
    }};

    /**
     * 壶言会话内置权限字符
     */
    public static final List<String> HUYAN_SESSION_PERM_LIST = new ArrayList<>() {{
        add("admin");
        add("session");
        add("hh");
        add("dct");
        add("ds");
    }};

    /**
     * 动态消息的匹配格式
     */
    public static final String DYNAMIC_PATTERN = "\\$\\{\\S+?\\.\\S+?}";

    public static final Map<String, List<String>> SINGLE_SESSION_DYNAMIC_PATTERN = new HashMap<>() {{
        put("at", new ArrayList<>() {{
            add("^\\d{6,11}$");
            add("this");
            add("all");
        }});
        put("user", new ArrayList<>() {{
            add("id");
            add("name");
            add("avatar");
            add("title");
        }});
        put("group", new ArrayList<>() {{
            add("id");
            add("name");
            add("avatar");
            //返回群主的名称
            add("owner");
        }});
        put("time", new ArrayList<>() {{
            add("now");
            add("[yMdDhHms-\\s\\.:：]+");
        }});
        put("mate", new ArrayList<>() {{
            add("\\d+");
        }});
        put("message", new ArrayList<>() {{
            add("this");
            add("reverse");
        }});
        put("owner", new ArrayList<>() {{
            add("id");
            add("name");
            add("avatar");
        }});
    }};

}
