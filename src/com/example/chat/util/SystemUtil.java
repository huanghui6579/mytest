package com.example.chat.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.model.Emoji;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * 系统常用的工具方法
 * 
 * @author huanghui1
 *
 */
public class SystemUtil {
	
	private static ExecutorService cachedThreadPool = null;//可缓存的线程池
	/**
	 * 检测系统网络是否可用
	 * @return
	 */
	public static boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) ChatApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			status = false;
		}
		return status;
	}
	
	/**
	 * 隐藏输入法
	 * @param view
	 */
	public static void hideSoftInput(View view) {
		InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	/**
	 * 如果输入法在窗口上已经显示，则隐藏，反之则显示
	 * @update 2014年10月25日 下午4:47:02
	 */
	public static void toogleSoftInput() {
		InputMethodManager imm = (InputMethodManager) ChatApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	/**
	 * 显示输入法
	 * @update 2014年10月25日 下午4:47:46
	 * @param view
	 */
	public static void showSoftInput(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view,InputMethodManager.SHOW_FORCED);
	}
	
	/**
	 * 获取输入法打开的状态
	 * @update 2014年10月25日 下午4:49:06
	 * @return
	 */
	public static boolean isSoftInputActive() {
		InputMethodManager imm = (InputMethodManager) ChatApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
		return isOpen;
	}
	
	/**
	 * 显示短时间的toast
	 * @author Administrator
	 * @update 2014年10月7日 上午9:49:18
	 * @param text
	 */
	public static void makeShortToast(CharSequence text) {
		Toast.makeText(ChatApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 显示短时间的toast
	 * @author Administrator
	 * @update 2014年10月7日 上午9:49:18
	 * @param text
	 */
	public static void makeShortToast(int resId) {
		Toast.makeText(ChatApplication.getInstance(), resId, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 显示长时间的toast
	 * @author Administrator
	 * @update 2014年10月7日 上午9:50:02
	 * @param text
	 */
	public static void makeLongToast(CharSequence text) {
		Toast.makeText(ChatApplication.getInstance(), text, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * 显示长时间的toast
	 * @author Administrator
	 * @update 2014年10月7日 上午9:50:02
	 * @param text
	 */
	public static void makeLongToast(int resId) {
		Toast.makeText(ChatApplication.getInstance(), resId, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * 获取手机型号
	 * @update 2014年10月9日 上午8:39:55
	 * @return
	 */
	public static String getPhoneModel() {
		String model = android.os.Build.MODEL;
		if(TextUtils.isEmpty(model)) {
			model = "Android";
		}
		return model;
	}
	
	/**
	 * 获得当前的Android版本
	 * @update 2014年10月13日 上午9:11:10
	 * @return
	 */
	public static final int getCurrentSDK() {
		return android.os.Build.VERSION.SDK_INT;
	}
	
	/**
	 * SD卡是否可用
	 * @update 2014年10月23日 下午5:17:09
	 * @return
	 */
	public static boolean isSdcardAvailable() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	/**
	 * 保存文件，根据用户名动态生成文件夹，该用户名为当前登录的用户名
	 * @update 2014年10月23日 下午5:06:50
	 * @param data
	 * @param filePath 保存文件的路径，不含文件名
	 * @param filename 保存的文件名称，不含有路径
	 * @return
	 */
	public static File saveFile(byte[] data, String filePath, String filename) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			if (!dir.exists() && dir.isDirectory()) {
				dir.mkdirs();
			}
			file = new File(dir, filename);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bos = null;
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fos = null;
			}
		}
		return file;
	}
	
	/**
	 * 保存文件，根据用户名动态生成文件夹，该用户名为当前登录的用户名
	 * @update 2014年10月23日 下午5:06:50
	 * @param data
	 * @param filePath 保存文件的路径，不含文件名
	 * @param filename 保存的文件名称，不含有路径
	 * @return
	 */
	public static File saveFile(byte[] data, String savePath) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File saveFile = null;
		try {
			saveFile = new File(savePath);
			File dir = saveFile.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			fos = new FileOutputStream(saveFile);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bos = null;
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fos = null;
			}
		}
		return saveFile;
	}
	
	/**
	 * 根据文件的全路径判断文件是否存在
	 * @update 2014年10月24日 上午9:00:51
	 * @param filePath
	 * @return
	 */
	public static boolean isFileExists(String filePath) {
		boolean flag = false;
		if (TextUtils.isEmpty(filePath)) {
			flag = false;
		} else {
			File file = new File(filePath);
			flag = file.exists();
		}
		return flag;
	}
	
	/**
	 * 保存文件，根据用户名动态生成文件夹，该用户名为当前登录的用户名
	 * @update 2014年10月23日 下午5:06:50
	 * @param data
	 * @param saveFile 保存文件的路径，不含文件名
	 * @return
	 */
	public static File saveFile(byte[] data, File saveFile) {
		if (data == null || data.length <= 0) {
			return null;
		}
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			File dir = saveFile.getParentFile();
			if (!dir.exists() && dir.isDirectory()) {
				dir.mkdirs();
			}
			fos = new FileOutputStream(saveFile);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bos = null;
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fos = null;
			}
		}
		return saveFile;
	}
	
	/**
	 * 保存文件，根据用户名动态生成文件夹，该用户名为当前登录的用户名
	 * @update 2014年10月23日 下午5:06:50
	 * @param photoVal 图片的Base64位编码字符串
	 * @param saveFile 保存文件的路径，不含文件名
	 * @return
	 */
	public static File saveFile(String photoVal, File saveFile) {
		byte[] data = getAvatarByStringVal(photoVal);
		if (data != null && data.length > 0) {
			return saveFile(data, saveFile);
		} else {
			return null;
		}
	}
	
	/**
	 * 保存文件，根据用户名动态生成文件夹，该用户名为当前登录的用户名
	 * @update 2014年10月23日 下午5:06:50
	 * @param photoVal 图片的Base64位编码字符串
	 * @param saveFile 保存文件的路径，不含文件名
	 * @return
	 */
	public static File saveFile(String photoVal, String savePath) {
		byte[] data = getAvatarByStringVal(photoVal);
		if (data != null && data.length > 0) {
			return saveFile(data, savePath);
		} else {
			return null;
		}
	}
	
	/**
	 * 将字节数组转换成文件
	 * @update 2014年10月23日 下午5:26:48
	 * @param file
	 * @return
	 */
	public static byte[] getFileBytes(File file) {
		
		byte[] data = null;
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		try {
			fis = new FileInputStream(file);
			baos = new ByteArrayOutputStream(1024);
			byte[] buf = new byte[1024];
			int len = -1;
			while ((len = fis.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			data = baos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			baos = null;
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			fis = null;
		}
		return data;
	}
	
	/**
	 * 将字节数组转换成文件
	 * @update 2014年10月23日 下午5:26:48
	 * @param filename 文件的全路径，包含文件名
	 * @return
	 */
	public static byte[] getFileBytes(String filename) {
		File file = new File(filename);
		return getFileBytes(file);
	}
	
	private static String convertByteArrayToHexString(byte[] arrayBytes) {
	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < arrayBytes.length; i++) {
	        stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
	                .substring(1));
	    }
	    return stringBuffer.toString();
	}
	
	/**
	 * 根据文件的字节数组获取文件的hash值
	 * @update 2014年10月23日 下午5:35:11
	 * @param bytes
	 * @return
	 */
	public static String getFileHash(byte[] bytes) {
		String hash= null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(bytes);
			byte[] hashbyte = digest.digest();
			hash = convertByteArrayToHexString(hashbyte);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hash;
	}
	
	/**
	 * 根据文件的字节数组获取文件的hash值
	 * @update 2014年10月23日 下午5:35:11
	 * @param file
	 * @return
	 */
	public static String getFileHash(File file) {
		String hash= null;
		FileInputStream fis = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			fis = new FileInputStream(file);
			byte[] buf = new byte[1024];
			int len = -1;
			while ((len = fis.read(buf)) != -1) {
				digest.update(buf, 0, len);
			}
			byte[] hashbyte = digest.digest();
			hash = convertByteArrayToHexString(hashbyte);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fis = null;
			}
		}
		return hash;
	}
	
	/**
	 * 通过64编码将图像的字符创解析为byte数组
	 * @update 2014年11月10日 下午9:45:31
	 * @param val
	 * @return
	 */
	public static byte[] getAvatarByStringVal(String val) {
        if (val == null) {
            return null;
        }
        return StringUtils.decodeBase64(val);
    }
	
	/**
	 * 根据当前用户获取root目录
	 * @update 2014年10月24日 下午8:12:44
	 * @return
	 */
	public static File getDefaultRoot() {
		String currentUser = ChatApplication.getInstance().getCurrentAccount();
		File root = new File(Environment.getExternalStorageDirectory(), "ChatApp" + File.separator + currentUser);
		if (!root.exists()) {
			root.mkdirs();
		}
		return root;
	}
	
	/**
	 * 获取头像默认的存放路径，格式为/mnt/sdcard/ChatApp/currentuser/head_icon/
	 * @update 2014年10月23日 下午6:09:27
	 * @param username
	 * @return
	 */
	public static String getDefaultIconPath() {
		File dir = new File(getDefaultRoot(), "head_icon");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir.getAbsolutePath();
	}
	
	/**
	 * 根据用户名创建图像的名称
	 * @update 2014年10月23日 下午6:11:41
	 * @param username
	 * @return
	 */
	public static File generateIconFile(String username) {
		return new File(getDefaultIconPath(), "icon_" + username + ".jpg");
	}
	
	/**
	 * 更具jid获取用户登录的资源
	 * @update 2014年10月23日 下午7:23:52
	 * @param jid
	 * @return
	 */
	public static String getResourceWithJID(String jid) {
		if (TextUtils.isEmpty(jid)) {
			return null;
		}
		if (jid.contains("/")) {
			return jid.substring(jid.indexOf("/") + 1);
		} else {
			return null;
		}
	}
	
	/**
	 * 从SD卡加载图片  
	 * @update 2014年10月24日 下午3:24:51
	 * @param imagePath
	 * @return
	 */
    public static Bitmap getImageFromLocal(String imagePath){
    	if (TextUtils.isEmpty(imagePath)) {
			return null;
		}
        File file = new File(imagePath);  
        if(file.exists()){  
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);  
            return bitmap;  
        }  
        return null;  
    }
    
    /**
     * 获取assets的内容
     * @update 2014年10月27日 上午11:27:27
     * @return
     */
    public static List<String> getEmojiFromFile(String filename) {
    	List<String> list = null;
    	BufferedReader br = null;
    	try {
    		InputStream is = ChatApplication.getInstance().getResources().getAssets().open(filename);
    		list = new ArrayList<>();
    		br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    		String line = null;
    		while((line = br.readLine()) != null) {
    			list.add(line);
    		}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				br = null;
			}
		}
    	return list;
    }
    
    /**
     * 根据文件名获取文件的资源id
     * @update 2014年10月27日 上午11:31:25
     * @param fileName
     * @return
     */
    public static int getRespurceIdByName(String fileName) {
    	Context context = ChatApplication.getInstance();
    	int resID = context.getResources().getIdentifier(fileName,
				"drawable", context.getPackageName());
    	return resID;
    }
    
    /**
     * 添加表情到editext输入框
     * @update 2014年10月27日 下午4:51:25
     * @param emoji
     * @return
     */
    public static SpannableStringBuilder addEmojiString(Emoji emoji) {
    	SpannableStringBuilder sb = new SpannableStringBuilder();
    	int resId = emoji.getResId();
    	String description = emoji.getDescription();
    	
    	Context context = ChatApplication.getInstance();
    	
    	sb.append(description);
    	Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
    	bitmap = Bitmap.createScaledBitmap(bitmap, 35, 35, true);
    	ImageSpan imageSpan = new ImageSpan(context, bitmap);
    	
    	sb.setSpan(imageSpan, 0, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	return sb;
    }
    
    /**
     * 获得屏幕的大小,size[0]:屏幕的宽，size[1]:屏幕的高
     * @update 2014年10月29日 下午9:16:12
     * @return
     */
    public static int[] getScreenSize() {
    	int[] size = new int[2];
    	WindowManager wm = (WindowManager) ChatApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
    	Display display = wm.getDefaultDisplay();
    	Point point = new Point();
    	display.getSize(point);
    	size[0] = point.x;
    	size[1] = point.y;
    	return size;
    }
    
    /**
     * 获取控件的尺寸大小,size[0]:view的宽，size[1]:view的高
     * @update 2014年10月29日 下午9:25:46
     * @param view
     * @return
     */
	public static int[] getViewSize(final View view) {
    	final int[] size = new int[2];
    	/*ViewTreeObserver vto = view.getViewTreeObserver();   
    	vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
    	    @SuppressWarnings("deprecation")
			@Override   
    	    public void onGlobalLayout() {
    	    	ViewTreeObserver obs = view.getViewTreeObserver();
    	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
    	    		obs.removeOnGlobalLayoutListener(this);
    	        } else {
    	        	obs.removeGlobalOnLayoutListener(this);
    	        }
    	    	size[0] = view.getWidth();
    	    	size[1] = view.getHeight();
    	    	Log.d("-----onGlobalLayout-----" + size[0] + "--,--" + size[1]);
    	    }   
    	});*/
    	
    	int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED); 
    	int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED); 
    	view.measure(w, h); 
    	int height = view.getMeasuredHeight(); 
    	int width = view.getMeasuredWidth();
    	size[0] = width;
    	size[1] = height;
    	return size;
    }
	
	/**
	 * 判断集合是否为空
	 * @update 2014年10月31日 下午3:22:19
	 * @param collection
	 * @return
	 */
	public static boolean isEmpty(Collection<?> collection) {
		if (collection != null && collection.size() > 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 判断一个数组是否为空
	 * @update 2014年10月31日 下午3:24:11
	 * @param array
	 * @return
	 */
	public static <T> boolean isEmpty(T[] array) {
		if (array != null && array.length > 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 格式化会话的时间，时间格式为MM-dd HH:mm
	 * @update 2014年10月31日 下午10:32:10
	 * @param time
	 * @return
	 */
	public static String formatMsgThreadTime(long time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMA_TPATTERN_THREAD, Locale.getDefault());
		return dateFormat.format(new Date(time));
	}
	
	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
	 * 
	 * @param context
	 * @param spannableString
	 * @param patten
	 * @param start
	 * @throws Exception
	 */
	private static void dealExpression(Context context,
			SpannableString spannableString, Pattern patten, int start)
			throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			//[大哭]
			String key = matcher.group();
			// 返回第一个字符的索引的文本匹配整个正则表达式,ture 则继续递归
			if (matcher.start() < start) {
				continue;
			}
			Emoji emoji = ChatApplication.getEmojiMap().get(key);
			if (emoji == null) {
				continue;
			}
			int resId = emoji.getResId();
			// 通过上面匹配得到的字符串来生成图片资源id
			if (resId != 0) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						context.getResources(), resId);
				bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, true);
				// 通过图片资源id来得到bitmap，用一个ImageSpan来包装
				ImageSpan imageSpan = new ImageSpan(context, bitmap);
				// 计算该图片名字的长度，也就是要替换的字符串的长度
				int end = matcher.start() + key.length();
				// 将该图片替换字符串中规定的位置中
				spannableString.setSpan(imageSpan, matcher.start(), end,
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				if (end < spannableString.length()) {
					// 如果整个字符串还未验证完，则继续。。
					dealExpression(context, spannableString, patten, end);
				}
				break;
			}
		}
	}
	
	/**
	 * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	public static SpannableString getExpressionString(Context context, String str) {
		SpannableString spannableString = new SpannableString(str);
		// 正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
		String zhengze = "\\[[^\\]]+\\]";
		// 通过传入的正则表达式来生成一个pattern
		Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
		try {
			dealExpression(context, spannableString, sinaPatten, 0);
		} catch (Exception e) {
			Log.e("dealExpression", e.getMessage());
		}
		return spannableString;
	}
	
	/**
	 * 获得可缓存的线程池
	 * @return
	 */
	public static ExecutorService getCachedThreadPool(){
		if(cachedThreadPool == null){
			cachedThreadPool = Executors.newCachedThreadPool();
		}
		return cachedThreadPool;
	}
	
	/**
	 * 获得一般的图片加载选项，用户会话列表、聊天界面等的图片显示，该选项没有磁盘缓存图片
	 * @update 2014年11月8日 上午11:43:13
	 * @return
	 */
	public static DisplayImageOptions getGeneralImageOptions() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.contact_head_icon_default)
			.showImageForEmptyUri(R.drawable.contact_head_icon_default)
			.showImageOnFail(R.drawable.contact_head_icon_default)
			.cacheInMemory(true)
			.cacheOnDisk(false)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.bitmapConfig(Bitmap.Config.RGB_565)	//防止内存溢出
			.displayer(new FadeInBitmapDisplayer(200))
			.build();
		return options;
	}
	
	/**
	 * 获得通讯录列表的特殊符号的选择器，特殊符号为：“↑”<br />
	 * <pre>
	 * String s = "↑";
	 * char c = s.charAt(0);
	 * System.out.println((int)c);
	 * </pre>
	 * @update 2014年11月8日 下午3:44:14
	 * @return
	 */
	public static int getContactListFirtSection() {
		return 8593;
	}
	
	/**
	 * 根据用户账号来包装成完整的jid,格式为:xxx@domain
	 * @update 2014年11月10日 下午8:47:14
	 * @param account 账号，格式为：xxx
	 * @return
	 */
	public static String wrapJid(String account) {
		if (!TextUtils.isEmpty(account)) {
			return account + "@" + Constants.SERVER_NAME;
		} else {
			return null;
		}
	}
	
	/**
	 * 将完整的jid托包装为账号，格式为：xxx
	 * @update 2014年11月10日 下午8:47:14
	 * @param jid 账号，格式为：xxx@doamin
	 * @return
	 */
	public static String unwrapJid(String jid) {
		if (!TextUtils.isEmpty(jid)) {
			return jid.substring(0, jid.indexOf("@"));
		} else {
			return null;
		}
	}
}
