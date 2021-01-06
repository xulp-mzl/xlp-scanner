package org.xlp.scanner.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.xlp.assertion.AssertUtils;
import org.xlp.scanner.constants.ScannerPkgConsts;

/**
 * <p>创建时间：2020年12月29日 下午11:21:32</p>
 * @author xlp
 * @version 1.0 
 * @Description 提供通过url获取文件和URI的简化操作
*/
public class URLUtils {
	/**
	 * 通过URL获取文件
	 * 
	 * @param url 
	 * @return
	 * @throws NullPointerException 假如参数为空则抛出该异常
	 */
	public static File getFile(URL url) {
		AssertUtils.isNotNull(url, "URL param must not be null!");
		if (!ScannerPkgConsts.FILE_PROTOCOL.equals(url.getProtocol())) {
			throw new IllegalArgumentException("给定的URL无法解析为绝对文件路径: " + url);
		}
		try {
			return new File(toURI(url).getSchemeSpecificPart());
		} catch (URISyntaxException ex) {
			return new File(url.getFile());
		}
	}
	
	/**
	 * 用给定的URL创建URI
	 * 用“%20”编码替换URI的空格。
	 * @param url 要转换为URI实例的URL
	 * @return URI对象
	 * @throws URISyntaxException 假如给定的 URL是一个无效的URI时，则抛出该异常
	 * @see java.net.URL#toURI()
	 */
	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}

	/**
	 * 用给定的字符串创建URI
	 * 用“%20”编码替换URI的空格。
	 * @param location 要转换为URI实例的字符串
	 * @return URI对象
	 * @throws URISyntaxException 假如给定的 location是一个无效的URI时，则抛出该异常
	 */
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(location.replace(" ", "%20"));
	}
	
	/**
	 * 判断给定的URL是不是file协议
	 * 
	 * @param url
	 * @return 假如是file协议，返回true，否则返回false
	 */
	public static boolean isFileProtocol(URL url){
		if (url == null) {
			return false;
		}
		return ScannerPkgConsts.FILE_PROTOCOL.equals(url.getProtocol());
	}
	
	/**
	 * 判断给定的URL是不是Jar协议
	 * 
	 * @param url
	 * @return 假如是Jar协议，返回true，否则返回false
	 */
	public static boolean isJarProtocol(URL url){
		if (url == null) {
			return false;
		}
		String protocol = url.getProtocol();
		return (ScannerPkgConsts.JAR_PROTOCOL.equals(protocol) 
				|| ScannerPkgConsts.ZIP_PROTOCOL.equals(protocol) 
				|| ScannerPkgConsts.VFSZIP_PROTOCOL.equals(protocol)
				|| ScannerPkgConsts.WSJAR_PROTOCOL.equals(protocol));
	}
	
	/**
	 * 在给定的连接上设置“useCaches”标志，对于基于JNLP的资源，设置false，其他资源该标志保留原样
	 * 
	 * @param urlConnection
	 */
	public static void useCachesIfNecessary(URLConnection urlConnection){
		if (urlConnection != null) {
			urlConnection.setUseCaches(urlConnection.getClass().getSimpleName().startsWith("JNLP"));
		}
	}
}
