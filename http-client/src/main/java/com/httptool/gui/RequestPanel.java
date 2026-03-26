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
    private JTextField keyField;
    private JTextArea bodyArea;
    private JTextArea encryptedBodyArea;
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
        // 上部面板 - 请求头和密钥
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        // Headers面板
        JPanel headersContainer = new JPanel(new BorderLayout());
        headersContainer.setBorder(BorderFactory.createTitledBorder("请求头"));

        headersPanel = new JPanel();
        headersPanel.setLayout(new BoxLayout(headersPanel, BoxLayout.Y_AXIS));

        // 添加常用请求头
        addHeaderRow("Content-Type", "application/json");
        addHeaderRow("Accept", "application/json");
        addHeaderRow("X-invc-env-flag", "new");

        JScrollPane headersScroll = new JScrollPane(headersPanel);
        headersScroll.setPreferredSize(new Dimension(0, 120));
        headersContainer.add(headersScroll, BorderLayout.CENTER);

        JButton addHeaderBtn = new JButton("添加请求头");
        addHeaderBtn.setBackground(new Color(0, 120, 212)); // 蓝色背景
        addHeaderBtn.setForeground(Color.WHITE); // 白色字体
        addHeaderBtn.setFocusPainted(false);
        addHeaderBtn.setOpaque(true);
        addHeaderBtn.setContentAreaFilled(true);
        addHeaderBtn.setBorderPainted(false); // 移除边框
        addHeaderBtn.setFont(new Font(addHeaderBtn.getFont().getName(), Font.BOLD, 12));
        addHeaderBtn.setPreferredSize(new Dimension(120, 30));
        addHeaderBtn.addActionListener(e -> addHeaderRow("", ""));
        headersContainer.add(addHeaderBtn, BorderLayout.SOUTH);

        topPanel.add(headersContainer, BorderLayout.CENTER);

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
        keyField.setPreferredSize(new Dimension(0, 30));
        keyGbc.gridx = 1;
        keyGbc.weightx = 1.0;
        keyPanel.add(keyField, keyGbc);

        topPanel.add(keyPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // 中间面板 - 请求体和密文
        JPanel bodyContainer = new JPanel(new BorderLayout(0, 10));

        // 请求体面板
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(BorderFactory.createTitledBorder("请求体 (JSON格式)"));

        bodyArea = new JTextArea(8, 40);
        bodyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bodyArea.setText("{\"shopStatus\":\"\"}");
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyPanel.add(bodyScroll, BorderLayout.CENTER);

        // 模板按钮和格式化按钮
        JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveTemplateBtn = new JButton("保存模板");
        JButton loadTemplateBtn = new JButton("加载模板");
        JButton formatJsonBtn = new JButton("格式化 JSON");

        // 格式化按钮样式 - 蓝色背景，白色字体
        formatJsonBtn.setBackground(new Color(0, 120, 212));
        formatJsonBtn.setForeground(Color.WHITE);
        formatJsonBtn.setFocusPainted(false);
        formatJsonBtn.setOpaque(true);
        formatJsonBtn.setContentAreaFilled(true);
        formatJsonBtn.setBorderPainted(false);
        formatJsonBtn.setFont(new Font(formatJsonBtn.getFont().getName(), Font.BOLD, 12));

        saveTemplateBtn.addActionListener(e -> saveTemplate());
        loadTemplateBtn.addActionListener(e -> loadTemplate());
        formatJsonBtn.addActionListener(e -> formatJsonBody());

        templatePanel.add(saveTemplateBtn);
        templatePanel.add(loadTemplateBtn);
        templatePanel.add(formatJsonBtn);
        bodyPanel.add(templatePanel, BorderLayout.SOUTH);

        bodyContainer.add(bodyPanel, BorderLayout.CENTER);

        // 请求体密文面板
        JPanel cipherPanel = new JPanel(new BorderLayout());
        cipherPanel.setBorder(BorderFactory.createTitledBorder("请求体密文 (Base64)"));

        encryptedBodyArea = new JTextArea(4, 40);
        encryptedBodyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        encryptedBodyArea.setEditable(false);
        encryptedBodyArea.setLineWrap(true);
        encryptedBodyArea.setWrapStyleWord(true);
        JScrollPane cipherScroll = new JScrollPane(encryptedBodyArea);
        cipherPanel.add(cipherScroll, BorderLayout.CENTER);

        // 复制密文按钮
        JPanel cipherButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton copyCipherBtn = new JButton("复制密文");
        copyCipherBtn.setBackground(new Color(0, 120, 212));
        copyCipherBtn.setForeground(Color.WHITE);
        copyCipherBtn.setFocusPainted(false);
        copyCipherBtn.setOpaque(true);
        copyCipherBtn.setContentAreaFilled(true);
        copyCipherBtn.setBorderPainted(false);
        copyCipherBtn.setFont(new Font(copyCipherBtn.getFont().getName(), Font.BOLD, 12));
        copyCipherBtn.addActionListener(e -> copyToClipboard(encryptedBodyArea.getText()));
        cipherButtonPanel.add(copyCipherBtn);
        cipherPanel.add(cipherButtonPanel, BorderLayout.SOUTH);

        bodyContainer.add(cipherPanel, BorderLayout.SOUTH);

        add(bodyContainer, BorderLayout.CENTER);
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

    public HttpRequestData collectRequestData() {
        HttpRequestData requestData = new HttpRequestData();
        requestData.setHeaders(getHeaders());
        requestData.setBody(bodyArea.getText());
        requestData.setKey(keyField.getText());
        requestData.setTimestamp(System.currentTimeMillis());
        return requestData;
    }

    private void saveTemplate() {
        String name = JOptionPane.showInputDialog(this, "请输入模板名称:");
        if (name != null && !name.trim().isEmpty()) {
            RequestTemplate template = new RequestTemplate();
            template.setName(name);
            template.setMethod(mainFrame.getMethod());
            template.setUrl(mainFrame.getUrl());
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
                    mainFrame.setMethod(template.getMethod());
                    mainFrame.setUrl(template.getUrl());
                    bodyArea.setText(template.getBody());
                    keyField.setText(template.getKey());
                    break;
                }
            }
        }
    }

    public void loadFromHistory(HttpRequestData requestData) {
        mainFrame.setMethod(requestData.getMethod());
        mainFrame.setUrl(requestData.getUrl());
        bodyArea.setText(requestData.getBody());
        keyField.setText(requestData.getKey());
    }

    private void formatJsonBody() {
        String text = bodyArea.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        try {
            // 使用 Jackson 解析和格式化 JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object json = mapper.readValue(text, Object.class);
            String formatted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            bodyArea.setText(formatted);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "JSON 格式错误: " + e.getMessage(),
                "格式化失败",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 设置加密后的请求体显示
     */
    public void setEncryptedBody(String encryptedBody) {
        if (encryptedBodyArea != null) {
            encryptedBodyArea.setText(encryptedBody != null ? encryptedBody : "");
        }
    }

    /**
     * 清空密文显示
     */
    public void clearEncryptedBody() {
        if (encryptedBodyArea != null) {
            encryptedBodyArea.setText("");
        }
    }

    private void copyToClipboard(String text) {
        if (text != null && !text.isEmpty()) {
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        }
    }
}
