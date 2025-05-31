package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.PrintWriter;
import java.net.Socket;

public class PrivateChatFrame extends JFrame {
    private final String sender;
    private final String receiver;
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private PrintWriter writer;

    public PrivateChatFrame(String sender, String receiver, Socket socket) {
        this.sender = sender;
        this.receiver = receiver;

        setTitle("私聊 - " + receiver);
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 聊天区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // 输入区域
        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 初始化输出流
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "连接输出流失败: " + e.getMessage());
        }

        // 事件绑定
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        setVisible(true);
        inputField.requestFocus(); // 初始聚焦输入框
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            String formatted = "[PRIVATE]" + sender + ":" + receiver + ":" + msg;
            if (writer != null) {
                writer.println(formatted);
                chatArea.append("我: " + msg + "\n");
                chatArea.setCaretPosition(chatArea.getDocument().getLength()); // 滚动到底部
            } else {
                chatArea.append("❌ 发送失败：输出流未就绪\n");
            }
            inputField.setText("");
        }
    }

}
