package com.notes.utils;

/**
 * @Author: lilx
 * @Date: 2019/12/17 14:20
 * @Description:
 */


import java.util.Properties;


public class ReadConfigUtil {

    public final static String path =  "/opt/data/apolloConfig/config.txt";

    public static Properties getProperty(){
      return  PropertiesUtils.getProperty(path);
    }


}