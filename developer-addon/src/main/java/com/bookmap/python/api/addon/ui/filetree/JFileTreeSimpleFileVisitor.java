package com.bookmap.python.api.addon.ui.filetree;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

public class JFileTreeSimpleFileVisitor extends SimpleFileVisitor<Path> {

    private final File root;
    private final WatchService watchService;
    private final DefaultTreeModel treeModel;
    private final Map<Path, JFileTree.DefaultMutableFileTreeNode> sortedNodes;
    private final Map<Path, JFileTree.DefaultMutableFileTreeNode> directoryToRespectiveNode;

    public JFileTreeSimpleFileVisitor(
        File root,
        WatchService watchService,
        DefaultTreeModel treeModel,
        Map<Path, JFileTree.DefaultMutableFileTreeNode> sortedNodes,
        Map<Path, JFileTree.DefaultMutableFileTreeNode> directoryToRespectiveNode
    ) {
        this.root = root;
        this.watchService = watchService;
        this.treeModel = treeModel;
        this.sortedNodes = sortedNodes;
        this.directoryToRespectiveNode = directoryToRespectiveNode;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        // register watcher for the current directory
        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        Path parentDir = file.getParent();
        if (directoryToRespectiveNode.containsKey(parentDir)) {
            var parentNode = (JFileTree.DefaultMutableFileTreeNode) directoryToRespectiveNode.get(parentDir);
            var fileNode = new JFileTree.DefaultMutableFileTreeNode(file.toFile());
            sortedNodes.put(file, fileNode);
            SwingUtilities.invokeLater(() -> treeModel.insertNodeInto(fileNode, parentNode, parentNode.getChildCount())
            );
        }

        return FileVisitResult.CONTINUE;
    }
}
