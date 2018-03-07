package com.zong.zdb.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.zong.zdb.bean.Table;
import com.zong.zdb.service.JdbcCodeService;
import com.zong.zdb.service.TemplateRoot;

/**
 * @desc 代码生成类， btl文件按关键字归类： bean，controller，mapperjava，mapperxml， service，jsp_
 * @author zong
 * @date 2016年12月2日 上午1:07:55
 */
public class CreateCodeUtil {
	private final static Logger LOGGER = Logger.getLogger(CreateCodeUtil.class);

	public static String downCode(Table table, String objectName, String className, String packageName,
			String parentPath, String btlPath) {
		String codePath = "";
		try {
			TemplateRoot root = TemplateRoot.createTemplateRoot(table, objectName, className, packageName);
			// String parentPath = FileUtils.getClassResources();//
			// code文件夹父文件夹路径
			// String parentPath =
			// request.getServletContext().getRealPath("/static");
			codePath = parentPath + "/code";// 生成代码存放路径
			// String btlPath = FileUtils.getClassResources() + "btl/";
			// 先删除原来的
			FileUtils.removeAllFile(codePath);

			System.out.println("======下载" + table.getTableName() + "代码start======");
			// 获取所有模板
			List<File> btls = FileUtils.listFile(btlPath, "btl");
			for (File btl : btls) {
				// 根据模板名称归类分别处理
				String btlName = btl.getName().toLowerCase();
				String fileName = "";
				String filePath = "";
				if (btlName.indexOf("bean") > -1) {
					// 生成实体bean
					fileName = root.getString("className") + ".java";
					filePath = root.getPackageBeanPath();
				} else if (btlName.indexOf("controller") > -1) {
					// 生成controller
					fileName = root.getString("className") + "Controller.java";
					if (btlName.indexOf("json") > -1) {
						fileName = root.getString("className") + "ControllerJson.java";
					} else if (btlName.indexOf("view") > -1) {
						fileName = root.getString("className") + "ControllerView.java";
					}
					filePath = root.getPackageControllerPath();
				} else if (btlName.indexOf("mapperjava") > -1) {
					// 生成mapper.java
					fileName = root.getString("className") + "Mapper.java";
					filePath = root.getPackageMapperPath();
				} else if (btlName.indexOf("mapperxml") > -1) {
					// 生成mapper.xml
					fileName = root.getString("className") + "Mapper.xml";
					filePath = root.getPackageMapperPath();
				} else if (btlName.indexOf("service") > -1) {
					// 生成service
					fileName = root.getString("className") + "Service.java";
					filePath = root.getPackageServicePath();
					if (btlName.indexOf("impl") > -1) {
						fileName = root.getString("className") + "ServiceImpl.java";
						filePath = root.getPackageServicePath() + "/impl";
					}
				} else if (btlName.indexOf("jsp_") > -1) {
					// 生成jsp
					fileName = btlName.replace(".btl", ".jsp").replace("jsp_", root.getString("objectName") + "_");
					filePath = "jsp/" + root.getString("objectName");
				}
				if (!fileName.equals("")) {
					// Freemarker.printFile(btl.getName(), btlPath, root,
					// codePath + "/" + filePath + "/" + fileName);
					BeetlUtil.printFile(btlPath + btlName, root, codePath + "/" + filePath + "/" + fileName);
					LOGGER.info("create：" + codePath + "/" + filePath + fileName);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
		}
		return codePath;
	}

	public static void main(String[] args) {
		try {
			LOGGER.info("读取配置文件");
			Map configData = ZDBConfig.readConfig();
			List<Map> dbs = (List<Map>) configData.get(ZDBConfig.DBS);
			LOGGER.info(configData.toString());
			JdbcCodeService codeService = JdbcCodeService.getInstance();
			LOGGER.info("查询表结构：" + configData.get("tableName").toString());
			Table table = codeService.showTable(dbs.get(0).get("dbname").toString(),
					configData.get("tableName").toString());
			LOGGER.info("生成文件start");
			// 生成代码
			String btlPath = FileUtils.getClassResources() + "btl/";
			downCode(table, configData.get("objectName").toString(), configData.get("className").toString(),
					configData.get("packageName").toString(), FileUtils.getClassResources(), btlPath);
			LOGGER.info("生成文件end");
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
		}
	}
}
