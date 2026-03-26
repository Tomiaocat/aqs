package com.httptool.gui;

import com.httptool.model.HttpRequestData;
import com.httptool.model.HttpResponseData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 历史记录面板
 */
public class HistoryPanel extends JPanel {
    private MainFrame mainFrame;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public HistoryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // 创建表格模型
        String[] columns = {"时间", "方法", "URL", "状态码", "耗时(ms)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        // 双击加载历史记录
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadHistoryItem();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("刷新");
        JButton clearBtn = new JButton("清空");
        JButton loadBtn = new JButton("加载选中");

        refreshBtn.addActionListener(e -> refreshHistory());
        clearBtn.addActionListener(e -> clearHistory());
        loadBtn.addActionListener(e -> loadHistoryItem());

        buttonPanel.add(loadBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(clearBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshHistory();
    }

    public void refreshHistory() {
        tableModel.setRowCount(0);
        List<HttpRequestData> history = mainFrame.getHistoryStorage().getHistory();

        for (HttpRequestData request : history) {
            Object[] row = {
                dateFormat.format(request.getTimestamp()),
                request.getMethod(),
                request.getUrl(),
                request.getResponseStatus(),
                request.getResponseDuration()
            };
            tableModel.addRow(row);
        }
    }

    private void loadHistoryItem() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<HttpRequestData> history = mainFrame.getHistoryStorage().getHistory();
            if (selectedRow < history.size()) {
                HttpRequestData requestData = history.get(selectedRow);
                mainFrame.getRequestPanel().loadFromHistory(requestData);
            }
        }
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要清空所有历史记录吗?",
            "确认",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            mainFrame.getHistoryStorage().clearHistory();
            refreshHistory();
        }
    }
}
