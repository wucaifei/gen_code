package com.qjwcf.generate;

import com.qjwcf.util.ExtendMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;

public class AutoCode {
    private String tableName = "";            //表名
    private String tableComment = "";            //表的注释
    private String moduleNameLower = "";        //模块名（小写）
    private String moduleNameUpper = "";        //模块名（大写）
    private String moduleNameCH = "";            //模块名（中文）

    private String pkName = "";                //主键字段名
    private String fields = "";                //表的所有字段，多个字段间以,间隔
    private String fieldsDetails = "";        //表字段的详细信息：由名称类型注释组成

    private String excelFields = "";            //Excel导入导出的字段信息

    private String templateDir;                //模板目录

    private String model = "assets";            //业务名
    private String actionUrl = model; // 访问action路径
    private String parentMenuName = "资产账单管理"; // 业务父菜单名
    private String sonMenuName = "资产账单管理"; // 业务子菜单名

    //HTML扩展
    private ArrayList<ExtendMap<String, String>> fieldList = new ArrayList<ExtendMap<String, String>>();

    public AutoCode(String tableName, String tableComment, String moduleName, String moduleNameCH) throws Exception {
        this.tableName = tableName;
        this.tableComment = tableComment;
        this.moduleNameLower = moduleName.substring(0, 1).toLowerCase() + moduleName.substring(1);
        this.moduleNameUpper = moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1);
        this.moduleNameCH = moduleNameCH;

        String substring = moduleNameLower.substring(model.length());
        if (substring.length() > 0) {
            actionUrl += "/";
            actionUrl += substring.substring(0, 1).toLowerCase() + substring.substring(1);
        }

//		templateDir=System.getProperty("user.dir")+"/src/template/";
        templateDir = "D:\\sks\\TemplateDemo";

