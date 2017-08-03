package frank.demo.photopicker.app_manager;

import android.app.Application;
import android.content.Context;
import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class MyApp extends Application {

    private static Context sContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sContext = getApplicationContext();
		CrashHandler.getInstance().init(sContext);
	}

	public static Context getContext() {
		return sContext;
	}

	/**自定义注解ViewResId*/
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ViewResId{
		int value();
	}

	/**
	 * 使Class内的@ViewRoot注解有意义
	 * @param obj  Class的实例
	 * @param contentParentView 使用注解的View的parentView
	 */
	public static void ViewResId(Object obj, View contentParentView) {
		Class<?> cls = obj.getClass(); //获取obj的Class
		Field[] fields = cls.getDeclaredFields(); //获取Class中所有的成员
		for (Field field : fields) { //遍历所有成员
			ViewResId viewResId = field.getAnnotation(ViewResId.class);//获取成员的注解
			//判断成员是否含有注解
			if (viewResId != null) {
				int viewId = viewResId.value(); //获取成员注解的参数，这就是我们传进去控件Id
				if (viewId != -1) {
					try {
						field.setAccessible(true);//取消成员的封装
						field.set(obj, contentParentView.findViewById(viewId));//即 field = contentParentView.findViewById(viewId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
