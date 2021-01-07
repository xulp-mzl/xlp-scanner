package org.xlp.scanner.item;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.json.JsonArray;
import org.xlp.scanner.annotation.PermissionControl;
import org.xlp.scanner.annotation.PermissionItem;
import org.xlp.scanner.item.exception.FunctionItemException;
import org.xlp.scanner.item.util.MethodUtils;
import org.xlp.scanner.pkg.ClassPathPkgScanner;
import org.xlp.utils.XLPArrayUtil;
import org.xlp.utils.XLPStringUtil;
import org.xlp.utils.XLPSystemParamUtil;
import org.xlp.utils.collection.XLPCollectionUtil;

/**
 * <p>
 * 创建时间：2021年1月1日 下午10:39:15
 * </p>
 * 
 * @author xlp
 * @version 1.0
 * @Description 解析功能条目处理器
 */
public class FunctionItemProcessor {
	private final static Logger LOGGER = LoggerFactory.getLogger(FunctionItemProcessor.class);
	/**
	 * 记录是否忽略功能条目id重复错误
	 */
	private boolean ignoreFunctionIdDuplicatedError = true;

	/**
	 * 类加载器
	 */
	private ClassLoader classLoader;
	
	/**
	 * 存储要处理包的名称
	 */
	private Set<String> processPkgs = new HashSet<String>();

	/**
	 * 构造器
	 */
	public FunctionItemProcessor() {
	}

	/**
	 * 构造器
	 * 
	 * @param ignoreFunctionIdDuplicatedError
	 * @param classLoader
	 */
	public FunctionItemProcessor(boolean ignoreFunctionIdDuplicatedError, ClassLoader classLoader) {
		this.ignoreFunctionIdDuplicatedError = ignoreFunctionIdDuplicatedError;
		this.classLoader = classLoader;
	}

	/**
	 * 构造器
	 * 
	 * @param classLoader
	 */
	public FunctionItemProcessor(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * 构造器
	 * 
	 * @param ignoreFunctionIdDuplicatedError
	 */
	public FunctionItemProcessor(boolean ignoreFunctionIdDuplicatedError) {
		this.ignoreFunctionIdDuplicatedError = ignoreFunctionIdDuplicatedError;
	}

	public boolean isIgnoreFunctionIdDuplicatedError() {
		return ignoreFunctionIdDuplicatedError;
	}

	public void setIgnoreFunctionIdDuplicatedError(boolean ignoreFunctionIdDuplicatedError) {
		this.ignoreFunctionIdDuplicatedError = ignoreFunctionIdDuplicatedError;
	}

	/**
	 * 根据包名获取所有的功能条目
	 * 
	 * @return 返回要解析包名下所有的功能条目
	 * @throws FunctionItemException
	 *             假如不忽略functionId重复错误，则抛出该异常
	 */
	public List<FunctionItem> getFunctionItems() {
		List<FunctionItem> functionItems = new LinkedList<FunctionItem>();
		if (processPkgs.isEmpty()) { 
			boolean isWarn = LOGGER.isWarnEnabled();
			if (isWarn) {
				LOGGER.warn("没有要解析的包名。");
			}
			return functionItems;
		}
		
		// 获取指定包名下的所有class
		ClassPathPkgScanner scanner = new ClassPathPkgScanner(classLoader);
		Set<Class<?>> classes = new HashSet<Class<?>>();

		boolean isDebug = LOGGER.isDebugEnabled();
		boolean isError = LOGGER.isErrorEnabled();
		//循环处理包名
		for (String basePackage : processPkgs) {
			if (isDebug) {
				LOGGER.debug("开始通过包名【" + basePackage + "】获取功能条目信息。");
			}
			try {
				classes.addAll(scanner.scannerToClass(basePackage));
			} catch (IOException e) {
				if (isError) {
					LOGGER.error("获取指定包下的class失败，失败原因如下：" + XLPSystemParamUtil.getSystemNewline(), e);
				}
			}
		}
		
		// 根据@PermissionControl注解过滤出需要的class
		classes = filterByAnnotation(classes, PermissionControl.class);
		// 获取@PermissionItem的功能条目
		processFunctionItems(classes, functionItems);
		// 建立条目之间的关系
		functionItems = createFunctionItemLink(functionItems);
		return functionItems;
	}

	/**
	 * 建立条目之间的关系
	 * 
	 * @param functionItems
	 * @return
	 * @throws FunctionItemException
	 *             假如不忽略functionId重复错误，则抛出该异常
	 */
	protected List<FunctionItem> createFunctionItemLink(List<FunctionItem> functionItems) {
		List<FunctionItem> parentFunctionItems = new ArrayList<FunctionItem>();
		List<FunctionItem> childFunctionItems = new ArrayList<FunctionItem>();

		FunctionItem parentFunctionItem;
		for (FunctionItem functionItem : functionItems) {
			parentFunctionItem = functionItem.getParentFunctionItem();
			// 判断是否是父功能条目
			if (parentFunctionItem == null) {
				checkFunctionItem(parentFunctionItems, functionItem);
			} else {
				checkFunctionItem(childFunctionItems, functionItem);
			}
		}

		List<FunctionItem> hasFunctionItems = new ArrayList<FunctionItem>();
		List<FunctionItem> tempFunctionItems = parentFunctionItems;
		// 创建关系
		int childFunctionItemsSize = childFunctionItems.size();
		while (childFunctionItemsSize > 0) {
			for (FunctionItem functionItem : childFunctionItems) {
				for (FunctionItem functionItem1 : tempFunctionItems) {
					// 判断functionItem是否是functionItem1的子功能条目
					if (functionItem.isSelfParent(functionItem1)) {
						functionItem1.addChildItem(functionItem);
						hasFunctionItems.add(functionItem);
					}
				}
			}
			// 从子功能条目中去除已处理的功能条目
			childFunctionItems.removeAll(hasFunctionItems);
			childFunctionItemsSize = childFunctionItems.size();
			tempFunctionItems = hasFunctionItems;
			hasFunctionItems.clear();
		}
		tempFunctionItems = null;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("关系创建完成！");
		}
		return parentFunctionItems;
	}

