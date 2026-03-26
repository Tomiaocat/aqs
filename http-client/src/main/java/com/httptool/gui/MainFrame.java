package com.httptool.gui;

import com.httptool.model.HttpRequestData;
import com.httptool.model.HttpResponseData;
import com.httptool.http.HttpClient;
import com.httptool.crypto.AesUtil;
import com.httptool.storage.HistoryStorage;
import com.httptool.storage.KeyStorage;
import com.httptool.storage.TemplateStorage;
import com.httptool.model.RequestTemplate;
import com.httptool.dialog.KeyManagerDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

/**
 * 主窗口
 */
public class MainFrame extends JFrame {
    private RequestPanel requestPanel;
    private ResponsePanel responsePanel;
    private HistoryPanel historyPanel;
    private KeyStorage keyStorage;
    private HistoryStorage historyStorage;
    private TemplateStorage templateStorage;

    // 顶部组件（延伸到右侧）
    private JComboBox<String> methodCombo;
    private JTextField urlField;

    public MainFrame() {
        setTitle("HTTP加密通信工具");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初始化存储
        keyStorage = new KeyStorage();
        historyStorage = new HistoryStorage();
        templateStorage = new TemplateStorage();

        initComponents();
        initMenuBar();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // 顶部面板 - URL和方法（延伸到整个宽度）
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 中间主面板
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // 创建左侧面板（请求配置）
        requestPanel = new RequestPanel(this);
        requestPanel.setPreferredSize(new Dimension(500, 0));
        centerPanel.add(requestPanel, BorderLayout.WEST);

        // 创建右侧面板（响应和历史）
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        // 响应面板
        responsePanel = new ResponsePanel();
        responsePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "响应数据",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));
        rightPanel.add(responsePanel, BorderLayout.CENTER);

        // 历史面板
        historyPanel = new HistoryPanel(this);
        historyPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "请求历史",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));
        historyPanel.setPreferredSize(new Dimension(0, 200));
        rightPanel.add(historyPanel, BorderLayout.SOUTH);

        centerPanel.add(rightPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "请求地址",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;

        // 方法选择 - 高度增加1.7倍（约35像素）
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        topPanel.add(new JLabel("方法:"), gbc);

        String[] methods = {"GET", "POST", "PUT", "PATCH", "DELETE"};
        methodCombo = new JComboBox<>(methods);
        methodCombo.setSelectedItem("POST");
        methodCombo.setPreferredSize(new Dimension(100, 35));
        methodCombo.setFont(new Font(methodCombo.getFont().getName(), Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.weightx = 0.1;
        topPanel.add(methodCombo, gbc);

        // URL输入
        gbc.gridx = 2;
        gbc.weightx = 0;
        topPanel.add(new JLabel("URL:"), gbc);

        urlField = new JTextField();
        urlField.setText("http://127.0.0.1:8080/invc-gw/invc-transfer-service/inner/data/query");
        urlField.setHorizontalAlignment(JTextField.CENTER); // 文字居中
        urlField.setPreferredSize(new Dimension(0, 35));
        urlField.setFont(new Font(urlField.getFont().getName(), Font.PLAIN, 14));
        gbc.gridx = 3;
        gbc.weightx = 0.67;  // 缩短三分之一
        topPanel.add(urlField, gbc);

        // 发送按钮 - 蓝色背景，白色字体，带点击效果
        JButton sendButton = new JButton("发送请求");
        Color normalColor = new Color(0, 120, 212);
        Color pressedColor = new Color(0, 90, 160);
        sendButton.setBackground(normalColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setOpaque(true);
        sendButton.setContentAreaFilled(true);
        sendButton.setBorderPainted(false);
        sendButton.setPreferredSize(new Dimension(120, 35));
        sendButton.setFont(new Font(sendButton.getFont().getName(), Font.BOLD, 14));

        // 添加点击效果
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                sendButton.setBackground(pressedColor);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                sendButton.setBackground(normalColor);
            }
        });

        sendButton.addActionListener(e -> sendRequestFromTopPanel());
        gbc.gridx = 4;
        gbc.weightx = 0;
        topPanel.add(sendButton, gbc);

        return topPanel;
    }

    private void sendRequestFromTopPanel() {
        // 从RequestPanel获取其他数据
        HttpRequestData requestData = requestPanel.collectRequestData();
        requestData.setMethod((String) methodCombo.getSelectedItem());
        requestData.setUrl(urlField.getText().trim());
        sendRequest(requestData);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // 工具菜单
        JMenu toolsMenu = new JMenu("工具");
        JMenuItem keyManagerItem = new JMenuItem("密钥管理");
        keyManagerItem.addActionListener(e -> showKeyManager());
        toolsMenu.add(keyManagerItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void showKeyManager() {
        KeyManagerDialog dialog = new KeyManagerDialog(this, keyStorage);
        dialog.setVisible(true);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "HTTP加密通信工具 v1.0\n\n" +
            "技术规格:\n" +
            "- AES-128/CBC/PKCS5Padding\n" +
            "- 密钥派生: MD5取前16字节\n" +
            "- IV: 随机16字节，拼接在密文前\n" +
            "- 编码: Base64\n\n" +
            "Copyright 2024",
            "关于",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public void sendRequest(HttpRequestData requestData) {
        new SwingWorker<HttpResponseData, Void>() {
            @Override
            protected HttpResponseData doInBackground() throws Exception {
                HttpClient client = new HttpClient();

                // 加密请求体
                String requestBody = requestData.getBody();
                String encryptedBody = null;
                if (requestBody != null && !requestBody.trim().isEmpty()) {
                    encryptedBody = AesUtil.encryptJsonValues(requestBody, requestData.getKey());
                }

                // 发送请求
                HttpResponseData response = client.sendRequest(
                    requestData.getMethod(),
                    requestData.getUrl(),
                    requestData.getHeaders(),
                    encryptedBody
                );

                // 解密响应体
                String responseBody = response.getBody();
                if (responseBody != null && !responseBody.trim().isEmpty()) {
                    try {
                        String decryptedBody = AesUtil.decryptJsonValues(responseBody, requestData.getKey());
                        response.setDecryptedBody(decryptedBody);
                    } catch (Exception e) {
                        response.setDecryptedBody("解密失败: " + e.getMessage());
                    }
                }

                return response;
            }

            @Override
            protected void done() {
                try {
                    HttpResponseData response = get();
                    responsePanel.displayResponse(response);
                    historyStorage.addHistory(requestData, response);
                    historyPanel.refreshHistory();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "请求失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public KeyStorage getKeyStorage() {
        return keyStorage;
    }

    public TemplateStorage getTemplateStorage() {
        return templateStorage;
    }

    public HistoryStorage getHistoryStorage() {
        return historyStorage;
    }

    public RequestPanel getRequestPanel() {
        return requestPanel;
    }

    public void setMethod(String method) {
        methodCombo.setSelectedItem(method);
    }

    public void setUrl(String url) {
        urlField.setText(url);
    }

    public String getMethod() {
        return (String) methodCombo.getSelectedItem();
    }

    public String getUrl() {
        return urlField.getText();
    }
}
