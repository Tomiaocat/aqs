package com.httptool.gui;

import com.httptool.model.HttpRequestData;
import com.httptool.model.RequestTemplate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求配置面板
 */
public class RequestPanel extends JPanel {
    private MainFrame mainFrame;
    private JComboBox<String> methodCombo;
    private JTextField urlField;
    private JTextField keyField;
    private JTextArea bodyArea;
    private JPanel headersPanel;
    private java.util.List<HeaderInputRow> headerRows = new java.util.ArrayList<>();

    public RequestPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "请求配置",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));

        initComponents();
    }

    private void initComponents() {
        // 顶部面板 - URL和方法
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 方法选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        topPanel.add(new JLabel("方法:"), gbc);

        String[] methods = {"GET", "POST", "PUT", "PATCH", "DELETE"};
        methodCombo = new JComboBox<>(methods);
        methodCombo.setSelectedItem("POST");
        gbc.gridx = 1;
        gbc.weightx = 0.2;
        topPanel.add(methodCombo, gbc);

        // URL输入
        gbc.gridx = 2;
        gbc.weightx = 0;
        topPanel.add(new JLabel("URL:"), gbc);

        urlField = new JTextField(30);
        urlField.setText("http://localhost:8080/api/user");
        gbc.gridx = 3;
        gbc.weightx = 0.8;
        topPanel.add(urlField, gbc);

        // 发送按钮
        JButton sendButton = new JButton("发送请求");
        sendButton.setBackground(new Color(0, 120, 212));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendRequest());
        gbc.gridx = 4;
        gbc.weightx = 0;
        topPanel.add(sendButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // 中间面板 - 请求头和密钥
        JPanel middlePanel = new JPanel(new BorderLayout(10, 10));

        // Headers面板
        JPanel headersContainer = new JPanel(new BorderLayout());
        headersContainer.setBorder(BorderFactory.createTitledBorder("请求头"));

        headersPanel = new JPanel();
        headersPanel.setLayout(new BoxLayout(headersPanel, BoxLayout.Y_AXIS));

        // 添加默认的Content-Type头
        addHeaderRow("Content-Type", "application/json");

        JScrollPane headersScroll = new JScrollPane(headersPanel);
        headersScroll.setPreferredSize(new Dimension(0, 100));
        headersContainer.add(headersScroll, BorderLayout.CENTER);

        JButton addHeaderBtn = new JButton("添加请求头");
        addHeaderBtn.addActionListener(e -> addHeaderRow("", ""));
        headersContainer.add(addHeaderBtn, BorderLayout.SOUTH);

        middlePanel.add(headersContainer, BorderLayout.CENTER);

        // 密钥面板
        JPanel keyPanel = new JPanel(new GridBagLayout());
        keyPanel.setBorder(BorderFactory.createTitledBorder("AES密钥"));
        GridBagConstraints keyGbc = new GridBagConstraints();
        keyGbc.insets = new Insets(5, 5, 5, 5);
        keyGbc.fill = GridBagConstraints.HORIZONTAL;

        keyGbc.gridx = 0;
        keyGbc.gridy = 0;
        keyGbc.weightx = 0;
        keyPanel.add(new JLabel("密钥:"), keyGbc);

        keyField = new JTextField(20);
        keyField.setText("your-secret-key-here");
        keyGbc.gridx = 1;
        keyGbc.weightx = 1.0;
        keyPanel.add(keyField, keyGbc);

        middlePanel.add(keyPanel, BorderLayout.SOUTH);

        add(middlePanel, BorderLayout.CENTER);

        // 底部面板 - 请求体
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(BorderFactory.createTitledBorder("请求体 (JSON格式)"));

        bodyArea = new JTextArea(10, 40);
        bodyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bodyArea.setText("{\n  \"id\": 1,\n  \"name\": \"张三\",\n  \"age\": 25\n}");
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyPanel.add(bodyScroll, BorderLayout.CENTER);

        // 模板按钮
        JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveTemplateBtn = new JButton("保存模板");
        JButton loadTemplateBtn = new JButton("加载模板");
        saveTemplateBtn.addActionListener(e -> saveTemplate());
        loadTemplateBtn.addActionListener(e -> loadTemplate());
        templatePanel.add(saveTemplateBtn);
        templatePanel.add(loadTemplateBtn);
        bodyPanel.add(templatePanel, BorderLayout.SOUTH);

        add(bodyPanel, BorderLayout.SOUTH);
    }

    private void addHeaderRow(String name, String value) {
        HeaderInputRow row = new HeaderInputRow(headersPanel, headerRows, name, value);
        headerRows.add(row);
        headersPanel.add(row);
        headersPanel.revalidate();
        headersPanel.repaint();
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        for (HeaderInputRow row : headerRows) {
            String name = row.getHeaderName();
            String value = row.getHeaderValue();
            if (!name.isEmpty()) {
                headers.put(name, value);
            }
        }
        return headers;
    }

    private void sendRequest() {
        HttpRequestData requestData = new HttpRequestData();
        requestData.setMethod((String) methodCombo.getSelectedItem());
        requestData.setUrl(urlField.getText().trim());
        requestData.setHeaders(getHeaders());
        requestData.setBody(bodyArea.getText());
        requestData.setKey(keyField.getText());
        requestData.setTimestamp(System.currentTimeMillis());

        mainFrame.sendRequest(requestData);
    }

    private void saveTemplate() {
        String name = JOptionPane.showInputDialog(this, "请输入模板名称:");
        if (name != null && !name.trim().isEmpty()) {
            RequestTemplate template = new RequestTemplate();
            template.setName(name);
            template.setMethod((String) methodCombo.getSelectedItem());
            template.setUrl(urlField.getText());
            template.setBody(bodyArea.getText());
            template.setKey(keyField.getText());
            template.setHeaders(getHeaders());

            mainFrame.getTemplateStorage().saveTemplate(template);
            JOptionPane.showMessageDialog(this, "模板已保存");
        }
    }

    private void loadTemplate() {
        java.util.List<RequestTemplate> templates = mainFrame.getTemplateStorage().getTemplates();
        if (templates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有保存的模板");
            return;
        }

        String[] names = templates.stream().map(RequestTemplate::getName).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this,
            "选择模板:",
            "加载模板",
            JOptionPane.QUESTION_MESSAGE,
            null,
            names,
            names[0]);

        if (selected != null) {
            for (RequestTemplate template : templates) {
                if (template.getName().equals(selected)) {
                    methodCombo.setSelectedItem(template.getMethod());
                    urlField.setText(template.getUrl());
                    bodyArea.setText(template.getBody());
                    keyField.setText(template.getKey());
                    break;
                }
            }
        }
    }

    public void loadFromHistory(HttpRequestData requestData) {
        methodCombo.setSelectedItem(requestData.getMethod());
        urlField.setText(requestData.getUrl());
        bodyArea.setText(requestData.getBody());
        keyField.setText(requestData.getKey());
    }
}
