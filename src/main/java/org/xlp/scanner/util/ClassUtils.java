package org.xlp.scanner.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 创建时间：2020年12月27日 下午6:24:30
 * </p>
 * 
 * @author xlp
 * @version 1.0
 * @Description 简化类的操作的工具类
 */
public class ClassUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);
	/**
	 * 返回要使用的默认类加载器：通常是线程上下文类加载器（如果可用）；
	 * 
	 * @return 返回默认类加载器（仅当系统类加载器不可访问时才返回null）
	 * @see Thread#getContextClassLoader()
	 * @see ClassLoader#getSystemClassLoader()
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
			// 当前线程上下文中获取类加载器失败
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("当前线程上下文中获取类加载器失败", ex); 
			}
		}
		if (cl == null) {
			// 无线程上下文类加载器->使用此类的类加载器
			cl = ClassUtils.class.getClassLoader();
			if (cl == null) {
				// getClassLoader（）返回null表示使用启动类加载器
				// ClassLoader
				try {
					cl = ClassLoader.getSystemClassLoader();
				} catch (Throwable ex) {
					// 获取系统类加载器失败
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("获取系统类加载器失败", ex); 
					}
				}
			}
		}
		return cl;
	}
}
