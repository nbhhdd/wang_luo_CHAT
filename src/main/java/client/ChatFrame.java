package client;
//聊天界面
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatFrame extends JFrame {
    private String username;//当前用户名
    private JTextArea messageArea;//显示群聊消息
    private JTextField inputField;//消息输入框
    private JButton sendButton;//发送按钮
    private DefaultListModel<String> userListModel;//用户在线数模型
    private JList<String> userList;//用户组件列表

    private Socket socket;//客户端
    private BufferedReader reader;//服务器输入流
    private PrintWriter writer;//服务器输出流
//构造器初始化整个界面
    public ChatFrame(String username, Socket socket, BufferedReader reader, PrintWriter writer) {
        this.username = username;
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;

        setTitle("群聊 - 用户: " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);//居中

        // 消息显示区域
        //创建一个文本区域组件，用来显示聊天记录
        messageArea = new JTextArea();

//        设置为不可编辑（只读），防止用户直接修改聊天内容
        messageArea.setEditable(false);
        //创建一个带滚动条的面板，包裹 messageArea，实现自动滚动
        JScrollPane scrollPane = new JScrollPane(messageArea);

        //获取文本区的光标对象（Caret）/
        DefaultCaret caret = (DefaultCaret) messageArea.getCaret();

//        设置光标策略为始终更新，确保新消息自动滚动到底部
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // 输入 + 发送按钮
        // 创建一个文本输入框，用户可以在此输入聊天内容
        inputField = new JTextField();
        // 创建一个“发送”按钮
        sendButton = new JButton("发送");

// 创建一个面板，使用 BorderLayout 布局管理器
// BorderLayout 可以按方位（东、南、西、北、中）放置组件
        JPanel inputPanel = new JPanel(new BorderLayout());
        // 将输入框放在面板的中间位置（占据大部分空间）
        inputPanel.add(inputField, BorderLayout.CENTER);
        // 将“发送”按钮放在面板的右侧（东边
        inputPanel.add(sendButton, BorderLayout.EAST);

        // 在线用户列表
//        创建一个 DefaultListModel<String> 对象，用于存储在线用户的用户名列表。
        userListModel = new DefaultListModel<>();
//        创建一个 JList<String> 组件，并将上面创建的 userListModel 设置为其数据模型。
//        JList 是 Swing 中的一个组件，用于显示一组字符串项（这里是在线用户列表）。
        userList = new JList<>(userListModel);
//        创建一个带滚动条的面板 JScrollPane，并将 userList 添加进去。
//        当在线用户数量过多时，会自动出现滚动条，方便查看所有用户。
       JScrollPane userScrollPane = new JScrollPane(userList);
//        设置这个滚动面板的首选宽度为 120 像素，高度不限制（0 表示由布局决定）。
//        确保用户列表区域不会过大或过小，保持合理的 UI 比例。
        userScrollPane.setPreferredSize(new Dimension(120, 0));

        // 布局
        add(scrollPane, BorderLayout.CENTER);    // 消息区中间
        add(inputPanel, BorderLayout.SOUTH);     // 输入区底部
        add(userScrollPane, BorderLayout.EAST);  // 用户列表右侧

        // 发送消息事件//按钮绑定事件
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // 私聊（双击用户名）
        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String targetUser = userList.getSelectedValue();
                    if (targetUser != null && !targetUser.equals(username)) {
                        new PrivateChatFrame(username, targetUser, socket);
                    }
                }
            }
        });

        // 启动接收线程
        startMessageReceiver();
        //显示窗口
        setVisible(true);
    }

    private void sendMessage() {
        //发送文本消息
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            writer.println("[GROUP]" + msg);
            inputField.setText("");
        }
    }

    private void updateUserList(String userListStr) {
//        使用 SwingUtilities.invokeLater() 确保以下操作在 Swing 的事件调度线程（Event Dispatch Thread） 上执行。
//        因为 Swing 不是线程安全的，任何 UI 更新都必须在这个线程中进行。
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : userListStr.split(",")) {
                userListModel.addElement(user);
            }
        });
    }

    private void startMessageReceiver() {
        new Thread(() -> {
            String line;
            try {
//                使用 BufferedReader 的 readLine() 方法一行一行地读取服务器发来的消息。
//                如果返回值为 null，表示服务器已关闭连接，循环结束。
                while ((line = reader.readLine()) != null) {
//                    判断当前行是否以 [USERLIST] 开头。
//                    这是一种自定义协议格式，表示这是一条用户列表更新的消息
                    if (line.startsWith("[USERLIST]")) {
//                        如果是用户列表消息，则调用 updateUserList() 方法进行处理。
//                        substring(10) 去掉开头的 [USERLIST] 字符串，只保留用户名部分。
                        updateUserList(line.substring(10));
                    } else {
                        messageArea.append(line + "\n");
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "连接断开");
            }
        }).start();
    }
}

