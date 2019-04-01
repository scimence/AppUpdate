package com.sc.update;



import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.sc.update.ThreadTool.ThreadPram;


/** WebTool.java: 网页信息获取，可在主线程中调用
* 
* 1、byte[] 	GetBytes(final String url)
* 2、String 	GetString(String dataUrl)
* 3、JSONObject 	GetJSONObject(String jsonUrl)
* 4、Bitmap 	GetBitmap(String imgUrl)
* 5、Drawable 	GetDrawable(String imgUrl)
* 
* ----- 2018-6-7 上午11:00:03 scimence */
public class WebTool
{
	// 缓存Drawable图像
	private static HashMap<String, Drawable> DrawableDic = new HashMap<String, Drawable>();
	
	/** 从网络上下载图片,转为Drawable */
	public static Drawable GetDrawable(String imgUrl)
	{
		Drawable drawable = null;
		
		if (DrawableDic.containsKey(imgUrl))
			drawable = DrawableDic.get(imgUrl);	// 从缓存读取图像
		else
		{
			Bitmap bmp = GetBitmap(imgUrl);						// 从服务器端下载图像
			if (bmp != null) drawable = Bitmap2Drawable(bmp);			// 转化为Drawable
			if (drawable != null) DrawableDic.put(imgUrl, drawable);		// 记录图像
		}
		
		return drawable;
	}
	
	/** 从网络上下载图片资源 */
	public static Bitmap GetBitmap(String imgUrl)
	{
		Bitmap bmp = null;
		try
		{
			byte[] data = GetBytes(imgUrl);								// 下载数据
			bmp = BitmapFactory.decodeByteArray(data, 0, data.length);	// 载入Bitmap
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return bmp;
	}
	
	/** Bitmap转化为Drawable */
	public static Drawable Bitmap2Drawable(Bitmap bitmap)
	{
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}
	
	/** Drawable转化为Bitmap */
	public static Bitmap Drawable2Bitmap(Drawable drawable)
	{
		BitmapDrawable bitDrawable = (BitmapDrawable) drawable;
		return bitDrawable.getBitmap();
	}
	
	
	/** 获取指定网址的数据 */
	public static String GetString(String dataUrl)
	{
		String Str = "";
		try
		{
			byte[] data = GetBytes(dataUrl);	// 下载数据
			Str = new String(data);				// 转化为字符串
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return Str;
	}

	/** 获取指定网址的数据为JSON */
	public static JSONObject GetJSONObject(String jsonUrl)
	{
		String webData = WebTool.GetString(jsonUrl);
		JSONObject webJson = null;
		try
		{
			webJson = new JSONObject(webData);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return webJson;
	}
	
	
	
	// ------------------------
	// 网络数据载入
	// ------------------------
	
	static HashMap<Long, byte[]> GetBytesDic = new HashMap<Long, byte[]>();
	
	/** 获取指定网址的数据，函数可在任意线程中执行，包括主线程 */
	public static byte[] GetBytes(final String url)
	{
		final long KEY = System.currentTimeMillis();
		if (!GetBytesDic.containsKey(KEY)) GetBytesDic.put(KEY, null);
		
		// 在非主线程中执行网络请求，获取数据
		ThreadTool.RunInCachedThread(new ThreadPram()
		{
			@Override
			public void Function()
			{
				byte[] data = GetBytes_process(url);
				GetBytesDic.put(KEY, data);
			}
		});
		
		// 等待异步线程中的网络请求逻辑执行完成
		while (GetBytesDic.get(KEY) == null) 	// 未获取到数据则
		{
			if (System.currentTimeMillis() > KEY + 1000 * 3) break;	// 超出3秒则终止
			Sleep(50); // 延时等待异步线程逻辑执行完成
		}
		
		byte[] data = GetBytesDic.get(KEY);
		GetBytesDic.remove(KEY);
		
		return data;
	}
	
	/** 获取指定网址的数据 */
	public static byte[] GetBytes_process(String url)
	{
		byte[] data = new byte[0];
		try
		{
			URL webUrl = new URL(url);
			URLConnection con = webUrl.openConnection();	// 打开连接
			InputStream in = con.getInputStream();			// 获取InputStream
			
			data = InputStreamToByte(in);					// 读取输入流数据
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return data;
	}

	/** InputStream -> Byte */
	public static final byte[] InputStreamToByte(InputStream in)
	{
		byte[] bytes = {};
		
		try
		{
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int count = 0;
			while ((count = in.read(data, 0, 1024)) > 0)
			{
				byteOutStream.write(data, 0, count);
			}
			
			bytes = byteOutStream.toByteArray();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return bytes;
	}
	
	/** 当前线程延时毫秒 */
	private static void Sleep(long timeMillion)
	{
		try
		{
			Thread.sleep(timeMillion);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
}

