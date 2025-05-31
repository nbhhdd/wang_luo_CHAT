package client;

import javax.swing.*;//导入图像界面组件GUI编程
import java.awt.*;//awt布局管理
//io操作
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;//网络连接包
//构建登录界面GUI编程
public class LoginFrame extends JFrame {
    //文本输入框JTextField
    private JTextField usernameField;//用户名输入框
    //密码输入框
    private JPasswordField passwordField;//密码输入框
    //按钮类
    private JButton loginButton;//登录按钮
    private JButton registerButton;//注册按钮
//构造窗口初始化
    public LoginFrame() {
        //JFrame设置 的常用方法
        setTitle("用户登录");//设置窗口标题
        setSize(400, 220);//窗口大小
        //设置关闭窗口时的操作
//        public void setDefaultCloseOperation(int operation) {
//            if (operation != DO_NOTHING_ON_CLOSE &&
//                    operation != HIDE_ON_CLOSE &&
//                    operation != DISPOSE_ON_CLOSE &&
//                    operation != EXIT_ON_CLOSE) {
//                throw new IllegalArgumentException("defaultCloseOperation must be"
//                        + " one of: DO_NOTHING_ON_CLOSE, HIDE_ON_CLOSE,"
//                        + " DISPOSE_ON_CLOSE, or EXIT_ON_CLOSE");
//            }
//        DO_NOTHING_ON_CLOSE：不做任何事情，窗口保持打开状态。
//        HIDE_ON_CLOSE：隐藏窗口，但不释放资源。
//        DISPOSE_ON_CLOSE：释放窗口占用的资源。
//        EXIT_ON_CLOSE：退出整个应用程序。
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口居中模式
        setLocationRelativeTo(null); // 居中窗口
        //JTextField构造函数设置用户名长度
        usernameField = new JTextField(15);
        //密码输入框构造函数设置密码长度
        passwordField = new JPasswordField(15);
        //构造函数设置文本内容
        loginButton = new JButton("登录");
        //构造函数设置文本内容
        registerButton = new JButton("注册");

        JPanel panel = new JPanel(new GridBagLayout());// 主面板采用网格布局
        //控制组件位置
        GridBagConstraints gbc = new GridBagConstraints();     // 布局约束器
        gbc.insets = new Insets(8, 10, 8, 10);// 内边距
        gbc.fill = GridBagConstraints.HORIZONTAL;              // 水平拉伸控件
        gbc.anchor = GridBagConstraints.CENTER;                // 居中对齐


        // 用户名标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("用户名:"), gbc);

        // 用户名输入框
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // 密码标签
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("密 码:"), gbc);

        // 密码输入框
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // 按钮区域（横向放置两个按钮）
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // 跨两列
        panel.add(buttonPanel, gbc);

        add(panel);

        // 事件绑定
        //登录注册时间绑定
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> new RegisterFrame().setVisible(true));
    }

    //登录处理器，
    private void handleLogin() {
        //获取密码，用户名
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        //密码用户名为空处理策略
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码都不能为空");
            return;
        }
        //try进行io异常捕获，网络连接失败，读写错误
        try {
            //网络
            //构建网络对象
//            创建一个 Socket 实例，尝试连接到本机（localhost）地址的 12345 端口。
//            这个端口通常是自定义的服务器监听端口。
            Socket socket = new Socket("localhost", 12345);
//            获取 socket 的输入流（从服务器接收数据），并使用 InputStreamReader 将字节流转换为字符流。
//            再用 BufferedReader 包装，方便按行读取文本数据。
            //换从流加快速度
//            in：用来接收服务器发来的消息。
//            out：用来向服务器发送消息。
//            这就构成了一个简单的客户端-服务器交互模型，适用于自定义文本协议的网络通信。
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            获取 socket 的输出流（向服务器发送数据）。
//            使用 PrintWriter 包装，并设置自动刷新（true），表示每次调用 println() 会立即发送数据。
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//              发送登录请求，格式为协议约定字符串："LOGIN:用户名:密码"
            out.println("LOGIN:" + username + ":" + password);
//            服务器根据这个协议解析用户名和密码。
//            从服务器读取一行响应内容，通常是一个字符串，例如 "SUCCESS" 或 "登录失败：密码错误"。
//
//
//            等待服务器返回一行响应内容，例如 "SUCCESS" 或 "用户名或密码错误"。
            String response = in.readLine();

            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "登录成功！");
//                显示一个弹窗提示：“登录成功”。
                dispose();
                //然后进入新的聊天窗口
                new ChatFrame(username, socket, in, out);
            } else {
                //失败
                JOptionPane.showMessageDialog(this, response);
                //关闭通信通道
                socket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "连接服务器失败");
        }
    }
//    开始 try
//├─ 连接服务器
//├─ 获取输入/输出流
//├─ 发送登录请求
//├─ 读取响应
//├─ 成功？→ 显示成功 + 打开聊天界面
//└─ 失败？→ 显示错误信息 + 关闭连接
//catch 异常 → 提示连接失败

    public static void main(String[] args) {
        //显示窗口
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}


