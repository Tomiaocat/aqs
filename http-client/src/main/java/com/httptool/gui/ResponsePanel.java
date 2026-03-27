package com.httptool.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.httptool.model.HttpResponseData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 响应展示面板
 */
public class ResponsePanel extends JPanel {
    private JTabbedPane tabbedPane;
    private JTextArea cipherTextArea;
    private JTextArea plainTextArea;
    private JTree jsonTree;
    private JLabel statusLabel;
    private JTable dataTable;
    private DefaultTableModel dataTableModel;
    private JLabel dataStatusLabel;

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
        tabbedPane.addTab("密文(AES+Base64)", cipherScroll);

        // 明文标签页 - 带格式化按钮
        JPanel plainPanel = new JPanel(new BorderLayout());
        plainTextArea = new JTextArea();
        plainTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        plainTextArea.setEditable(false);
        plainTextArea.setLineWrap(true);
        plainTextArea.setWrapStyleWord(true);
        JScrollPane plainScroll = new JScrollPane(plainTextArea);
        plainPanel.add(plainScroll, BorderLayout.CENTER);

        // 格式化按钮 - 使用 FlatLaf 主按钮样式
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton formatPlainBtn = new JButton("格式化 JSON");
        formatPlainBtn.putClientProperty("JButton.buttonType", "primary");
        formatPlainBtn.addActionListener(e -> formatPlainText());
        formatPanel.add(formatPlainBtn);
        plainPanel.add(formatPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("明文(JSON)", plainPanel);

        // 数据表格标签页
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataStatusLabel = new JLabel("暂无数据");
        dataStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dataPanel.add(dataStatusLabel, BorderLayout.NORTH);

        dataTableModel = new DefaultTableModel();
        dataTable = new JTable(dataTableModel);
        dataTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane dataScroll = new JScrollPane(dataTable);
        dataPanel.add(dataScroll, BorderLayout.CENTER);

        tabbedPane.addTab("数据", dataPanel);

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
        int statusCode = response.getStatusCode();

        // 更新状态
        statusLabel.setText(String.format("状态码: %d | 耗时: %d ms | 大小: %d bytes",
            statusCode, response.getDuration(), response.getBody().length()));

        // 根据状态码设置边框颜色
        Color borderColor;
        if (statusCode == 200) {
            borderColor = new Color(34, 139, 34); // 绿色
        } else {
            borderColor = new Color(220, 20, 60); // 红色
        }

        // 创建带颜色的边框
        javax.swing.border.Border coloredBorder = BorderFactory.createLineBorder(borderColor, 3);
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
            coloredBorder,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // 显示密文
        cipherTextArea.setText(response.getBody());

        // 显示明文（自动尝试格式化）
        String decryptedBody = response.getDecryptedBody();
        if (decryptedBody != null) {
            String formatted = tryFormatJson(decryptedBody);
            plainTextArea.setText(formatted);
            // 更新JSON树
            updateJsonTree(decryptedBody);
            // 更新数据表格
            boolean hasData = updateDataTable(decryptedBody);

            // 智能切换标签页：有数据表 -> 数据标签，否则 -> 明文标签
            if (hasData) {
                tabbedPane.setSelectedIndex(2); // "数据" 标签页
            } else {
                tabbedPane.setSelectedIndex(1); // "明文(JSON)" 标签页
            }
        } else {
            plainTextArea.setText("解密失败");
            // 解密失败，显示密文标签页
            tabbedPane.setSelectedIndex(0); // "密文(AES+Base64)" 标签页
        }
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

    /**
     * 尝试格式化 JSON，如果失败则返回原文
     */
    private String tryFormatJson(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(text, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            // 无法格式化，返回原文
            return text;
        }
    }

    /**
     * 手动格式化明文区的 JSON
     */
    private void formatPlainText() {
        String text = plainTextArea.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(text, Object.class);
            String formatted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            plainTextArea.setText(formatted);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "JSON 格式错误: " + e.getMessage(),
                "格式化失败",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 更新数据表格，从JSON响应中提取data字段并以表格形式展示
     * @return true 如果成功解析并展示了数据，false 否则
     */
    private boolean updateDataTable(String json) {
        dataTableModel.setRowCount(0);
        dataTableModel.setColumnCount(0);

        if (json == null || json.trim().isEmpty()) {
            dataStatusLabel.setText("响应为空");
            return false;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            JsonNode dataNode = rootNode.get("data");

            if (dataNode == null) {
                dataStatusLabel.setText("响应中未找到data字段");
                return false;
            }

            if (!dataNode.isArray()) {
                dataStatusLabel.setText("data字段不是数组类型");
                return false;
            }

            if (dataNode.size() == 0) {
                dataStatusLabel.setText("data数组为空");
                return false;
            }

            // 收集所有列名（从所有Map中合并）
            Set<String> columns = new LinkedHashSet<>();
            for (JsonNode row : dataNode) {
                if (row.isObject()) {
                    Iterator<String> fieldNames = row.fieldNames();
                    while (fieldNames.hasNext()) {
                        columns.add(fieldNames.next());
                    }
                }
            }

            if (columns.isEmpty()) {
                dataStatusLabel.setText("data数组中没有对象数据");
                return false;
            }

            // 设置表头
            List<String> columnList = new ArrayList<>(columns);
            for (String col : columnList) {
                dataTableModel.addColumn(col);
            }

            // 填充数据
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            for (JsonNode row : dataNode) {
                Object[] rowData = new Object[columnList.size()];
                for (int i = 0; i < columnList.size(); i++) {
                    String col = columnList.get(i);
                    JsonNode value = row.get(col);
                    rowData[i] = formatValue(value, dateFormat);
                }
                dataTableModel.addRow(rowData);
            }

            dataStatusLabel.setText("共 " + dataNode.size() + " 条记录");

            // 自动调整列宽
            autoResizeColumns(dataTable);

            return true;

        } catch (Exception e) {
            dataStatusLabel.setText("解析失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 格式化单元格值，处理日期类型转换
     */
    private String formatValue(JsonNode value, SimpleDateFormat dateFormat) {
        if (value == null || value.isNull()) {
            return "";
        }
        if (value.isTextual()) {
            String text = value.asText();
            // 尝试解析日期字符串
            Date parsedDate = tryParseDate(text);
            if (parsedDate != null) {
                return dateFormat.format(parsedDate);
            }
            return text;
        }
        if (value.isNumber()) {
            long num = value.asLong();
            // 判断是否为时间戳（2000-2100年范围内）
            // 2000-01-01 = 946656000000L (毫秒)
            // 2100-01-01 = 4102444800000L (毫秒)
            if (num > 946656000000L && num < 4102444800000L) {
                return dateFormat.format(new Date(num));
            }
            // 判断是否为Double/Float
            if (value.isFloatingPointNumber()) {
                return String.valueOf(value.asDouble());
            }
            return value.asText();
        }
        if (value.isBoolean()) {
            return value.asText();
        }
        if (value.isObject() || value.isArray()) {
            return value.toString();
        }
        return value.asText();
    }

    /**
     * 尝试解析日期字符串
     */
    private Date tryParseDate(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        // 尝试多种日期格式
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false);
                return sdf.parse(text);
            } catch (ParseException e) {
                // 继续尝试下一个格式
            }
        }
        return null;
    }

    /**
     * 自动调整表格列宽以适应内容
     */
    private void autoResizeColumns(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 100; // 最小宽度
            for (int row = 0; row < table.getRowCount(); row++) {
                Object value = table.getValueAt(row, column);
                if (value != null) {
                    int cellWidth = value.toString().length() * 8 + 20; // 估算宽度
                    width = Math.max(width, cellWidth);
                }
            }
            // 限制最大宽度
            width = Math.min(width, 400);
            // 考虑表头宽度
            String header = table.getColumnName(column);
            int headerWidth = header.length() * 8 + 30;
            width = Math.max(width, headerWidth);

            table.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }
}
