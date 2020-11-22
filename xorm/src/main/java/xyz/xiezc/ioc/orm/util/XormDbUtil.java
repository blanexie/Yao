package xyz.xiezc.ioc.orm.util;

/**
 * @Description 相关数据库的工具类
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/23 4:27 下午
 **/
public class XormDbUtil {

    public static String getDbType(String nameContainsProductInfo) {
        if (nameContainsProductInfo.contains("mysql")) {
            return "mysql";
        } else if (nameContainsProductInfo.contains("oracle")) {
            return "oracle";
        } else if (nameContainsProductInfo.contains("postgresql")) {
            return "postgresql";
        } else if (nameContainsProductInfo.contains("sqlite")) {
            return "sqlite";
        } else if (nameContainsProductInfo.contains("sqlserver")) {
            return "sqlserver";
        } else if (nameContainsProductInfo.contains("hive")) {
            return "hive";
        } else if (nameContainsProductInfo.contains("h2")) {
            return "h2";
        } else if (nameContainsProductInfo.startsWith("jdbc:derby://")) {
            // Derby数据库网络连接方式
            return "derby";
        } else if (nameContainsProductInfo.contains("derby")) {
            // 嵌入式Derby数据库
            return "derby";
        } else if (nameContainsProductInfo.contains("hsqldb")) {
            // HSQLDB
            return "hsqldb";
        } else if (nameContainsProductInfo.contains("dm")) {
            // 达梦7
            return "dm";
        }
        return null;
    }
}