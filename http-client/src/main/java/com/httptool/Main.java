package com.httptool;

import com.httptool.gui.MainFrame;
import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import java.awt.*;

/**
 * 客户端程序入口
 */
public class Main {
    public static void main(String[] args) {
        // 设置高 DPI 支持
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("sun.java2d.dpiaware", "true");

        // 设置 FlatLaf 主题（跨平台一致，IntelliJ风格）
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());

            // 设置全局字体抗锯齿
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
