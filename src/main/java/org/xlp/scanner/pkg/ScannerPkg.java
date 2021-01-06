package org.xlp.scanner.pkg;

import java.io.IOException;
import java.util.Set;

/**
 * <p>
 * 创建时间：2020年12月19日 下午11:27:29
 * </p>
 * 
 * @author xlp
 * @version 1.0
 * @Description 包扫描接口
 */
public interface ScannerPkg {
	/**
	 * 扫描指定包下的class文件
	 * 
	 * @param packageName
	 *            包名称（xxx.xxx）
	 * @throws IOException
	 *             假如扫描失败，则抛出该异常
	 * @return 返回指定包下的class全称集合
	 */
	public Set<String> scanner(String packageName) throws IOException;

	/**
	 * 扫描指定包下的class文件
	 * 
	 * @param packageName
	 *            包名称（xxx.xxx）
	 * @throws IOException
	 *             假如扫描失败，则抛出该异常
	 * @return 返回指定包下的class集合
	 */
	public Set<Class<?>> scannerToClass(String packageName) throws IOException;
}
