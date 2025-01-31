package Dev;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;

public class FileChooserInComponent extends JPanel {
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    public FileChooserInComponent() {
        setLayout(new BorderLayout());
        initFileTree();
        add(new JScrollPane(fileTree), BorderLayout.CENTER);
    }

    private void initFileTree() {
        // 创建虚拟根节点（包含所有磁盘）
        FileTreeNode rootNode = new FileTreeNode(null);
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);
        fileTree.setRootVisible(false); // 隐藏虚拟根节点

        // 初始化磁盘节点
        File[] roots = File.listRoots();
        for (File root : roots) {
            FileTreeNode diskNode = new FileTreeNode(root);
            rootNode.add(diskNode);
            // 预加载一级子目录（立即显示展开箭头）
            loadChildren(diskNode);
        }
        treeModel.reload();

        // 自定义渲染器
        fileTree.setCellRenderer(new FileTreeCellRenderer());

        // 节点展开监听（修复事件处理）
        fileTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            @Override
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent event) {
                TreePath path = event.getPath();
                FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
                if (!node.isLoaded()) {
                    loadChildren(node);
                }
            }

            @Override
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent event) {}
        });
    }

    private void loadChildren(FileTreeNode parentNode) {
        File parentFile = parentNode.getFile();
        if (parentFile == null) return;

        // 标记为正在加载（避免重复请求）
        parentNode.setLoaded(true);

        SwingWorker<Void, File> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    File[] children = parentFile.listFiles(file ->
                            file.isDirectory() && !file.isHidden()
                    );
                    if (children != null) {
                        for (File child : children) {
                            publish(child);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("加载失败: " + parentFile + " | 错误: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(java.util.List<File> chunks) {
                for (File child : chunks) {
                    FileTreeNode childNode = new FileTreeNode(child);
                    parentNode.add(childNode);
                }
                treeModel.nodesWereInserted(parentNode, new int[parentNode.getChildCount()]);
                fileTree.expandPath(new TreePath(parentNode.getPath())); // 自动展开
            }

            @Override
            protected void done() {
                if (parentNode.getChildCount() == 0) {
                    // 如果没有子节点，显示占位符
                    parentNode.add(new FileTreeNode(new File("[空]")));
                    treeModel.reload(parentNode);
                }
            }
        };
        worker.execute();
    }

    // 文件树节点定义
    private static class FileTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
        private final File file;
        private boolean loaded = false;

        public FileTreeNode(File file) {
            super(file);
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public boolean isLoaded() {
            return loaded;
        }

        public void setLoaded(boolean loaded) {
            this.loaded = loaded;
        }
    }

    // 渲染器（显示系统图标）
    private class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            FileTreeNode node = (FileTreeNode) value;
            if (node.getFile() != null) {
                setIcon(fileSystemView.getSystemIcon(node.getFile()));
                setText(fileSystemView.getSystemDisplayName(node.getFile()));
            } else {
                setText("未知节点");
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("响应式文件树");
            frame.setSize(400, 600);
            frame.add(new FileChooserInComponent());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}