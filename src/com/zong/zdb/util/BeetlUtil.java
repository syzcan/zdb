package com.zong.zdb.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;
import org.beetl.core.resource.StringTemplateResourceLoader;

/**
 * @desc beetl模板工具类
 * @author zong
 * @date 2017年3月17日
 */
public class BeetlUtil {
	/**
	 * 渲染模板返回字符串结果
	 * 
	 * @param btlPath 模板实际全路径
	 * @param binding 模板替换变量map
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static String printString(String btlPath, Map binding) throws IOException {
		btlPath = btlPath.replaceAll("\\\\", "/");
		String fileName = btlPath.substring(btlPath.lastIndexOf("/"));
		String root = btlPath.replace(fileName, "");
		FileResourceLoader resourceLoader = new FileResourceLoader(root, "utf-8");
		Configuration cfg = Configuration.defaultConfiguration();
		GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
		Template t = gt.getTemplate(fileName);
		t.binding(binding);
		return t.render();

	}

	/**
	 * 渲染模板写入文件
	 * 
	 * @param btlPath 模板实际全路径
	 * @param binding 模板替换变量map
	 * @param filePath 生成文件路径
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static void printFile(String btlPath, Map binding, String filePath) throws IOException {
		btlPath = btlPath.replaceAll("\\\\", "/");
		String fileName = btlPath.substring(btlPath.lastIndexOf("/"));
		String root = btlPath.replace(fileName, "");
		FileResourceLoader resourceLoader = new FileResourceLoader(root, "utf-8");
		Configuration cfg = Configuration.defaultConfiguration();
		GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
		Template t = gt.getTemplate(fileName);
		t.binding(binding);
		String content = t.render();
		// 写入文件
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		FileUtils.writeTxt(filePath, content);
	}

	/**
	 * 渲染模板字符串返回结果
	 * 
	 * @param btl 模板字符串
	 * @param binding 模板替换变量map
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static String printBtl(String btl, Map binding) throws IOException {
		StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
		Configuration cfg = Configuration.defaultConfiguration();
		GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
		Template t = gt.getTemplate(btl);
		t.binding(binding);
		return t.render();

	}
	
}
