package com.zong.zdb.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

/**
 * @desc 文件工具类
 * @author zong
 * @date 2016年11月26日 下午1:19:31
 */
public class FileUtils {

	/**
	 * 读取文本文件
	 * 
	 * @param filePath
	 * @return 文本内容
	 */
	public static String readTxt(String filePath) {
		StringBuffer sb = new StringBuffer();
		try {
			File file = new File(filePath);
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader br = new BufferedReader(read);
			String s = null;
			while ((s = br.readLine()) != null) {
				sb.append(s + "\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 写入文本内容到文件
	 * 
	 * @param filePath
	 * @param content
	 */
	public static void writeTxt(String filePath, String content) {
		try {
			File file = new File(filePath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			BufferedWriter bw = new BufferedWriter(write);
			bw.write(content);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回类根路径，后面带/
	 * 
	 * @return F:/work-eclipse-space/database-client/build/classes/
	 */
	public static String getClassResources() {
		URL url = FileUtils.class.getClassLoader().getResource("");
		if (File.separator.equals("/")) {
			return url.toString().replace("file:", "");
		}
		return url.toString().replace("file:/", "");
	}

	/**
	 * 返回项目路径
	 * 
	 * @return F:/work-eclipse-space/database-client/
	 */
	public static String getProjectPath() {
		return new StringBuffer(System.getProperty("user.dir").replaceAll("\\\\", "/")).append("/").toString();
	}

}
