# xlp-scanner
包扫描器，获取指定包下的所有class，提供用注解进行功能条目管理

```
package org.xlp.test;

import org.xlp.scanner.annotation.PermissionControl;
import org.xlp.scanner.annotation.PermissionItem;

@PermissionControl
@PermissionItem(id="123", name="test")
public class Test {
	@PermissionItem(id="1234", name="test1")
	public void name() {
		
	}
	@PermissionItem(id="1237", name="test2")
	public void name1() {
		
	}
	
	@PermissionItem(id="1237", name="test22")
	public void name2() {
		
	}
}

```

```
		ClassPathPkgScanner classPathPkgScanner = new ClassPathPkgScanner();
        Set<String> set = classPathPkgScanner.scanner("org.xlp.test");
        for (String string : set) {
			System.out.println(string);
		}
        
        FunctionItemProcessor functionItemProcessor = new FunctionItemProcessor();
        System.out.println(functionItemProcessor.functionItemsToJson("org.xlp.test"));
```
