package com.httptool.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.httptool.model.HttpResponseData;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * 响应展示面板
 */
public class ResponsePanel extends JPanel {
    private JTabbedPane tabbedPane;
    private JTextArea cipherTextArea;
    private JTextArea plainTextArea;
    private JTree jsonTree;
    private JLabel statusLabel;

    public ResponsePanel() {
        setLayout(new BorderLayout(10, 10));

        // 状态栏
        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusLabel, BorderLayout.NORTH);

        // 标签页
        tabbedPane = new JTabbedPane();

        // 密文标签页
        cipherTextArea = new JTextArea();
        cipherTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        cipherTextArea.setEditable(false);
        cipherTextArea.setLineWrap(true);
        cipherTextArea.setWrapStyleWord(true);
        JScrollPane cipherScroll = new JScrollPane(cipherTextArea);
        tabbedPane.addTab("密文(Base64)", cipherScroll);

        // 明文标签页
        plainTextArea = new JTextArea();
        plainTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        plainTextArea.setEditable(false);
        plainTextArea.setLineWrap(true);
        plainTextArea.setWrapStyleWord(true);
        JScrollPane plainScroll = new JScrollPane(plainTextArea);
        tabbedPane.addTab("明文(JSON)", plainScroll);

        // JSON树标签页
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Response");
        jsonTree = new JTree(root);
        JScrollPane treeScroll = new JScrollPane(jsonTree);
        tabbedPane.addTab("JSON树", treeScroll);

        add(tabbedPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton copyCipherBtn = new JButton("复制密文");
        JButton copyPlainBtn = new JButton("复制明文");
        copyCipherBtn.addActionListener(e -> copyToClipboard(cipherTextArea.getText()));
        copyPlainBtn.addActionListener(e -> copyToClipboard(plainTextArea.getText()));
        buttonPanel.add(copyCipherBtn);
        buttonPanel.add(copyPlainBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void displayResponse(HttpResponseData response) {
        // 更新状态
        statusLabel.setText(String.format("状态码: %d | 耗时: %d ms | 大小: %d bytes",
            response.getStatusCode(), response.getDuration(), response.getBody().length()));

        // 显示密文
        cipherTextArea.setText(response.getBody());

        // 显示明文
        String decryptedBody = response.getDecryptedBody();
        if (decryptedBody != null) {
            plainTextArea.setText(decryptedBody);
            // 更新JSON树
            updateJsonTree(decryptedBody);
        } else {
            plainTextArea.setText("解密失败");
        }

        // 自动切换到明文标签页
        tabbedPane.setSelectedIndex(1);
    }

    private void updateJsonTree(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            DefaultMutableTreeNode treeRoot = createTreeNode("Response", rootNode);

            jsonTree.setModel(new javax.swing.tree.DefaultTreeModel(treeRoot));
            expandAll(jsonTree, new javax.swing.tree.TreePath(treeRoot), true);
        } catch (Exception e) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("解析失败: " + e.getMessage());
            jsonTree.setModel(new javax.swing.tree.DefaultTreeModel(root));
        }
    }

    private DefaultMutableTreeNode createTreeNode(String name, JsonNode node) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(name);

        if (node.isObject()) {
            java.util.Iterator<java.util.Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                java.util.Map.Entry<String, JsonNode> entry = fields.next();
                treeNode.add(createTreeNode(entry.getKey() + ": " + getNodeValue(entry.getValue()), entry.getValue()));
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                treeNode.add(createTreeNode("[" + i + "]: " + getNodeValue(node.get(i)), node.get(i)));
            }
        }

        return treeNode;
    }

    private String getNodeValue(JsonNode node) {
        if (node.isTextual()) {
            return "\"" + node.asText() + "\"";
        } else if (node.isNumber()) {
            return node.asText();
        } else if (node.isBoolean()) {
            return node.asText();
        } else if (node.isNull()) {
            return "null";
        } else if (node.isObject()) {
            return "{Object}";
        } else if (node.isArray()) {
            return "[Array(" + node.size() + ")]";
        }
        return node.asText();
    }

    private void expandAll(JTree tree, javax.swing.tree.TreePath parent, boolean expand) {
        javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                javax.swing.tree.DefaultMutableTreeNode child = (javax.swing.tree.DefaultMutableTreeNode) node.getChildAt(i);
                javax.swing.tree.TreePath path = parent.pathByAddingChild(child);
                expandAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    private void copyToClipboard(String text) {
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    public void clear() {
        cipherTextArea.setText("");
        plainTextArea.setText("");
        statusLabel.setText("就绪");
    }
}
