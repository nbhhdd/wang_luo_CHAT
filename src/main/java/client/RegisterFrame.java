package client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton registerButton;

    public RegisterFrame() {
        setTitle("用户注册");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭窗口但不退出程序
        setLocationRelativeTo(null); // 居中

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        registerButton = new JButton("注册");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 外边距
        gbc.fill = GridBagConstraints.HORIZONTAL;

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

        // 按钮区域
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);

        add(panel);

        // 注册按钮监听
        registerButton.addActionListener(e -> handleRegister());
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空");
            return;
        }

        try {
            Socket socket = new Socket("localhost", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("REGISTER:" + username + ":" + password);
            String response = in.readLine();
            socket.close();

            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "注册成功！请返回登录界面登录。");
                dispose(); // 关闭注册窗口
            } else {
                JOptionPane.showMessageDialog(this, "注册失败：" + response);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "连接服务器失败");
        }
    }
}
