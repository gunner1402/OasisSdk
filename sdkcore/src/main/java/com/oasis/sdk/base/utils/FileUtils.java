package com.oasis.sdk.base.utils;

import java.io.File;
import java.io.FileOutputStream;

import android.os.Environment;
import android.text.TextUtils;




/**
 * 读写文件
 * 
 * @author xdb
 * 
 */
public class FileUtils {
	static long MAXLENGTH = 1024*1024;// 1M *1024
	public static void writeLogToStore(String logs){
		try {
			File logFile = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_DOWNLOADS);
			logFile.mkdirs();
			
//			if(logFile.getUsableSpace() > MAXLENGTH){
//				logFile.delete();
//				logFile.mkdirs();
//			}
			
			logs = logs.replace("<br>", "\r\n");
			
			logFile = new File(logFile.getAbsolutePath(), TextUtils.isEmpty(SystemCache.GAMECODE)?"log.log":(SystemCache.GAMECODE+".log"));
			FileOutputStream fos = new FileOutputStream(logFile, true);
//			if(logFile.exists() && fos.getChannel().size() > MAXLENGTH){
//				logFile.delete();
//			}
//			fos.close();
//			
//			fos = new FileOutputStream(logFile, true);
//			BaseUtils.logDebug("OASISSDK", "log 创建完成"+fos.getChannel().position()+"   "+fos.getChannel().size());
			fos.write(logs.getBytes());
			fos.flush();
			fos.close();
		} catch (Exception e) {
//			e.printStackTrace();
			// 写文件失败
		}
	}
	/**
	 * 当应用退出时，删除该文件
	 */
	public static void deleteFileOnAppStartOrDestory(){
		File logFile = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DOWNLOADS);
		logFile = new File(logFile.getAbsolutePath(), "log.log");
		if(logFile.exists())
			logFile.delete();
	}
}