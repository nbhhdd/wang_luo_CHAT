package server;
//主服务器类
//package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

//    这个类负责监听端口、接收客户端连接，并为每个连接分配一个新的处理线程
    private static final int PORT = 12345;
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
//    用来存储所有在线用户及其对应的连接处理器。
//    PORT: 定义服务器监听的端口号。
//
//    clients: 保存当前所有在线用户，键是用户名，值是对应的客户端处理线程对象 ClientHandler。



    public static void main(String[] args) {
        System.out.println("服务器启动中...");
//        使用 try-with-resources 语法创建一个 ServerSocket 实例，绑定到预定义的端口 PORT（通常是 12345）。
//        这个 ServerSocket 将用于监听客户端的连接请求。
//        使用 try-with-resources 可确保在程序结束或发生异常时自动关闭 socket。

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
//                调用 accept() 方法，阻塞等待客户端连接。
//                一旦有客户端连接成功，就会返回一个 Socket 对象，表示与该客户端的通信通道。
                Socket clientSocket = serverSocket.accept();
                System.out.println("新连接: " + clientSocket.getInetAddress());
//                为每个新连接的客户端创建一个新的线程，并传入 ClientHandler 处理器。
//                ClientHandler 是一个实现了 Runnable 接口的类，负责处理该客户端的所有消息收发逻辑。
//                clients 是一个共享的在线用户列表（Map），用于广播消息或管理用户状态。
                //为新用户创建线程
                new Thread(new ClientHandler(clientSocket, clients)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    main() 启动
//│
//    ├─ 输出："服务器启动中..."
//    ├─ 创建 ServerSocket 监听 PORT
//└─ 进入循环等待客户端连接
//     │
//     ├─ accept() 阻塞等待
//     ├─ 收到新连接 → 获取 Socket
//     ├─ 创建 ClientHandler 线程处理这个客户端
//     └─ 回到 accept() 继续监听

}
