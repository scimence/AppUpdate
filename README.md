# AppUpdate

#### 介绍
安卓应用自更新代码逻辑。libs中引入AppUpdate.jar。

1、在update.txt中，记录应用最新的 版本、下载地址。如：https://scimence.gitee.io/CallShielder/update.txt 

2、在安卓应用中检测到版本变动时，下载应用、并提示更新。

示例：
String ConfigUrl = "https://scimence.gitee.io/CallShielder/update.txt";	// 服务端最新版本配置信息
String curVersion = "20190401";						// 当前版本信息
AppUpdate.CheckUpdate(this, ConfigUrl, curVersion);			// 检测版本自动更新

[安卓应用自更新](https://blog.csdn.net/scimence/article/details/88948937)