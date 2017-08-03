package frank.demo.photopicker.app_manager;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
public class ActivityCollector{

    /*创建一个List的实例,以列表的形式存放管理所有的活动 */
    public static List<Activity> mListActivities = new ArrayList<Activity>();

    /*向List中加入活动*/
    public static void addActivity(Activity activity){
        if ( !mListActivities.contains(activity) ) {//只有当集合中不包含传入的Activity实例的时候才会将它添加到集合中
            mListActivities.add(activity);
        }
    }

    /*从List中移除活动*/
    public static void removeActivity(Activity activity){
        mListActivities.remove(activity);
    }

    /*关闭List中存放的左右的活动*/
    public static void finishAll(){
        for( Activity activity : mListActivities ){//从List的列表的实例mListActivities中获取活动
            if( !activity.isFinishing() ){
                activity.finish();
            }
        }
    }

    public static void finishApp() {
        for( Activity activity : mListActivities ){//从List的列表的实例mListActivities中获取活动
        if( !activity.isFinishing() ){
            activity.finish();
        }
    }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}

