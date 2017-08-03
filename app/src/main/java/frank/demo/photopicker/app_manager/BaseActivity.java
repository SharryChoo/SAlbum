package frank.demo.photopicker.app_manager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import frank.demo.photopicker.utils.BitmapUtils;
import frank.demo.photopicker.utils.FileUtils;

public class BaseActivity extends AppCompatActivity {

	private final static int REQUEST_PERMISSION = 1;
	private final static int TAKE_PHOTO = 1;
	private final static int CROP_PHOTO = 2;
	private final static int CHOOSE_PHOTO = 3;

	private PermissionListener mPermissionListener = null;
	private OpenCameraListener mOpenCameraListener = null;
	private OpenAlbumListener mOpenAlbumListener = null;
	private Uri mImageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityCollector.addActivity(this);
		super.onCreate(savedInstanceState);
	}

	/**
	 * 请求权限
	 */
	public void requestRuntimePermission(String []permissions, PermissionListener listener) {
		mPermissionListener = listener;
		List<String> permissionList = new ArrayList<>();
		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				permissionList.add(permission);
			}
		}
		if(!permissionList.isEmpty()) {
			ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), REQUEST_PERMISSION);
		} else {
			mPermissionListener.onGranted();
		}
	}

	/**
	 * 请求权限结果的回调
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_PERMISSION: {
				if (grantResults.length > 0) {
					List<String> deniedPermissions = new ArrayList<>();
					for (int i = 0; i < grantResults.length; i++) {
						int grantResult = grantResults[i];
						String permission = permissions[i];
						if (grantResult != PackageManager.PERMISSION_GRANTED) {
							deniedPermissions.add(permission);
						}
					}
					if(deniedPermissions.isEmpty()) {
						mPermissionListener.onGranted();
					} else {
						missingPermissionDialog();
					}
				}
				break;
			}
			default:
				break;
		}
	}

	/**
	 * 缺失权限提示
	 */
	private void missingPermissionDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("当前应用缺少必要权限");
		builder.setTitle("帮助");

		//拒绝
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//启动当前App的系统设置界面
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.setData(Uri.parse("package:" + getPackageName()));
				startActivity(intent);
			}
		});

		builder.show();
	}

	/**
	 * 打开相机
	 */
	public void openCamera(OpenCameraListener listener) {
		mOpenCameraListener = listener;

		//创建File对象,用于存储拍照后的图片
		FileUtils fileUtils = FileUtils.getInstance();
		File cameraPhoto = fileUtils.createFile("camera_photo.png", FileUtils.CACHE);
		mImageUri = fileUtils.getUri(cameraPhoto);

		//启动相机
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");//action中的image_capture即为图片拍摄
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);//调用Intent的putExtra()方法指定图片的输出地址
		startActivityForResult(intent, TAKE_PHOTO);
	}

	/**
	 * 打开相册
	 */
	public void openAlbum(OpenAlbumListener listener) {
		mOpenAlbumListener = listener;
		Intent intent = new Intent("android.intent.action.GET_CONTENT");
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		startActivityForResult(intent, CHOOSE_PHOTO);
	}

	/**
	 * 执行照片的裁剪
	 */
	private void cropPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");//启动裁剪工具
		//安卓7.0以上版本需加Flags
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
		intent.setDataAndType(uri, "image/*");//可以选择图片类型, 如果是*表明所有类型的图片
		intent.putExtra("crop", true);//设置可裁剪状态
		intent.putExtra("scale", true);//裁剪时是否保留图片的比例, 这里的比例是1:1
		intent.putExtra("aspectX", 1);// aspectX, aspectY是宽高的比例，这里设置的是正方形
		intent.putExtra("aspectY", 1);// aspectX, aspectY是宽高的比例，这里设置的是正方形
		intent.putExtra("outputX", 500);//outputX是裁剪图片宽
		intent.putExtra("outputY", 500);//outputY是裁剪图片高
		intent.putExtra("outputFormat", "PNG");//设置输出的格式
		intent.putExtra("return-data", true);//是否将数据保留在Bitmap中返回
		startActivityForResult(intent, CROP_PHOTO);

		/*intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);//该方法已经不可用
		 * Android4.4不能使用扩展卡，所以判断以后 使用了应用自己的目录，而不是扩展卡目录。
		 * 这样就是让第三方的裁剪应用把图片保存到自己的APP目录下，这显然是不可能的，可以通
		 * 过一些文件管理应用查看权限，这个目录只有应用本身才可以读写的，其它应用没有读写权
		 * 限。所以保存失败了。这样一来，扩展卡不能保存，第三方又不能跨APP保存，所以失败了。*/
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
			case TAKE_PHOTO://拍照
				if(resultCode == RESULT_OK){
					try {
						Bitmap oldBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
						Bitmap nowBimap = BitmapUtils.rotateBitmap(oldBitmap, 90);
						if(oldBitmap == nowBimap){
							cropPhoto(mImageUri);
						}else {
							//将旋转后的Bitmap保存到本地
							FileUtils fileUtils = FileUtils.getInstance();
							File file = fileUtils.createFile("rotated_camera_photo.png", FileUtils.CACHE);
							BitmapUtils.writeBitmapToFile(file, nowBimap, 80);
							//获取新Uri
							Uri uri = fileUtils.getUri(file);
							//启动裁剪程序
							cropPhoto(uri);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
				break;

			case CHOOSE_PHOTO:
				//从相册选择照片
				if(resultCode == RESULT_OK){
					Uri uri = intent.getData();
					cropPhoto(uri);
				}
				break;

			case CROP_PHOTO:
				//裁剪照片
				if(resultCode == RESULT_OK) {
					//获取返回的Bitmap
					Bitmap bitmap = intent.getParcelableExtra("data");
					if(mOpenCameraListener != null) {
						mOpenCameraListener.onResult(bitmap);
					} else {
						mOpenAlbumListener.onResult(bitmap);
					}
				}
				break;

			default:
				break;
		}
	}

	@Override
	protected void onDestroy(){
		ActivityCollector.removeActivity(this);//当有活动关闭时从ActivityCollector中移除这个活动
		super.onDestroy();
	}

	/**权限监听器*/
	public interface PermissionListener {
		void onGranted();
	}
	/**相机图片处理结果监听器*/
	public interface OpenCameraListener {
		void onResult(Bitmap bitmap);
	}
	/**相册处理结果监听器*/
	public interface OpenAlbumListener {
		void onResult(Bitmap bitmap);
	}
}