	/**
	 * 检查功能条目是否重复
	 * 
	 * @param functionItems
	 * @param functionItem
	 */
	private void checkFunctionItem(List<FunctionItem> functionItems, FunctionItem functionItem) {
		FunctionItem oldFunctionItem = FunctionItemUtils.getFunctionItemByFunctionItem(functionItems, functionItem);
		boolean isAvailable = !functionItem.isAbandoned();
		boolean hasRemoved = false;
		if (oldFunctionItem != null) {
			StringBuilder msgSb = new StringBuilder();
			msgSb.append(functionItem.getClassName()).append(".")
					.append(XLPStringUtil.emptyTrim(functionItem.getMethodName())).append("[")
					.append(functionItem.getFunctionId()).append("]").append("该功能条目id已存在！");
			// 记录是否已存在的功能条目不可用
			boolean isReportFunctionIdDuplicated = (oldFunctionItem.isAbandoned() && !isAvailable)
					|| (!oldFunctionItem.isAbandoned() && isAvailable);
			if (!ignoreFunctionIdDuplicatedError && isReportFunctionIdDuplicated) {
				throw new FunctionItemException(msgSb.toString());
			}
			if (LOGGER.isWarnEnabled() && isReportFunctionIdDuplicated) {
				LOGGER.warn(msgSb.toString());
			}

			if ((isAvailable && oldFunctionItem.isAbandoned()) || isReportFunctionIdDuplicated) {
				functionItems.remove(oldFunctionItem);
				hasRemoved = true;
			}
		}
		if (isAvailable || hasRemoved) {
			functionItems.add(functionItem);
		}
	}

