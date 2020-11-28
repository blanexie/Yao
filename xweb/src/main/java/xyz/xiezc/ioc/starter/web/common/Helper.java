package xyz.xiezc.ioc.starter.web.common;

import java.io.File;
import java.util.regex.Matcher;

public class Helper {
	// 正则化文件路径
    public static String normalizePath(String path) {
        String result = path.replaceAll("/+", Matcher.quoteReplacement(File.separator));
        return result.replaceAll("\\\\+", Matcher.quoteReplacement(File.separator));
    }
	// 正则化URL路径
    public static String normalizeUrl(String path) {
        String result = path.replaceAll("\\\\+", "/");
        result = result.replaceAll("/+", "/");
        result = result.replaceAll("http:/+", "http://");
        result = result.replaceAll("https:/+", "https://");
        return result;
    }

    public static void main(String[] args) {
        String s = normalizeUrl("//dsfds//fhg43");
        System.out.println(s);
        String s1 = normalizePath("\\dsfds\\fhg43");
        System.out.println(s1);
    }
}