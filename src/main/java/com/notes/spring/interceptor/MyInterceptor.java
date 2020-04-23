package com.notes.spring.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.notes.Swingclient;
import com.notes.spring.httpclient.JsonReplaceUtil;
import com.notes.spring.httpclient.SimpleHttpParam;
import com.notes.spring.httpclient.SimpleHttpResult;
import com.notes.spring.httpclient.SimpleHttpUtils;
import com.notes.utils.ReadConfigUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 文件描述 Interceptor拦截器
 **/
@Component
public class MyInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<Long> tl = new ThreadLocal<Long>();


    public  static String host = ReadConfigUtil.getProperty().get("configUrl")==null?null:ReadConfigUtil.getProperty().get("configUrl").toString();
    public  static String jsonStr = ReadConfigUtil.getProperty().get("jsonStr")==null?null:ReadConfigUtil.getProperty().get("jsonStr").toString();

    @Resource
    private Swingclient swingclient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("coming interceptor...");
        tl.set(System.currentTimeMillis());
        //返回为false将不会执行postHandle方法
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("code", "0");
        jsonObj.put("msg", "success");
        jsonObj.put("subCode", "0");
        jsonObj.put("subMsg", "success");
        jsonObj.put("data", "test");
        response.setContentType("text/xml;charset=UTF-8");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        String host1 ="http://apollo.meta";
        String url = host + request.getRequestURI();
        if(request.getQueryString()!=null){
            url = url + "?" + request.getQueryString();
        }
        SimpleHttpParam http = new SimpleHttpParam(url);
        http.setConnectTimeout(5000);
        http.setMethod("GET");
        SimpleHttpResult response1= SimpleHttpUtils.httpRequest(http);
        String html = response1.getContent();
        if(html!=null) {
            html= html.replaceAll(host, host1);
        }else {
            html="[]";
        }
        //String jsonStr = "{\"dubbo.registry.address\":\"127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183\"}";
        html = JsonReplaceUtil.dealWithResponse(html,jsonStr).toString();
        if(!html.equals("[]")) {
            swingclient.println("请求:" + url);
            swingclient.println("返回:" + html);
        }
        response.setContentType(response1.getContentType());

        if(response1.getStatusCode()!=200){
            response.setStatus(200);
            response.sendRedirect(url.replaceAll(host1, host));
        }
        try (PrintWriter writer = response.getWriter()) {
            writer.write(html);
        } catch (IOException e) {
        }
        return false;
    }

  /*  @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        System.out.println("interceptor耗时:" + (System.currentTimeMillis() - tl.get()) + "ms---URL:" + url);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }*/
}
