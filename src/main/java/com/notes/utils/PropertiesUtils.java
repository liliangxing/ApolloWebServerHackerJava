package com.notes.utils;

/**
 * @Author: lilx
 * @Date: 2019/12/17 14:20
 * @Description:
 */

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


public class PropertiesUtils {

    /**
     * 通过配置文件名获取配置信息
     * @param fileName
     * @return
     */
    public static Properties getProps(String fileName){
        Properties props = new Properties();
        try {
            // 只需要文件名 dbconfig.properties与resource/dbconfig.properties的区别
            props.load(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    /**
     * 通过配置文件路径获取配置信息
     * @param fileName
     * @return
     */
    public static Properties getProperty(String fileName) {
        //第一步是取得一个Properties对象
        Properties props = new Properties();
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        //第二步是取得配置文件的输入流
        //InputStream is = PropertiesUtils.class.getClassLoader().getResourceAsStream("dbconfig.properties");//在非WEB环境下用这种方式比较方便
        try {
            InputStream input = new FileInputStream(fileName);
            // 第三步是把配置文件的输入流load到Properties对象中，
            props.load(input);
            // 注意两种的区别
            //props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return props;
    }

    /**
     * properties获取key值
     * @param fileName
     * @param key
     * @return
     */
    public static String getProperty(String fileName, String key) {
        String value = "";
        //第一步是取得一个Properties对象
        Properties props = new Properties();
        //第二步是取得配置文件的输入流
        //InputStream is = PropertiesUtils.class.getClassLoader().getResourceAsStream("dbconfig.properties");//在非WEB环境下用这种方式比较方便
        try {
            //InputStream input = new FileInputStream("dbconfig.properties");//在WEB环境下用这种方式比较方便，不过当配置文件是放在非Classpath目录下的时候也需要用这种方式
            //第三步讲配置文件的输入流load到Properties对象中，这样在后面就可以直接取来用了
            props.load(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            value = props.getProperty(key);
            //is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * properties写入key值
     * @param fileName
     * @param data
     */
    public static String setProperty(String fileName, Map<String, String> data) {
        String message = "true";

        // 第一步也是取得一个Properties对象
        Properties props = new Properties();
        // 第二步也是取得该配置文件的输入流
        // InputStream is = PropUtil.class.getClassLoader().getResourceAsStream("dbconfig.properties");
        try {
            InputStream input = new FileInputStream(fileName);
            // 第三步是把配置文件的输入流load到Properties对象中，
            // props.load(new InputStreamReader(PropertiesUtils.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
            props.load(input);
            // 接下来就可以随便往配置文件里面添加内容了
            // props.setProperty(key, value);
            if (data != null) {
                Iterator<Entry<String, String>> iter = data.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, String> entry = iter.next();
                    props.setProperty(entry.getKey().toString(), entry.getValue().toString());
                }
            }
            // 在保存配置文件之前还需要取得该配置文件的输出流，切记，如果该项目是需要导出的且是一个非WEB项目，
            // 则该配置文件应当放在根目录下，否则会提示找不到配置文件
            OutputStream out = new FileOutputStream(fileName);
            // 最后就是利用Properties对象保存配置文件的输出流到文件中;
            props.store(out, null);
            input.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            message = "false";
            e.printStackTrace();
        }

        return message;
    }


    public static void main(String[] args) {
        System.out.println(PropertiesUtils.getProps("dbconfig.properties"));
//        System.out.println(PropertiesUtils.getProperty("resource/dbconfig.properties"));
//        Map<String, String> data = new HashMap<String, String>();
//        data.put("db.db_type", "oracle1");
//        data.put("db.username", "root1");
//        data.put("db.password", "root1");
//        PropertiesUtils.setProperty("resource/dbconfig.properties", data);
    }

}