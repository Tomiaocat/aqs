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

        // 添加默认的Content-Type头
        addHeaderRow("Content-Type", "application/json");

        JScrollPane headersScroll = new JScrollPane(headersPanel);
        headersScroll.setPreferredSize(new Dimension(0, 120));
        headersContainer.add(headersScroll, BorderLayout.CENTER);

        JButton addHeaderBtn = new JButton("添加请求头");
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

        // 中间面板 - 请求体
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(BorderFactory.createTitledBorder("请求体 (JSON格式)"));

        bodyArea = new JTextArea(12, 40);
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

        add(bodyPanel, BorderLayout.CENTER);
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
}
