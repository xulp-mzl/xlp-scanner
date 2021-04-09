package org.xlp.scanner.pkg;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.scanner.constants.ScannerPkgConsts;
import org.xlp.scanner.util.ClassUtils;
import org.xlp.scanner.util.StringUtils;
import org.xlp.scanner.util.URLUtils;
import org.xlp.utils.XLPStringUtil;
import org.xlp.utils.XLPSystemParamUtil;

/**
 * <p>
 * 创建时间：2020年12月23日 下午10:39:00
 * </p>
 * 
 * @author xlp
 * @version 1.0
 * @Description 获取classpath下指定包下的class
 */
public class ClassPathPkgScanner implements ScannerPkg {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathPkgScanner.class);

	/**
	 * 类加载器
	 */
	private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

	/**
	 * 构造函数
	 */
	public ClassPathPkgScanner() {
	}

	/**
	 * 构造函数
	 * 
	 * @param classLoader
	 *            类加载器
	 */
	public ClassPathPkgScanner(ClassLoader classLoader) {
		if (classLoader != null) {
			this.classLoader = classLoader;
		}
	}

	/**
	 * @param packageName
	 *            包名称
	 * @throws IOException
	 *             假如解析包名失败，则抛出该异常
	 */
	@Override
	public Set<String> scanner(String packageName) throws IOException {
		Set<String> classSet = new HashSet<String>();
		packageName = XLPStringUtil.emptyTrim(packageName);
		
		//假如传过来的包名为空，则扫描类路径下的所有类
		if (XLPStringUtil.isEmpty(packageName)) { 
			String classPath = XLPSystemParamUtil.getJavaClassPath();
			//获取所有的类路径，包括jar文件
			String[] classPaths = classPath.split(XLPSystemParamUtil.getPathSeparator());
			File tempFile;
			for (String cp : classPaths) {
				tempFile = new File(cp);
				//判断是否是jar文件
				if (tempFile.isFile() && cp.endsWith(ScannerPkgConsts.JAR_FILE_EXT)) {
					classSet.addAll(findByJarFile(new JarFile(tempFile)));
				} else if (tempFile.isDirectory()) {
					//获取所有的子文件或文件夹
					File[] childrenFile = tempFile.listFiles();
					if (childrenFile != null) {
						for (File childFile : childrenFile) {
							// 以文件的方式扫描整个包下的文件 并添加到集合中
							findClassesInPackageByFile(XLPStringUtil.EMPTY, childFile.getAbsolutePath(), classSet);
						}
					}
				}
			}
		} else {
			//扫描自定包下的所有类
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("开始扫描该" + packageName + "下的所有class文件");
			}
			
			String packagePath = StringUtils.dotToSplash(packageName);
			Enumeration<URL> resourceUrls = (classLoader != null ? classLoader.getResources(packagePath)
					: ClassLoader.getSystemResources(packagePath));
			boolean isDebug = LOGGER.isDebugEnabled();
			while (resourceUrls.hasMoreElements()) {
				URL packageUrl = (URL) resourceUrls.nextElement();
				if (isDebug) {
					LOGGER.debug("开始扫描：" + packageUrl + "中的class文件"); 
				}
	
				// 如果是以文件的形式保存在服务器上
				if (URLUtils.isFileProtocol(packageUrl)) {
					// file类型的扫描
					File file = URLUtils.getFile(packageUrl);
					// 获取包的物理路径
					String filePath = file.getAbsolutePath();
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findClassesInPackageByFile(packageName, filePath, classSet);
				} else if (URLUtils.isJarProtocol(packageUrl)) { 
					findClassesInJarFile(packageName, packageUrl, classSet); 
				}
			}
		}
		return classSet;
	}

	/**
	 * 扫描jar文件中的class
	 * 
	 * @param packageName
	 *            包名称
	 * @param packageUrl
	 *            jar的url
	 * @param classSet
	 *            class全路径集合
	 * @throws IOException 假如解析出现io异常时，则抛出该异常
	 * @see java.net.JarURLConnection
	 */
	protected void findClassesInJarFile(String packageName, URL packageUrl, 
			Set<String> classSet) throws IOException {
		URLConnection con = packageUrl.openConnection();
		JarFile jarFile = null; 
		String jarFileUrl = ""; 
		boolean closeJarFile = true;

		if (con instanceof JarURLConnection) {
			JarURLConnection jarCon = (JarURLConnection) con;
			URLUtils.useCachesIfNecessary(jarCon);
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			closeJarFile = !jarCon.getUseCaches();
		} else {
			//不是JarURLConnection->需要依赖于URL文件解析。
			//我们假设URL的格式为“jar:path!/entry”，只要遵循条目格式，协议就是任意的。
			//我们还将处理带和不带前导“file:”前缀的路径。
			String urlFile = packageUrl.getFile();
			try {
				int separatorIndex = urlFile.indexOf(ScannerPkgConsts.JAR_URL_SEPARATOR);
				if (separatorIndex != -1) {
					jarFileUrl = urlFile.substring(0, separatorIndex);
					jarFile = getJarFile(jarFileUrl);
				} else {
					jarFile = new JarFile(urlFile);
					jarFileUrl = urlFile;
				}
				closeJarFile = true;
			} catch (Exception ex) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("跳过无效jar classpath条目 [" + urlFile + "]");
				}
			}
		}
		
		if (jarFile == null) {
			return;
		}
		
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("在jar文件中查找匹配的资源 [" + jarFileUrl + "]");
			}
			
			classSet.addAll(findByJarFile(packageName, jarFile));
		} finally {
			if (closeJarFile && jarFile != null) {
				jarFile.close();
			}
		}
	}

	/**
	 * 获取指定jar文件中所有class名称（包名+类名称）格式为xx.xx.yy
	 * 
	 * @param jarFile jar文件
	 * @return class名称集合， 假如参数为null，返回大小为0的集合
	 */
	public Set<String> findByJarFile(JarFile jarFile) {
		return findByJarFile(XLPStringUtil.EMPTY, jarFile);
	}
	
	/**
	 * 获取指定jar文件中所有class名称（包名+类名称） xx.xx.yy
	 * 
	 * @param packageName 包名前缀xx.xx
	 * @param jarFile jar文件
	 * @return class名称集合， 假如参数为null，返回大小为0的集合
	 */
	public Set<String> findByJarFile(String packageName, JarFile jarFile) {
		Set<String> classSet = new HashSet<>();
		if (jarFile == null) {
			return classSet;
		}
		packageName = XLPStringUtil.emptyTrim(packageName);
		String packageBasePath = StringUtils.dotToSplash(packageName);
		if (!"".equals(packageBasePath) && !packageBasePath.endsWith("/")) {
			// 根条目路径必须以斜杠结束，以允许正确的匹配。匹配sunjre在这里不返回斜杠，但是beajrockit返回。
			packageBasePath = packageBasePath + "/";
		}
		for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			String entryPath = entry.getName();
			if (entryPath.startsWith(packageBasePath) && entryPath.endsWith(ScannerPkgConsts.CLASS_FILE_EXT)) { 
				int index = entryPath.indexOf(ScannerPkgConsts.CLASS_FILE_EXT);
				String relativePath = entryPath.substring(0, index);
				//排除内部类
				if (relativePath.contains(ScannerPkgConsts.INNER_CLASS_FLAG)) {
					continue;
				}
				classSet.add(StringUtils.splashToDot(relativePath));
			}
		}
		return classSet;
	}

	/**
	 * 将给定的jar文件URL解析为JarFile对象
	 * 
	 * @param jarFileUrl
	 */
	protected JarFile getJarFile(String jarFileUrl) throws IOException {
		if (jarFileUrl.startsWith(ScannerPkgConsts.FILE_URL_PREFIX)) {
			try {
				return new JarFile(URLUtils.toURI(jarFileUrl).getSchemeSpecificPart());
			} catch (URISyntaxException ex) {
				return new JarFile(jarFileUrl.substring(ScannerPkgConsts.FILE_URL_PREFIX.length()));
			}
		} else {
			return new JarFile(jarFileUrl);
		}
	}
	
	/**
	 * 以文件的方式扫描整个包下的文件 并添加到集合中
	 * 
	 * @param packageName
	 *            包名称
	 * @param packagePath
	 *            包的物理路径
	 * @param classSet
	 *            class全路径集合
	 */
	protected void findClassesInPackageByFile(String packageName, String packagePath, 
			Set<String> classSet) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists()) {
			LOGGER.warn("用户定义包名: 【" + packageName + "】不存在！");
			return;
		}
		if (!dir.isDirectory()) {
			LOGGER.warn("用户定义包名: 【" + packageName + "】不是目录！");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)，去除内部类
			public boolean accept(File file) {
				String name = file.getName();
				return (file.isDirectory()) || ((name.endsWith(ScannerPkgConsts.CLASS_FILE_EXT)
						&& !name.contains(ScannerPkgConsts.INNER_CLASS_FLAG)));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			String name = file.getName();
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findClassesInPackageByFile(packageName + "." + name, file.getAbsolutePath(), classSet);
			} else {
				String filename = file.getName();
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = filename.substring(0, filename.length() - ScannerPkgConsts.CLASS_FILE_EXT.length());
				className = packageName + "." + className;
				//去掉前缀“.”
				if (className.startsWith(".")) {
					className = className.substring(1);
				}
				classSet.add(className);
			}
		}
	}

	/**
	 * @param packageName
	 *            包名称
	 * @throws IOException
	 *             假如解析包名失败，则抛出该异常
	 */
	@Override
	public Set<Class<?>> scannerToClass(String packageName) throws IOException {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		if (classLoader == null) {
			return classes;
		}
		Set<String> classNames = scanner(packageName);
		for (String className : classNames) {
			//这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
			try {
				classes.add(classLoader.loadClass(className));
			} catch (ClassNotFoundException e) {
				LOGGER.error("【" + className + "】该class加载失败", e); 
			}
		}
		return classes;
	}
}
