package com.httptool.gui;

import javax.swing.*;
import java.awt.*;

/**
 * 请求头输入行组件
 */
public class HeaderInputRow extends JPanel {
    private JTextField nameField;
    private JTextField valueField;
    private JButton deleteButton;

    public HeaderInputRow(JPanel parent, java.util.List<HeaderInputRow> rows, String name, String value) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        nameField = new JTextField(name, 15);
        valueField = new JTextField(value, 25);
        deleteButton = new JButton("X");
        deleteButton.setPreferredSize(new Dimension(40, 25));
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        deleteButton.putClientProperty("JButton.buttonType", "danger"); // 危险操作样式

        deleteButton.addActionListener(e -> {
            parent.remove(this);
            rows.remove(this);
            parent.revalidate();
            parent.repaint();
        });

        add(new JLabel(" 名称: "));
        add(nameField);
        add(new JLabel(" 值: "));
        add(valueField);
        add(Box.createHorizontalStrut(5));
        add(deleteButton);
    }

    public String getHeaderName() {
        return nameField.getText().trim();
    }

    public String getHeaderValue() {
        return valueField.getText().trim();
    }
}
