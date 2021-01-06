package org.xlp.scanner.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>创建时间：2020年12月31日 下午11:05:55</p>
 * @author xlp
 * @version 1.0 
 * @Description 功能权限访问控制注解
*/

@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.TYPE, ElementType.METHOD})//定义注解的作用目标**作用范围字段 
@Documented//说明该注解将被包含在javadoc中 
public @interface PermissionItem {
	/**
	 * 权限条目id，需在同级唯一性
	 */
	public String id();
	
	/**
	 * 权限条目的父条目id，需在同级唯一性
	 */
	public String parentId() default "";
	
	/**
	 * 权限条目描述
	 */
	public String descriptor() default "";
	
	/**
	 * 权限条目名称
	 */
	public String name();
	
	/**
	 * 是否废弃该权限条目
	 */
	public boolean isAbandoned() default false;
}
