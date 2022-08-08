package com.leisurexi.codeeditor.testssh;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.leisurexi.codeeditor.dto.ExecuteResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * ssh基本
 *
 * @author Administrator
 * @date 2022/07/06
 */
public class SshBasic {

    private String ipAddress;   //主机ip
    private String username;   // 账号
    private String password;   // 密码
    private int port;  // 端口号
    Session session = null;
    StringBuilder sb = null;

    public SshBasic(String ipAddress, String username, String password, int port) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    /**
     * 连接到指定的ip
     */
    public void connect() {

        try {
            JSch jsch = new JSch();
            if (port < 0 || port > 65535) {
                //连接服务器，如果端口号错误，采用默认端口
                session = jsch.getSession(username, ipAddress);
            } else {
                session = jsch.getSession(username, ipAddress, port);
            }
            //设置登录主机的密码
            session.setPassword(password);
            //如果服务器连接不上，则抛出异常
            if (session == null) {
                throw new Exception("session is null");
            }
            //设置首次登录跳过主机检查
            session.setConfig("StrictHostKeyChecking", "no");
            //设置登录超时时间
            session.connect(3000);
        } catch (Exception e) {
        }
    }


    /**
     * 执行相关的命令
     *
     * @param command
     * @return
     */
    public String execute(List<String> commands) throws IOException {
        connect();
        int returnCode = 0;
        ChannelShell channel = null;
        PrintWriter printWriter = null;
        BufferedReader input = null;
        sb = new StringBuilder();
        try {
            //建立交互式通道
            channel = (ChannelShell) session.openChannel("shell");
            channel.connect();

            //获取输入
            InputStreamReader inputStreamReader = new InputStreamReader(channel.getInputStream());
            input = new BufferedReader(inputStreamReader);

            //输出
            printWriter = new PrintWriter(channel.getOutputStream());
            for (String command : commands) {
                printWriter.println(command);
            }
            printWriter.println("exit");
            printWriter.flush();
            String line;
            while ((line = input.readLine()) != null) {
                if (line.startsWith("[") || commands.contains(line) ||
                        "exit".equals(line) || "logout".equals(line) || line.startsWith("Last login")
                        || "".equals(line)) {
                    continue;
                }
                sb.append(line + "\n");
            }
        } catch (Exception e) {
            return sb.toString();
        } finally {
            printWriter.close();
            input.close();
            if (channel != null) {
                //关闭通道
                channel.disconnect();
            }
        }
        return sb.toString();
    }

    public void close() {
        if (session != null) {
            session.disconnect();
        }
    }

    public static ExecuteResults inputCode(String code) {
        String ls = "";
        SshBasic sshBasic = null;
        SshBasic finalSshBasic = sshBasic;
        synchronized (SshBasic.class) {
            try {
                sshBasic = new SshBasic("", "root", "", 22);
                List<String> list = new ArrayList<>();
                list.add("cd uxn");
                list.add("cd bin");
                list.add("echo '' > hello.tal");
                String[] commod = code.split("\n");
                for (String s : commod) {
                    list.add("echo " + "\"" + s + "\"" + ">> hello.tal");
                }
                list.add("./uxnasm hello.tal hello.rom");
                list.add("./uxncli hello.rom");
                ls = sshBasic.execute(list);
            } catch (IOException e) {
                return new ExecuteResults(false, new ArrayList(), "编译失败！请检查语法！");
            } finally {
                if (sshBasic != null) {
                    sshBasic.close();
                }
            }
        }
        return new ExecuteResults(true, null, ls);
    }

    public static void main(String[] args) throws IOException {
        String code = "( hola.tal ) \n" +
                "|0100 LIT 'h #18 DEO\n" +
                "      LIT 'o #18 DEO\n" +
                "      LIT 'l #18 DEO\n" +
                "      LIT 'a #18 DEO\n" +
                "      #0a #18 DEO ( nuevalínea )";
        ExecuteResults executeResults = inputCode(code);
        System.out.println(executeResults.getStdout());
    }
}