	/**
	 * 解析所有的FunctionItem放入指定的功能条目集合中
	 * 
	 * @param classes
	 * @param functionItems
	 * @throws FunctionItemException
	 *             假如不忽略functionId重复错误，则抛出该异常
	 */
	protected void processFunctionItems(Set<Class<?>> classes, List<FunctionItem> functionItems) {
		PermissionItem permissionItem;
		FunctionItem functionItem = null;
		String functionId;
		boolean isWarn = LOGGER.isWarnEnabled();
		for (Class<?> cs : classes) {
			functionItem = null;
			String className = cs.getSimpleName();
			permissionItem = cs.getAnnotation(PermissionItem.class);
			if (permissionItem != null) {
				functionItem = new FunctionItem();
				functionItem.setClassName(className);
				permissionItemFillFunctionItem(permissionItem, functionItem);
				functionId = functionItem.getFunctionId();
				if (XLPStringUtil.isEmpty(functionId)) {
					if (isWarn) {
						LOGGER.warn(functionItem + "的functionId为空，跳过该功能条目。");
					}
					functionItem = null;
				} else {
					functionItems.add(functionItem);
				}
			}
			// 处理该类下含PermissionItem注解的方法
			// 获取该类下的所有公共方法
			Method[] methods = cs.getDeclaredMethods();
			// 获取该类下的所有公共方法
			List<Method> methodList = MethodUtils.filterPublicMethod(methods);
			// 清空临时数据，加快垃圾回收器回收
			methods = null;
			FunctionItem parentFunctionItem = functionItem;
			for (Method method : methodList) {
				permissionItem = method.getAnnotation(PermissionItem.class);
				if (permissionItem != null) {
					functionItem = new FunctionItem();
					functionItem.setClassName(className);
					functionItem.setMethodName(method.getName());
					permissionItemFillFunctionItem(permissionItem, functionItem);
					
					functionId = functionItem.getFunctionId();
					if (XLPStringUtil.isEmpty(functionId)) {
						if (isWarn) {
							LOGGER.warn(functionItem + "的functionId为空，跳过该功能条目。");
						}
						functionItem = null;
						continue;
					}
					
					// 假如没有父功能条目, 就设置class对应的功能条目为父功能条目
					if (functionItem.getParentFunctionItem() == null) {
						functionItem.setParentFunctionItem(parentFunctionItem);
					}
					functionItems.add(functionItem);
				}
			}
		}
	}

	/**
	 * 用PermissionItem填充FunctionItem对象
	 * 
	 * @param permissionItem
	 * @param functionItem
	 */
	private void permissionItemFillFunctionItem(PermissionItem permissionItem, FunctionItem functionItem) {
		functionItem.setFunctionId(permissionItem.id());
		functionItem.setAbandoned(permissionItem.isAbandoned());
		functionItem.setDescriptor(permissionItem.descriptor());
		functionItem.setFunctionName(permissionItem.name());
		String parentId = permissionItem.parentId();
		if (!XLPStringUtil.isEmpty(parentId)) {
			// 创建临时的父功能条目FunctionItem
			FunctionItem parentFunctionItem = new FunctionItem();
			parentFunctionItem.setFunctionId(permissionItem.parentId());
			functionItem.setParentFunctionItem(parentFunctionItem);
		}
	}

	/**
	 * 根据给定的注解过滤出需要的class
	 * 
	 * @param classes
	 * @param annotationClss
	 * @return
	 */
	protected Set<Class<?>> filterByAnnotation(Set<Class<?>> classes, Class<? extends Annotation> annotationClss) {
		Set<Class<?>> filterClasses = new HashSet<Class<?>>();
		for (Class<?> clazz : classes) {
			Annotation annotation = clazz.getAnnotation(annotationClss);
			if (annotation != null) {
				filterClasses.add(clazz);
			}
		}
		return filterClasses;
	}

	/**
	 * 根据包名获取所有的功能条目并转换成json格式字符串
	 * 
	 * @return 解析包名获取所有的功能条目并转换成json格式字符串
	 * @throws FunctionItemException
	 *             假如不忽略functionId重复错误，则抛出该异常
	 */
	public String functionItemsToJson() {
		return JsonArray.fromCollection(this.getFunctionItems()).toString();
	}
	
	/**
	 * 添加要处理的包名称
	 * 
	 * @param basePackage 包名称
	 * @return 返回本类实例对象
	 */
	public FunctionItemProcessor addProcessPackage(String basePackage){
		if (!XLPStringUtil.isEmpty(basePackage)) {
			this.processPkgs.add(basePackage.trim());
		}
		return this;
	}
	
	/**
	 * 添加要处理的包名称
	 * 
	 * @param basePackages 包名称
	 * @return 返回本类实例对象
	 */
	public FunctionItemProcessor addProcessPackages(String[] basePackages){
		if (!XLPArrayUtil.isEmpty(basePackages)) {
			for (String basePackage : basePackages) {
				addProcessPackage(basePackage);
			}
		}
		return this;
	}
	
	/**
	 * 添加要处理的包名称
	 * 
	 * @param basePackages 包名称
	 * @return 返回本类实例对象
	 */
	public FunctionItemProcessor addProcessPackages(List<String> basePackages){ 
		if (!XLPCollectionUtil.isEmpty(basePackages)) {
			for (String basePackage : basePackages) {
				addProcessPackage(basePackage);
			}
		}
		return this;
	}
}