        parseTableInfo();
    }

    public void display() throws Exception {
        Field fields[] = AutoCode.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName() + "：" + field.get(this));
        }
    }

    private static Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(Config.url, Config.userName, Config.password);
        return conn;
    }

    private static String buildSpace(int number) {
        String space = "";
        for (int i = 0; i < number; i++) space += " ";
        return space;
    }

    private void parseTableInfo() throws Exception {
        String selectTableInfo = "Select COLUMN_NAME,COLUMN_TYPE,COLUMN_COMMENT,COLUMN_KEY from INFORMATION_SCHEMA.COLUMNS "
                + "Where table_name='" + tableName + "' AND table_schema='" + Config.dbName + "'";

        Connection conn = getConnection();
        ResultSet rs = conn.createStatement().executeQuery(selectTableInfo);
        while (rs.next()) {
            String name = rs.getString("COLUMN_NAME");
            String type = rs.getString("COLUMN_TYPE");
            String comment = rs.getString("COLUMN_COMMENT");
            String key = rs.getString("COLUMN_KEY");

            fields += ("," + name);

            if (key.equals("PRI")) pkName = name;

            if (fieldsDetails.length() > 0) fieldsDetails += "\n * ";
            fieldsDetails += (name + buildSpace(25 - name.length()) + type + buildSpace(25 - type.length()) + comment);

            //HTML扩展
            ExtendMap<String, String> fieldMap = new ExtendMap<String, String>();
            fieldMap.put("nameEN", name);
            fieldMap.put("type", type);
            fieldMap.put("nameCH", comment.substring(0,
                    (comment.indexOf("：") > 0 ? comment.indexOf("：") : comment.length())
            ));
            fieldMap.put("comment", comment.substring(
                    (comment.indexOf("：") > 0 ? comment.indexOf("：") + 1 : 0)
            ));

            //导入导出扩展
            if (excelFields.length() > 0) excelFields += "\n\t";
            excelFields += ("<field code=\"" + name + "\" name=\"" + fieldMap.getString("nameCH") + "\"/>");

            fieldList.add(fieldMap);
        }
        fields = fields.substring(1);

        conn.close();
    }

    private String buildModuleListJsp(String source) {
        String code = source;
        StringBuilder _field = new StringBuilder();
        for (ExtendMap<String, String> fieldMap : fieldList) {
            String nameCH = fieldMap.get("nameCH");// 学生姓名
            String nameEN = fieldMap.get("nameEN");// student_name_
            _field.append("{\n" +
                    "                title: '" + nameCH + "',\n" +
                    "                field: '" + converStr(nameEN) + "',\n" +
                    "            },\n");
        }
        code = code.replaceAll("@fields", _field.toString());
        return code;
    }

    private String buildModuleAddJsp(String source) {
        String code = source;
        StringBuilder _field = new StringBuilder();
        for (ExtendMap<String, String> fieldMap : fieldList) {
            String nameCH = fieldMap.get("nameCH");// 学生姓名
            String nameEN = fieldMap.get("nameEN");// student_name_
            String converResult = converStr(nameEN);
            _field.append("<div class='form-group'>\n" +
                    "                                <label for='" + converResult + "' class='col-lb-1 control-label required'>" + nameCH + "</label>\n" +
                    "                                <div class='col-lb-2'>\n" +
                    "                                    <input class='form-control' type='text' id='" + converResult + "' name='" + converResult + "'/>\n" +
                    "                                </div>\n" +
                    "                            </div>");
        }
        code = code.replace("@inputs", _field.toString());
        return code;
    }

    private String buildModuleEditJsp(String source) {
        String code = source;
        StringBuilder _field = new StringBuilder();
        for (ExtendMap<String, String> fieldMap : fieldList) {
            String nameCH = fieldMap.get("nameCH");// 学生姓名
            String nameEN = fieldMap.get("nameEN");// student_name_
            String converResult = converStr(nameEN);
            _field.append("<div class='form-group'>\n" +
                    "                                <label for='" + converResult + "' class='col-lb-1 control-label required'>" + nameCH + "</label>\n" +
                    "                                <div class='col-lb-2'>\n" +
                    "                                    <input class='form-control' type='text' id='" + converResult + "' name='" + converResult + "'  value='${templatePo." + converResult + "}'/>\n" +
                    "                                </div>\n" +
                    "                            </div>");
        }
        code = code.replace("@inputs", _field.toString());
        return code;
    }

    private String buildModuleDetailJsp(String source) {
        String code = source;
        StringBuilder _field = new StringBuilder();
        for (ExtendMap<String, String> fieldMap : fieldList) {
            String nameCH = fieldMap.get("nameCH");// 学生姓名
            String nameEN = fieldMap.get("nameEN");// student_name_
            String converResult = converStr(nameEN);
            _field.append("<div class=\"form-group\">\n" +
                    "                                <label for=\"" + converResult + "\" class=\"col-lb-1 control-label required\">" + nameCH + "</label>\n" +
                    "                                <div class=\"col-lb-2\">\n" +
                    "                                    <input class=\"form-control\" type=\"text\" id=\"" + converResult + "\" name=\"" + converResult + "\" value=\"${templatePo." + converResult + "}\" " +
                    "readonly/>\n" +
                    "                                </div>\n" +
                    "                            </div>");
        }
        code = code.replace("@inputs", _field);
        return code;
    }

    private String buildModuleJS(String source) throws Exception {
        String code = source;

        String _fields = "'" + fields.replaceAll(",", "','") + "'";
        code = code.replaceFirst("@fields", _fields);

        String columns = "";
        String queryField = "";
        String textfieldColumn1 = "";
        String textfieldColumn2 = "";
        int count = 0;
        for (ExtendMap<String, String> fieldMap : fieldList) {
            if (count > 0) {
                columns += (",\n\t\t\t");
                queryField += (",\n\t\t\t\t\t\t");
            }
            columns += ("{header:'" + fieldMap.get("nameCH") + "',dataIndex:'" + fieldMap.get("nameEN")
                    + "',tooltip:'" + fieldMap.get("comment") + "',hidden:" + (count > 9) + "}");
            queryField += ("{id:'" + fieldMap.get("nameEN") + "',value:'" + fieldMap.get("nameCH") + "'}");

            if (count % 2 == 0) {
                if (textfieldColumn1.length() > 0) textfieldColumn1 += (",\n\t\t\t\t");
                textfieldColumn1 += ("{xtype:'textfield',fieldLabel:'" + fieldMap.get("nameCH") + "',name:'"
                        + fieldMap.get("nameEN") + "',anchor:'92%',listeners:fieldToolTip,tooltip:'" + fieldMap.get("comment") + "'}");
            } else {
                if (textfieldColumn2.length() > 0) textfieldColumn2 += (",\n\t\t\t\t");
                textfieldColumn2 += ("{xtype:'textfield',fieldLabel:'" + fieldMap.get("nameCH") + "',name:'"
                        + fieldMap.get("nameEN") + "',anchor:'92%',listeners:fieldToolTip,tooltip:'" + fieldMap.get("comment") + "'}");
            }
            count++;
        }
        code = code.replaceFirst("@columns", columns);
        code = code.replaceAll("@queryField", queryField);
        code = code.replaceFirst("@textfieldColumn1", textfieldColumn1);
        code = code.replaceFirst("@textfieldColumn2", textfieldColumn2);

//		code=code.replaceAll("@moduleName",moduleNameCH);
//		code=code.replaceFirst("@pkName",pkName);

        return code;
    }

    public void execute() throws Exception {
        File dir = new File(templateDir);
        execute(dir);
        System.out.println(tableName + "，模块代码已生成！！！");
    }

    private void execute(File dir) throws Exception {
        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                execute(file);
                continue;
            }

            String fileName = file.getName();

            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {//使用readLine方法，一次读一行
                if (content.length() > 0) content.append("\n");
                content.append(line);
            }
            br.close();

            String code = content.toString();

            code = code.replaceAll("@moduleName", moduleNameCH);
            code = code.replaceAll("@tableName", tableName);
            code = code.replaceFirst("@tableComment", tableComment);
            code = code.replaceFirst("@fieldsDetails", fieldsDetails);
            code = code.replaceAll("@pkName", pkName);

            code = code.replaceAll("@packagePath", Config.packagePath);
            code = code.replaceAll("@reqPath", Config.reqPath);

            code = code.replaceAll("@excelFields", excelFields);

            code = code.replaceAll("@model", model);

            if (fileName.toLowerCase().indexOf("template.js") >= 0) {//HTML扩展
                code = buildModuleJS(code);
            }
            if (fileName.toLowerCase().indexOf("list.jsp") >= 0) {//HTML扩展
                code = buildModuleListJsp(code);
            }
            if (fileName.toLowerCase().indexOf("add.jsp") >= 0) {//HTML扩展
                code = buildModuleAddJsp(code);
            }
            if (fileName.toLowerCase().indexOf("edit.jsp") >= 0) {//HTML扩展
                code = buildModuleEditJsp(code);
            }
            if (fileName.toLowerCase().indexOf("detail.jsp") >= 0) {//HTML扩展
                code = buildModuleDetailJsp(code);
            }


            code = code.replaceAll("template", moduleNameLower);
            code = code.replaceAll("Template", moduleNameUpper);

            code = code.replaceAll("@parentMenuName", parentMenuName);
            code = code.replaceAll("@sonMenuName", sonMenuName);
            code = code.replaceAll("@actionUrl", actionUrl);

            fileName = fileName.replaceFirst("template", moduleNameLower);
            fileName = fileName.replaceFirst("Template", moduleNameUpper);

            String outDir = dir.getAbsolutePath().replaceFirst("template", "export");
            outDir = outDir + "/out";
            File _outDir = new File(outDir);
            if (!_outDir.exists()) _outDir.mkdirs();

            File outfile = new File(outDir + "/" + fileName);
            boolean append = outfile.exists();
            if (!outfile.exists()) outfile.createNewFile();
            FileWriter fileWriter = new FileWriter(outfile, append);
            fileWriter.write((append ? "\n" : "") + code);
            fileWriter.close();
        }
    }

    /**
     * 批量自动生成代码
     *
     * @param tableNamePattern(传入具体表名，生成与表相对应的模块代码；传入模糊表名，生成所有与之匹配表的模块代码；传空生成该数据库所有表的模块代码)
     * @throws Exception
     */
    public static void batchBuild(String tableNamePattern) throws Exception {
        String selectAllTable = "Select TABLE_NAME,TABLE_COMMENT from INFORMATION_SCHEMA.TABLES "
                + "Where table_schema='" + Config.dbName + "'";
        if (tableNamePattern != null && tableNamePattern.length() > 0) {
            selectAllTable += (" and table_name like '" + tableNamePattern + "'");
        }

        Connection conn = getConnection();
        ResultSet rs = conn.createStatement().executeQuery(selectAllTable);
        while (rs.next()) {//遍历要自动生成代码的表
            //获取表的详细信息
            String _tableName = rs.getString("TABLE_NAME");
            String _tableComment = rs.getString("TABLE_COMMENT");
            String _moduleNameCH = (_tableComment.substring(_tableComment.length() - 1).equals("表")
                    ? _tableComment.substring(0, _tableComment.length() - 1) : _tableComment);

            String _moduleName = "";
            String ary[] = _tableName.split("_");
            if (ary.length > 1) {
                for (int i = Config.tableIndex; i < ary.length; i++) {
                    if (i > Config.tableIndex) {
                        _moduleName += (ary[i].substring(0, 1).toUpperCase() + ary[i].substring(1));
                    } else {
                        _moduleName += ary[i];
                    }
                }
            } else {
                _moduleName = _tableName;
            }

            //执行：自动生成表的模块代码
            AutoCode autoCode = new AutoCode(_tableName, _tableComment, _moduleName, _moduleNameCH);
            autoCode.execute();
        }

        conn.close();
    }

    /**
     * 根据一定的格式进行字符串转换，例如：传入student_demo_test，结果返回studentNameTest
     *
     * @param str 要转换的字符串
     * @return 被处理过返回的结果
     */
    private String converStr(String str) {
        String result = "";
        String ary[] = str.split("_");
        if (ary.length > 1) {
            for (int i = 0; i < ary.length; i++) {
                if (i > 0) {
                    result += (ary[i].substring(0, 1).toUpperCase() + ary[i].substring(1));
                } else {
                    result += ary[i];
                }
            }
        } else {
            result = str;
        }
        return result;
    }
}