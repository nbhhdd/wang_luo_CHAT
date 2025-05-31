package client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket;                  // 客户端 socket，连接到服务器
    private BufferedReader reader;          // 输入流：接收服务器发来的消息
    private PrintWriter writer;             // 输出流：发送消息到服务器
    private String username;                // 当前客户端的用户名
    private Thread receiverThread;          // 用于接收消息的后台线程
//    连接服务器并初始化
    public ChatClient(String host, int port, String username) throws IOException {
        this.username = username;
        connectToServer(host, port);
    }
//    连接服务器方法
    private void connectToServer(String host, int port) throws IOException {
//        创建一个客户端 Socket 实例，尝试连接指定主机和端口上的服务器。
//        如果连接成功，则建立了一个 TCP 连接通道；如果失败会抛出异常。
        socket = new Socket(host, port);
//        获取 socket 的输入流（从服务器接收数据），并使用 InputStreamReader 将字节流转换为字符流。
//        指定编码为 "UTF-8"，确保能正确处理中文等字符。
//        再用 BufferedReader 包装，方便按行读取文本数据
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//        获取 socket 的输出流（向服务器发送数据）。
//        使用 OutputStreamWriter 将字符流转换为字节流，同样指定 "UTF-8" 编码。
//        使用 PrintWriter 包装，并设置自动刷新（true），表示每次调用 println() 会立即发送数据。
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        writer.println(username); // 登录时发送用户名
    }

    /**
     * 发送消息到服务器
     */
    public void sendMessage(String msg) {
        if (writer != null) {
            writer.println(msg);
        }
    }

    /**
     * 异步接收服务器发来的消息
     */
    public void receiveMessages(Consumer<String> onMessage) {
//        创建一个新的线程对象，用于在后台持续监听服务器发来的消息。
//        使用 Lambda 表达式定义线程任务体。
        receiverThread = new Thread(() -> {
            try {
                String msg;
                while ((msg = reader.readLine()) != null) {
//                    每次读取到消息后，调用 onMessage.accept(msg)，将消息传递给上层逻辑处理。
                    onMessage.accept(msg);
                }
            } catch (IOException e) {
                onMessage.accept("服务器断开连接");
//                不论是否异常，最终都会执行 close() 方法，释放资源（关闭 socket 和流）。
            } finally {
                close();
            }
        });
//        设置该线程为守护线程（daemon thread），意味着当主线程退出时，这个线程会自动终止，不会阻塞程序退出。
        receiverThread.setDaemon(true); // 确保程序关闭时线程不会阻塞
        receiverThread.start();
    }

    /**
     * 关闭连接资源
     */
    public void close() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }
}

