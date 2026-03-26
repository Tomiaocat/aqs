package com.httptool.dialog;

import com.httptool.storage.KeyStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/**
 * 密钥管理对话框
 */
public class KeyManagerDialog extends JDialog {
    private KeyStorage keyStorage;
    private JTable keyTable;
    private DefaultTableModel tableModel;

    public KeyManagerDialog(JFrame parent, KeyStorage keyStorage) {
        super(parent, "密钥管理", true);
        this.keyStorage = keyStorage;

        setSize(600, 400);
        setLocationRelativeTo(parent);

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // 密钥表格
        String[] columns = {"名称", "密钥值"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        keyTable = new JTable(tableModel);
        keyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(keyTable);
        add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn = new JButton("添加密钥");
        JButton editBtn = new JButton("编辑选中");
        JButton deleteBtn = new JButton("删除选中");
        JButton closeBtn = new JButton("关闭");

        addBtn.addActionListener(e -> addKey());
        editBtn.addActionListener(e -> editKey());
        deleteBtn.addActionListener(e -> deleteKey());
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(closeBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        Map<String, String> keys = keyStorage.getAllKeys();
        for (Map.Entry<String, String> entry : keys.entrySet()) {
            String maskedKey = entry.getValue().length() > 6
                ? entry.getValue().substring(0, 3) + "***" + entry.getValue().substring(entry.getValue().length() - 3)
                : entry.getValue();
            tableModel.addRow(new Object[]{entry.getKey(), maskedKey});
        }
    }

    private void addKey() {
        JTextField nameField = new JTextField(20);
        JTextField keyField = new JTextField(30);

        Object[] message = {
            "密钥名称:", nameField,
            "密钥值:", keyField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "添加密钥",
            JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String key = keyField.getText().trim();

            if (name.isEmpty() || key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "名称和密钥不能为空");
                return;
            }

            keyStorage.saveKey(name, key);
            refreshTable();
        }
    }

    private void editKey() {
        int selectedRow = keyTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的密钥");
            return;
        }

        String oldName = (String) tableModel.getValueAt(selectedRow, 0);
        String oldKey = keyStorage.getKey(oldName);

        JTextField nameField = new JTextField(oldName, 20);
        JTextField keyField = new JTextField(oldKey, 30);

        Object[] message = {
            "密钥名称:", nameField,
            "密钥值:", keyField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "编辑密钥",
            JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newKey = keyField.getText().trim();

            if (newName.isEmpty() || newKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "名称和密钥不能为空");
                return;
            }

            // 如果名称改变，删除旧的
            if (!oldName.equals(newName)) {
                keyStorage.deleteKey(oldName);
            }

            keyStorage.saveKey(newName, newKey);
            refreshTable();
        }
    }

    private void deleteKey() {
        int selectedRow = keyTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的密钥");
            return;
        }

        String name = (String) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除密钥 '" + name + "' 吗?",
            "确认删除",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            keyStorage.deleteKey(name);
            refreshTable();
        }
    }
}
