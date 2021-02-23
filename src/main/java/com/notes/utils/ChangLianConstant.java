package com.notes.utils;

/**
 * 自定义配置常量定义类
 *
 * @author Liugl
 * @version 2019/3/22 17:56,Liugl,Ins
 * @copyright 2019/3/22 17:56
 */
public class ChangLianConstant {

    /**
     * 畅联方渠道编码，由畅联提供给渠道使用，待对接前确认 【渠道代码】
     */
    public static String ENTITY_ID = "ZHUHAIEST";
    /**
     * 渠道用户名
     */
    public static String CHANNEL_USER_NAME  = "7716804";
    /**
     * 渠道加密key值
     */
    public static String ENTITY_KEY = "11Kv3GfsDV2RnuNriHwug2sL7wcsDdUh";
    /**
     * 畅联方集团编码
     */
    public static String CHAIN_CODE = "CCM";
    /**
     * 担保类型（信用卡担保 会有信用卡信息节点）
     */
    public static String GUARANTEE_TYPE = "TAGTD";

    /**
     * webservice请求接口地址
     */
    public static String AVAILABILITY_URL = "https://col-test.shijicloud.com/Col_switch_ws/Availability.asmx?wsdl";
    public static String RESERVATION_URL = "https://col-test.shijicloud.com/Col_switch_ws/Reservation.asmx?wsdl";
    /**
     * SOAPAction地址
     */
    public static String AVAILABILITY_SOAPACTION = "http://webservices.micros.com/ows/5.1/Availability.wsdl#Availability";
    public static String CREATEBOOKING_SOAPACTION = "http://webservices.micros.com/ows/5.1/Reservation.wsdl#CreateBooking";
    public static String MODIFYBOOKING_SOAPACTION = "http://webservices.micros.com/ows/5.1/Reservation.wsdl#ModifyBooking";
    public static String CANCELBOOKING_SOAPACTION = "http://webservices.micros.com/ows/5.1/Reservation.wsdl#CancelBooking";

}
