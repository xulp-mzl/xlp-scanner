package org.xlp.scanner.item.exception;
/**
 * <p>创建时间：2021年1月2日 下午10:55:07</p>
 * @author xlp
 * @version 1.0 
 * @Description 类描述
*/
public class FunctionItemException extends RuntimeException {
	private static final long serialVersionUID = -2866589594218358900L;

	public FunctionItemException(String message, Throwable cause) {
		super(message, cause);
	}

	public FunctionItemException(String message) {
		super(message);
	}
}
