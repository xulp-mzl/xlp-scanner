package org.xlp.scanner.util;
/**
 * <p>创建时间：2020年12月25日 下午10:35:42</p>
 * @author xlp
 * @version 1.0 
 * @Description 字符串操作工具类
*/
public final class StringUtils {
	/**
     * "org.xlp.scanner" -> "org/xlp/scanner"
     * 
     * @param name 要转换的字符串
     * @return 假如参数为null，则返回null，否则返回转换后的字符串
     */
    public static String dotToSplash(String name) {
    	if (name != null) {
    		return name.replaceAll("\\.", "/");
		}
        return name;
    }
    
    /**
     * "org/xlp/scanner" -> "org.xlp.scanner"
     * 
     * @param name 要转换的字符串
     * @return 假如参数为null，则返回null，否则返回转换后的字符串
     */
    public static String splashToDot(String name) {
    	if (name != null) {
    		return name.replaceAll("/", ".");
		}
        return name;
    }
}
