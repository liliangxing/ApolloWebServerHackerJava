package com.notes;

/**
 * @Author: lilx
 * @Date: 2019/12/17 11:39
 * @Description:
 */

import com.notes.spring.interceptor.MyInterceptor;
import com.notes.utils.PropertiesUtils;
import com.notes.utils.ReadConfigUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class Swingclient extends JFrame  implements  ActionListener {
    private final static String path = ReadConfigUtil.path;
    private TrayIcon trayIcon;//托盘图标
    private SystemTray systemTray;//系统托盘  //

    public void minimizeToTray() {

        MouseAdapter iconAdap = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)//双击托盘窗口再现
                    setExtendedState(Frame.NORMAL);
                setVisible(true);
            }
        };
        try {
            if (SystemTray.isSupported()) // 判断当前平台是否支持系统托盘
            {
                systemTray = SystemTray.getSystemTray();// 获得系统托盘的实例
                URL path = Swingclient.class.getClassLoader().getResource("logo.png");
                System.out.println(path);
                Image imgae = ImageIO.read(path);
                trayIcon = new TrayIcon(imgae);
                systemTray.add(trayIcon);// 设置托盘的图标
                trayIcon.addMouseListener(iconAdap);//为图标设置鼠标监听器
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        WindowAdapter winAdap = new WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                dispose();//窗口最小化时dispose该窗口
            }
        };

        addWindowListener(winAdap);

    }

        public void actionPerformed(ActionEvent e)
    {
    }
    /**
     *
     */
    private static final long serialVersionUID = 2572235358190956651L;


    /**
     * 用户名
     */
    private JTextField configUrl;



    /**
     * 登录按钮
     */
    private JButton loginButton;




    /**
     * 聊天内容
     */
    private JTextArea chatContext;


    /**
     * 操作提示
     */
    private static JTextArea txtLogInfo;

    public static void println(String tips){
        txtLogInfo.append(tips+"\n");
    }

    public Swingclient() {
        minimizeToTray();
        getContentPane().setLayout(null);

        //登录部分
        JLabel lblIp = new JLabel("配置中心地址");
        lblIp.setFont(new Font("宋体", Font.PLAIN, 12));
        lblIp.setBounds(76, 60, 204, 15);
        getContentPane().add(lblIp);

        configUrl = new JTextField();
        configUrl.setBounds(159, 60, 252, 21);
        getContentPane().add(configUrl);
        configUrl.setColumns(10);


        Properties properties= ReadConfigUtil.getProperty();
        configUrl.setText( properties.getProperty("configUrl"));


        //登录
        loginButton = new JButton("保存配置");
        loginButton.setFont(new Font("宋体", Font.PLAIN, 12));


        loginButton.setBounds(315, 30, 95, 23);
        getContentPane().add(loginButton);



        //聊天内容框
        chatContext = new JTextArea();
        chatContext.setLineWrap(true);

        JScrollPane scrollBar = new JScrollPane(chatContext);
        scrollBar.setBounds(76, 96, 93, 403);
        scrollBar.setSize(336, 300);
        getContentPane().add(scrollBar);
        chatContext.setText( properties.getProperty("jsonStr"));


        //错误提示区域
        txtLogInfo = new JTextArea();
        JScrollPane scrollBar1 = new JScrollPane(txtLogInfo);
        txtLogInfo.setLineWrap(true);
        txtLogInfo.setBackground(Color.LIGHT_GRAY);
        txtLogInfo.setForeground(Color.darkGray);
        txtLogInfo.setCaretPosition(txtLogInfo.getText().length());
        txtLogInfo.paintImmediately(txtLogInfo.getBounds());
        txtLogInfo.setEditable(false);
        txtLogInfo.setFont(new Font("宋体", Font.PLAIN, 12));
        scrollBar1.setBounds(4, 406, 476, 250);
        getContentPane().add(scrollBar1);


        int weigh = 500;
        int heigh = 700;
        int w = (Toolkit.getDefaultToolkit().getScreenSize().width - weigh) / 2;
        int h = (Toolkit.getDefaultToolkit().getScreenSize().height - heigh) / 2;
        this.setLocation(w, h);
        this.setTitle("Apollo配置中心代理Java版");
        this.setSize(weigh, heigh);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);



        /**
         * 点击保存后，将数据写入配置文件
         */
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String configUrlVal = configUrl.getText().toString();
                String jsonStr = chatContext.getText();
                Map<String, String> data = new HashMap<>();
                data.put("configUrl", configUrlVal);
                data.put("jsonStr", jsonStr);
               if("".equals(configUrlVal)) {
                   Toolkit.getDefaultToolkit().beep();
                   JOptionPane.showMessageDialog(null, "配置中心地址不能为空！", "提示", JOptionPane.INFORMATION_MESSAGE);
               }else {
                   String msg = PropertiesUtils.setProperty(path, data);
                   if ("true".equals(msg)) {
                       MyInterceptor.host = configUrlVal;
                       MyInterceptor.jsonStr = jsonStr;
                       Toolkit.getDefaultToolkit().beep();
                       JOptionPane.showMessageDialog(null, "保存成功！", "提示", JOptionPane.PLAIN_MESSAGE);
                   } else {
                       Toolkit.getDefaultToolkit().beep();
                       JOptionPane.showMessageDialog(null, "保存异常！", "提示", JOptionPane.ERROR_MESSAGE);
                   }
               }

            }
        });
        //==============================================================//
    }
}