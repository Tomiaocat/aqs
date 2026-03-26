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

    public MainFrame() {
        setTitle("HTTP加密通信工具");
        setSize(1200, 800);
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

        // 创建左侧面板（请求配置）
        requestPanel = new RequestPanel(this);
        requestPanel.setPreferredSize(new Dimension(500, 0));
        add(requestPanel, BorderLayout.WEST);

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

        add(rightPanel, BorderLayout.CENTER);
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
}
