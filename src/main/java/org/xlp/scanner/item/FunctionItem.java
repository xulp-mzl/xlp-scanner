package org.xlp.scanner.item;

import java.util.LinkedList;
import java.util.List;

import org.xlp.javabean.annotation.Bean;
import org.xlp.javabean.annotation.FieldName;


/**
 * <p>
 * 创建时间：2021年1月1日 下午10:10:33
 * </p>
 * 
 * @author xlp
 * @version 1.0
 * @Description 后台管理功能条目
 */
@Bean
public class FunctionItem {
	/**
	 * 功能条目对应的类名
	 */
	@FieldName
	private String className;

	/**
	 * 功能条目对应的方法名称
	 */
	@FieldName
	private String methodName;

	/**
	 * 功能条目对应的id
	 */
	@FieldName
	private String functionId;

	/**
	 * 功能条目对应的描述
	 */
	@FieldName
	private String descriptor = "";

	/**
	 * 功能条目对应的名称
	 */
	@FieldName
	private String functionName;

	/**
	 * 是否废弃该功能条目
	 */
	@FieldName
	private boolean isAbandoned = false;
	
	/**
	 * 子功能条目
	 */
	@FieldName
	private List<FunctionItem> childItems = new LinkedList<FunctionItem>();
	
	/**
	 * 父功能条目
	 */
	private FunctionItem parentFunctionItem;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public boolean isAbandoned() {
		return isAbandoned;
	}
	
	public void setAbandoned(boolean isAbandoned) {
		this.isAbandoned = isAbandoned;
	}

	public List<FunctionItem> getChildItems() {
		return childItems;
	}

	public void addChildItem(FunctionItem childItem) {
		this.childItems.add(childItem);
	}

	/**
	 * 根据子functionId获取子FunctionItem
	 * 
	 * @param functionId
	 * @return
	 */
	public FunctionItem getChildItemByFunctionId(String functionId){
		for (FunctionItem functionItem : childItems) {
			if (functionItem.getFunctionId().equals(functionId)) {
				return functionItem;
			}
		}
		return null;
	}
	
	public FunctionItem getParentFunctionItem() {
		return parentFunctionItem;
	}

	public void setParentFunctionItem(FunctionItem parentFunctionItem) {
		this.parentFunctionItem = parentFunctionItem;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((functionId == null) ? 0 : functionId.hashCode());
		result = prime * result + ((parentFunctionItem == null) ? 0 : parentFunctionItem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionItem other = (FunctionItem) obj;
		if (functionId == null) {
			if (other.functionId != null)
				return false;
		} else if (!functionId.equals(other.functionId))
			return false;
		if (parentFunctionItem == null) {
			if (other.parentFunctionItem != null)
				return false;
		} else if (!parentFunctionItem.equals(other.parentFunctionItem))
			return false;
		return true;
	}
	
	/**
	 * 判断给定的功能条目是否是本功能条目的父项
	 * 
	 * @param parentFunctionItem
	 * @return 假如是返回true，否则返回false
	 */
	public boolean isSelfParent(FunctionItem parentFunctionItem){
		if (parentFunctionItem == null) {
			return false;
		}
		return parentFunctionItem.equals(this.getParentFunctionItem());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FunctionItem [className=").append(className).append(", methodName=").append(methodName)
				.append(", functionId=").append(functionId).append(", descriptor=").append(descriptor)
				.append(", functionName=").append(functionName).append(", isAbandoned=").append(isAbandoned)
				.append(", childItems=").append(childItems).append("]");
		return builder.toString();
	}
}
