package org.xlp.scanner.item.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>创建时间：2021年1月2日 下午11:16:35</p>
 * @author xlp
 * @version 1.0 
 * @Description
*/
public class MethodUtils {
	/**
	 * 过滤出public方法
	 * 
	 * @param methods 要过滤的方法集合
	 * @return
	 */
	public static List<Method> filterPublicMethod(Method[] methods){
		List<Method> result = new ArrayList<Method>();
		if (methods == null) {
			return result;
		}
		for (Method method : methods) {
			if (method.getModifiers() == Modifier.PUBLIC) {
				result.add(method);
			}
		}
		return result;
	}
	
	
}
