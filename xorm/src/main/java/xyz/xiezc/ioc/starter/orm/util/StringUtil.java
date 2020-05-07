package xyz.xiezc.ioc.starter.orm.util;

public class StringUtil {

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static final String EMPTY = "";

    /**
     * 字符串首字母小写写
     *
     * @param camelCaseName
     * @return
     */
    public static String lownCaseFirstChar(String camelCaseName) {
        if (isNullOrEmpty(camelCaseName)) {
            return EMPTY;
        }
        char c = camelCaseName.charAt(0);
        char c1 = Character.toLowerCase(c);
        return c1 + camelCaseName.substring(1);
    }

    /**
     * 字符串首字母大写
     *
     * @param camelCaseName
     * @return
     */
    public static String upCaseFirstChar(String camelCaseName) {
        if (isNullOrEmpty(camelCaseName)) {
            return EMPTY;
        }
        char c = camelCaseName.charAt(0);
        char c1 = Character.toUpperCase(c);
        return c1 + camelCaseName.substring(1);
    }

    /**
     * 安全的进行字符串 format
     *
     * @param target 目标字符串
     * @param params format 参数
     * @return format 后的
     */
    public static String format(String target, Object... params) {

        if (target.contains("%s") && params != null && params.length > 0) {
            return String.format(target, params);
        }
        return target;
    }

    /**
     * 驼峰转换为下划线
     *
     * @param camelCaseName
     * @return
     */
    public static String underscoreName(String camelCaseName) {
        StringBuilder result = new StringBuilder();
        if (camelCaseName != null && camelCaseName.length() > 0) {
            result.append(camelCaseName.substring(0, 1).toLowerCase());
            for (int i = 1; i < camelCaseName.length(); i++) {
                char ch = camelCaseName.charAt(i);
                if (Character.isUpperCase(ch)) {
                    result.append("_");
                    result.append(Character.toLowerCase(ch));
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    /**
     * 下划线转换为驼峰
     *
     * @param underscoreName
     * @return
     */
    public static String camelCaseName(String underscoreName) {
        StringBuilder result = new StringBuilder();
        if (underscoreName != null && underscoreName.length() > 0) {
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ("_".charAt(0) == ch) {
                    flag = true;
                } else {
                    if (flag) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }

}