package com.sc.update;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.sc.update.DownloadTool.CallBack;
import com.sc.update.ThreadTool.ThreadPram;

/** 应用自更新：
 * 1、修改ConfigUrl为：待更新应用对应的update.txt地址；
 * 2、在curVersion记录当前应用版本信息；
 * 3、在应用启动时，调用检测更新：AppUpdate.CheckUpdate(this, "", ""); 
 *  */
public class AppUpdate
{	
	//	update.txt中配置应用的最新：版本信息、下载地址
	//	version(20190401)
	//	url(https://scimence.gitee.io/CallShielder/CallShielder.apk)

	static String mConfigUrl = "https://scimence.gitee.io/CallShielder/update.txt";
	static String mCurVersion = "20190401";
	static String msg = "";
	
	/** 应用版本检测，自动更新 */
	public static void CheckUpdate(final Context context, String ConfigUrl, String CurVersion)
	{
		if(ConfigUrl != null && !ConfigUrl.equals("")) mConfigUrl = ConfigUrl;
		if(CurVersion != null && !CurVersion.equals("")) mCurVersion = CurVersion;
		
		ThreadTool.RunInCachedThread(new ThreadPram()
		{
			@Override
			public void Function()
			{
				String configData = WebTool.GetString(mConfigUrl);			// 获取版本配置文件中的信息
				String version = getNodeData(configData, "version", true);	// 版本信息
				String url = getNodeData(configData, "url", true);			// 最新版apk地址
				msg = getNodeData(configData, "msg", true);					// 更新安装时，提示信息
				
				if(!url.equals("") && !mCurVersion.equals(version))	 // 若当前版本非最新版
				{
					Download(context, url);
				}
			}
		});
	}
	
	/**
	 * 从自定义格式的数据data中，获取key对应的节点数据
	 * RegisterPrice(1)RegisterPrice
	 */
    public static String getNodeData(String data, String key, boolean finalNode)
    {
        try
        {
            String S = key + "(", E = ")" + (finalNode ? "" : key);
            int indexS = data.indexOf(S) + S.length();
            int indexE = data.indexOf(E, indexS);

            return data.substring(indexS, indexE);
        }
        catch (Exception ex) { return data; }
    }
    
	/** 下载并自动提示安装 */
	public static void Download(final Context context, String url)
	{
		String apkName = url.substring(url.lastIndexOf("/") + 1);	// 解析apk文件名称
		final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sc/apps/" + apkName;
		
		CallBack call = new CallBack()
		{
			@Override
			public void F()
			{
				Install(context, filePath);					// 下载完成后，执行安装
			}
		};
		
		DownloadTool.DownloadFile(url, filePath, call);    	// 从网络下载文件到本地
	}
	
	/** 安装apk包 */
	public static void Install(final Context context, final String filePath)
	{
		ThreadTool.RunInMainThread(new ThreadPram()
		{
			@Override
			public void Function()
			{
				Toast.makeText(context, "检测到新版本！" + msg, Toast.LENGTH_SHORT).show();
				
				File apkFile = new File(filePath);
				Uri data = Uri.fromFile(apkFile);
				
				Intent intent = new Intent();
		        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        intent.setAction(android.content.Intent.ACTION_VIEW);
		        intent.setDataAndType(data, "application/vnd.android.package-archive");
		        context.startActivity(intent);
			}
		});
		
	}
}
