package org.xlp.scanner.item;

import java.util.List;

import org.xlp.utils.collection.XLPCollectionUtil;

/**
 * <p>
 * 创建时间：2021年1月1日 下午10:24:52
 * </p>
 * 
 * @author xlp
 * @version 1.0
 * @Description 功能条目处理工具类
 */
class FunctionItemUtils {
	/**
	 * 从给定的功能条目集合，获取指定FunctionItem的具体功能条目
	 * 
	 * @param functionItems
	 * @param functionItem
	 * @return 假如没有找到返回null
	 */
	public static FunctionItem getFunctionItemByFunctionItem(List<FunctionItem> functionItems, FunctionItem functionItem) {
		if (XLPCollectionUtil.isEmpty(functionItems) || functionItem == null) {
			return null;
		}
		for (FunctionItem functionItem1 : functionItems) {
			if (functionItem.equals(functionItem1)) {
				return functionItem1;
			}
		}
		return null;
	}
	
	/**
	 * 判断给定的集合中是否包含给定的功能条目
	 * 
	 * @param functionItems
	 * @param functionItem
	 * @return 假如给定的集合中包含给定的功能条目返回true，否则返回false
	 */
	public static boolean hasFunctionItem(List<FunctionItem> functionItems, FunctionItem functionItem){
		return getFunctionItemByFunctionItem(functionItems, functionItem) != null;
	}
}
