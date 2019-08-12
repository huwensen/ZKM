package com.test.zoo;

import com.intellij.idea.IdeaLogger;
import com.intellij.idea.RareLogger;
import com.intellij.notification.EventLog;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DefaultProjectFactory;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.jgoodies.common.base.Strings;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import sun.util.logging.PlatformLogger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ZookeeperCli implements MouseListener {
    private JTextField txtHost;
    private JButton getButton;
    private JPanel rootContent;
    private JTree tree;
    private String ProFile = "zk.properties";


    public ZookeeperCli(ToolWindow toolWindow) {
        getButton.addMouseListener(this);
        tree.addMouseListener(this);
        init();
    }


    private void init() {
        tree.setModel(new DefaultTreeModel(null));
        tree.setBorder(new ToolWindow.Border());
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(ProFile)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String host = properties.getProperty("host", "localhost:2181");
            txtHost.setText(host);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveHost(String text) {
        try (FileOutputStream oFile = new FileOutputStream(ProFile)) {
            Properties properties = new Properties();
            properties.setProperty("host", text);
            properties.store(oFile, "host ip");
               /* File f=new File(ProFile);
                System.out.printf("文件位置：%s", f.getAbsolutePath());
                System.out.println(f.getPath());
                System.out.println(f.getCanonicalPath());*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void get() {
        String text = txtHost.getText();
        if (Strings.isEmpty(text)) return;
        try {
            ZooKeeper zooKeeper = new ZooKeeper(text, 2000000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        saveHost(text);
                    }
                }
            });
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("/");
            List<String> children = zooKeeper.getChildren("/", true);
            for (String child : children) {
                DefaultMutableTreeNode parent = new DefaultMutableTreeNode(child);
                getNode(zooKeeper, "/" + child, parent);
                root.add(parent);
            }
            tree.setModel(new DefaultTreeModel(root));
            tree.setRootVisible(false);
            zooKeeper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getNode(ZooKeeper zooKeeper, String str, DefaultMutableTreeNode root) throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(str, true);
        for (String child : children) {
            DefaultMutableTreeNode parent = new DefaultMutableTreeNode(child);
            getNode(zooKeeper, str + "/" + child, parent);
            root.add(parent);
        }
    }


    public JPanel getRootContent() {
        return rootContent;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource().equals(getButton)) {
            get();
        } else if (e.getSource().equals(tree) && e.getButton() == 3) {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable tText = new StringSelection(tree.getSelectionPath().toString());
            clip.setContents(tText, null);
            String msg = "已复制到剪贴板：" + tree.getSelectionPath().toString();

            Notifications.Bus.notify(new Notification("ZKM", "zookeeper插件复制",
                    msg, NotificationType.INFORMATION), ProjectManager.getInstance().getDefaultProject());

        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
