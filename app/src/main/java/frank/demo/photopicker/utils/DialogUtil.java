package frank.demo.photopicker.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import frank.demo.photopicker.R;

public class DialogUtil {
    private static DialogUtil sInstance;
    private Dialog mDialog;

    private DialogUtil() {
    }

    public static DialogUtil getInstance() {
        if (sInstance == null) {
            synchronized(DialogUtil.class) {
                if(sInstance == null) {
                    sInstance = new DialogUtil();
                }
            }

        }
        return sInstance;
    }

    /**指定Dialog的宽度为屏幕的宽度*/
    public void displayDialogWindowWidth(Context context, View view, int gravity) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        dismissDialog();
        mDialog = new Dialog(context, R.style.ShareMenuDialog);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setGravity(gravity);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setWindowAnimations(R.style.ShareAnimation);
        WindowManager.LayoutParams mParams = mDialog.getWindow()
                .getAttributes();
        WindowManager windowManager =(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        mParams.width = (int) (display.getWidth() * 1.0);
        mDialog.getWindow().setAttributes(mParams);
        mDialog.show();
    }

    /**Dialog的宽度为布局wrap_content的宽度*/
    public void displayDialog(Context context, View view, int gravity) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        dismissDialog();
        mDialog = new Dialog(context, R.style.ShareMenuDialog);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        window.setGravity(gravity);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setWindowAnimations(R.style.ShareAnimation);
        mDialog.show();
    }

    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog.cancel();
        }
        mDialog = null;
    }

    public Dialog getDialog() {
        return mDialog;
    }
}
