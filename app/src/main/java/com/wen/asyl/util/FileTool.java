package com.wen.asyl.util;



import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;

/**
 * Description：上传工具类 <br/>
 * Copyright (c) 2017<br/>
 * This program is protected by copyright laws <br/>
 * Date:2017/8/19 16:44
 *
 * @author 姜文莒
 * @version : 1.0
 */
public class FileTool {
    /**
     * Description: 向FTP服务器上传文件
     *
     * @param url
     *            FTP服务器hostname
     * @param port
     *            FTP服务器端口
     * @param username
     *            FTP登录账号
     * @param password
     *            FTP登录密码
     * @param path
     *            FTP服务器保存目录，是linux下的目录形式,如/photo/
     * @param filename
     *            上传到FTP服务器上的文件名,是自己定义的名字，
     * @param input
     *            输入流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String url, int port, String username,
                                     String password, String path, String filename, InputStream input) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
       // LogUitl.Infor(url+port+username+password+path);


        try {
            int reply;
            ftp.connect(url, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.setFileType(FTP.BINARY_FILE_TYPE);//上传上去的图片数据格式（）一定要写这玩意，不然在服务器就打不开了
            if (!ftp.changeWorkingDirectory(path)) {
                if (ftp.makeDirectory(path)) {
                    ftp.changeWorkingDirectory(path);
                }
            }
           //  ftp.changeWorkingDirectory(path);
            //设置成其他端口的时候要添加这句话
          //  ftp.enterLocalPassiveMode();
            ftp.storeFile(filename, input);
            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }
}
