package com.qjwcf.generate;

public class Config {
	public static String url="jdbc:mysql://192.168.10.100:3306/sks_pro";
	public static String dbName="sks_pro";
	public static String userName="root";
	public static String password="aa123456";
	
	public static int tableIndex=1;
	
	public static String reqPath="/module";
	public static String packagePath="com.project.module";
	
//	public static String reqPath="/system";
//	public static String packagePath="com.tiger.system";
	
	public static void main(String args[]) throws Exception{
		//传入具体表名，生成与表相对应的模块代码；传入模糊表名，生成所有与之匹配表的模块代码；传空生成该数据库所有表的模块代码
		AutoCode.batchBuild("t_user_asset_record");
//		AutoCode.batchBuild("sys_%");
	}
}