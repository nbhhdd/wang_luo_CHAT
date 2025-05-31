package server;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    // 客户端与服务器通信的Socket连接
    private Socket socket;

    // 输入流，用于读取客户端发送的消息
    private BufferedReader reader;

    // 输出流，用于向客户端发送消息
    private PrintWriter writer;

    // 当前客户端的用户名（登录成功后赋值）
    private String username;
    // 用于存储所有在线用户
    private Map<String, ClientHandler> clients;

    // 改为绝对路径，确保打包运行时仍能找到文件
    // 设置用户信息保存路径为当前工作目录下的 users.txt
    private static final String USER_FILE = System.getProperty("user.dir") + File.separator + "users.txt";

    // 用于存储所有注册过的用户和密码（注册时写入，登录时读取验证）
    private static Map<String, String> userDatabase = new ConcurrentHashMap<>();
//    ConcurrentHashMap 是线程安全的集合，用于多线程环境。
//
//            System.getProperty("user.dir") 表示程序启动时所在目录

    // 加载用户数据
    static {
        loadUserDatabase();
    }
//    构造函数：客户端连接进来后调用
    public ClientHandler(Socket socket, Map<String, ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }
//    updateUserList()：用于更新所有在线用户的列表，并广播给所有客户端。
//    broadcast(String message)：向所有在线用户发送广播消息（如“某人已上线”）。
//    handleRegister(String line)：处理客户端发来的注册请求。
    @Override
//    run(): 处理注册、登录、消息分发
    public void run() {
        try {
            //客户端的输入输出
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String line = reader.readLine();
            if (line == null) return;
            //处理注册
            if (line.startsWith("REGISTER:")) {
                handleRegister(line);
                return;

                //登录
            } else if (line.startsWith("LOGIN:")) {
                if (!handleLogin(line)) return; // 登录失败直接断开
            } else {
                writer.println("非法请求格式");
                return;
            }
            //打印上线的人
            broadcast("[系统] " + username + " 已上线");
            updateUserList();

            //获取聊天消息，判断是 否是群聊消息还是私信
            String msg;
            while ((msg = reader.readLine()) != null) {
                if (msg.startsWith("[GROUP]")) {
                    //msg.substring(7)不打印[GROUP]
                    broadcast(username + ": " + msg.substring(7));
                } else if (msg.startsWith("[PRIVATE]")) {
                    handlePrivateMessage(msg.substring(9));
                }
            }
        } catch (IOException e) {
            System.out.println("连接异常: " + username);
        } finally {
            if (username != null) {
                //下线
                clients.remove(username);
                //向每个客户端打印
                broadcast("[系统] " + username + " 已下线");
                //更新 用户列表
                updateUserList();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //推送aa
        }
    }
// 注册函数
    private void handleRegister(String line) {
        String[] parts = line.split(":", 3);
        if (parts.length < 3) {
            writer.println("注册格式错误");
            return;
        }

        String uname = parts[1];
        String pwd = parts[2];

        if (userDatabase.containsKey(uname)) {
            writer.println("用户名已存在");
        } else {
            userDatabase.put(uname, pwd);
            saveUserToFile(uname, pwd);
            writer.println("SUCCESS");
            System.out.println("注册成功: " + uname);
        }
    }
    //登录函数
    private boolean handleLogin(String line) {
        String[] parts = line.split(":", 3);
        if (parts.length < 3) {
            writer.println("登录格式错误");
            return false;
        }

        String uname = parts[1];
        String pwd = parts[2];

        if (!userDatabase.containsKey(uname)) {
            writer.println("用户未注册");
            return false;
        }

        if (!userDatabase.get(uname).equals(pwd)) {
            writer.println("密码错误");
            return false;
        }

        if (clients.containsKey(uname)) {
            writer.println("该用户已在线");
            return false;
        }

        this.username = uname;
        //存放上线的人
        clients.put(username, this);
        writer.println("SUCCESS");
        return true;
    }
//处理私聊消息
    private void handlePrivateMessage(String body) {
        String[] parts = body.split(":", 3);
        if (parts.length < 3) return;

        String sender = parts[0];
        String receiver = parts[1];
        String content = parts[2];
        //找到目标用户
        ClientHandler target = clients.get(receiver);
        if (target != null) {
            target.writer.println("[私聊] " + sender + ": " + content);
        } else {
            writer.println("[系统] 用户 " + receiver + " 不在线。");
        }
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
//            遍历全局变量 clients 中的所有在线用户（每个用户对应一个 ClientHandler 实例）。
//            clients 是一个线程安全的 ConcurrentHashMap<String, ClientHandler>，键是用户名，值是对应的客户端处理器。
            //展示区域消息
            client.writer.println(message);
//            获取每个客户端的输出流（PrintWriter），并向其发送消息。
//            每个客户端收到该消息后会在界面上显示出来。
        }
    }
//    broadcast("Hello")
//│
//        ├─ 遍历 clients → 所有在线用户
//│   └─ 向每个用户的 writer 发送 "Hello"
//            └─ 所有用户界面都会显示这条消息

//    定义一个私有方法 updateUserList()，无返回值，不接受参数。
//    此方法通常在用户登录、登出或刷新用户列表时被调用。
    //更新用户列表， 显示当前在线用户列表
    private void updateUserList() {
//        创建一个 StringBuilder 实例，用于高效拼接字符串。
        StringBuilder sb = new StringBuilder();
//        遍历全局变量 clients 的键集合（即所有在线用户名）。
//        将每个用户名追加到 StringBuilder 后面，并加上逗号 , 分隔符。
//        示例输出：Alice,Bob,Charlie,
        for (String user : clients.keySet()) {
            sb.append(user).append(",");
        }
        if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
//        如果 StringBuilder 不为空，则删除最后一个多余的逗号。
//        示例变成：Alice,Bob,Charlie

//        遍历所有在线客户端的处理对象 ClientHandler。
//        通过每个客户端的输出流 PrintWriter 发送格式为 [USERLIST]Alice,Bob,Charlie 的消息。
//        客户端收到这个消息后会解析并更新界面上的在线用户列表。
        for (ClientHandler client : clients.values()) {
            client.writer.println("[USERLIST]" + sb);
        }
    }

    // 加载用户数据
    private static synchronized void loadUserDatabase() {
        File file = new File(USER_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    userDatabase.put(parts[0], parts[1]);
                }
            }
            System.out.println("用户数据加载完毕（共 " + userDatabase.size() + " 个）");
        } catch (IOException e) {
            System.err.println("读取用户数据失败: " + e.getMessage());
        }
    }

    // 保存用户数据
    private static synchronized void saveUserToFile(String username, String password) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            bw.write(username + ":" + password);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("保存用户失败: " + e.getMessage());
        }
    }
}



