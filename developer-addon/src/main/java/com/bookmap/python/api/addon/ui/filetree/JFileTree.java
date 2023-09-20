package com.bookmap.python.api.addon.ui.filetree;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.bookmap.python.api.addon.ui.listeners.FileTrackerListener;
import com.bookmap.python.api.addon.ui.listeners.FileTreeSelectionListener;
import com.bookmap.python.api.addon.utils.FileUtils;
import com.bookmap.python.api.addon.utils.Log;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Represents component depicting file tree structure
 */
// TODO: handle proper file locking
//  separate file tree handling logic from displaying
public class JFileTree extends JPanel implements Closeable {

    private final DefaultTreeModel treeModel;
    private final JTree jTree;
    private final JScrollPane jScrollPane;
    private final WatchService watchService;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final ExecutorService watchServiceExecutor = Executors.newSingleThreadExecutor();
    private final Map<Path, DefaultMutableFileTreeNode> directoryToRespectiveNode = new ConcurrentHashMap<>();
    private final JFileTreeSimpleFileVisitor visitor;
    private final State state = new State();
    private final List<FileTrackerListener> trackerListeners = new ArrayList<>();
    // store tree of nodes keeping absolute names of their files as keys for fast search
    // TODO: maybe place it somewhere to combine insert to this tree and insertion to TreeModel
    private final Map<Path, DefaultMutableFileTreeNode> sortedNodes = new TreeMap<>(
        Comparator.comparing(Path::toAbsolutePath)
    );
    private final ActionListener newFileAction;
    private final Consumer<Path> onFileDelete;

    /**
     * @param rootDirectory specified rootDirectory directory, if it is not directory throws exception
     */
    public JFileTree(File rootDirectory, ActionListener newFileAction, Consumer<Path> onFileDelete) throws IOException {
        state.rootFile = rootDirectory;
        this.newFileAction = newFileAction;
        this.onFileDelete = onFileDelete;

        if (rootDirectory == null || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Root argument does not point to directory");
        }
        this.setLayout(new BorderLayout());
        var rootNode = new DefaultMutableFileTreeNode(rootDirectory);
        sortedNodes.put(state.rootFile.toPath(), rootNode);
        jTree = new JTree(rootNode);
        //jTree.addTreeExpansionListener(new RememberingTreeExpansionListener(state));
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        handleRightMouseClick(rootDirectory, e);
                        return;
                    }

                    if (SwingUtilities.isLeftMouseButton(e)) {
                        handleLeftMouseClick(e);
                    }
                }
            }
        );
        treeModel = (DefaultTreeModel) jTree.getModel();
        jScrollPane = new JScrollPane(jTree);
        jScrollPane.setMinimumSize(jTree.getMinimumSize());
        add(jScrollPane);
        watchService = FileSystems.getDefault().newWatchService();
        this.visitor =
            new JFileTreeSimpleFileVisitor(
                state.rootFile,
                watchService,
                treeModel,
                sortedNodes,
                directoryToRespectiveNode
            );
        initFileTreeAndTreeWatcher();
        expandRootDirectory(rootNode);
    }

    private void handleLeftMouseClick(MouseEvent e) {
        TreePath selPath = jTree.getPathForLocation(e.getX(), e.getY());
        if (selPath == null) {
            jTree.clearSelection();
        }
    }

    private void handleRightMouseClick(File rootDirectory, MouseEvent e) {
        TreePath selPath = jTree.getPathForLocation(e.getX(), e.getY());
        var addFile = new JMenuItem("New Python file...");
        var deleteFile = new JMenuItem("Delete file...");
        var openContainingFolder = new JMenuItem("Open containing folder");
        Path absolutePathToSelectedFile;
        // TODO: cache menu and its items
        var menu = new JPopupMenu();
        menu.add(addFile);

        // if user clicked somewhere outside specific node, but inside the tree,
        // just show a dialog and handle it as if he clicks on root node
        if (selPath == null) {
            absolutePathToSelectedFile = rootDirectory.getAbsoluteFile().toPath();
        } else {
            var clickedNode = (DefaultMutableFileTreeNode) selPath.getLastPathComponent();
            absolutePathToSelectedFile = clickedNode.getFile().toPath().toAbsolutePath();
            // allow remove only if non root directory is selected
            if (!rootDirectory.getAbsoluteFile().toPath().equals(absolutePathToSelectedFile)) {
                deleteFile.addActionListener(actionEvent -> {
                    Log.info(String.format("Clicked on 'Delete file' for %s", absolutePathToSelectedFile));
                    try {
                        int result = JOptionPane.showConfirmDialog(
                            jTree,
                            String.format(
                                "Are you sure you want delete '%s'?",
                                absolutePathToSelectedFile.getFileName()
                            ),
                            "Delete file",
                            JOptionPane.YES_NO_OPTION
                        );
                        if (result != JOptionPane.YES_OPTION) {
                            return;
                        }
                        FileUtils.deleteFileTree(absolutePathToSelectedFile.toFile());
                    } catch (IOException ex) {
                        Log.error("Failed to delete file", ex);
                        JOptionPane.showMessageDialog(
                            null,
                            ex.getMessage(),
                            "Failed to delete file",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
                menu.add(deleteFile);
            }
        }

        menu.add(openContainingFolder);

        addFile.addActionListener(newFileAction);

        openContainingFolder.addActionListener(actionEvent -> {
            Log.info("Clicked on 'Open containing folder'");
            try {
                File dir;
                if (absolutePathToSelectedFile.toFile().isDirectory()) {
                    dir = absolutePathToSelectedFile.toFile();
                } else {
                    dir = absolutePathToSelectedFile.toFile().getParentFile();
                }
                Desktop.getDesktop().open(dir);
            } catch (IOException ex) {
                Log.error("Failed to open containing folder", ex);
            }
        });

        menu.show(jTree, e.getX(), e.getY());
        jTree.setSelectionPath(selPath);
    }

    public void addFileTreeSelectionListener(FileTreeSelectionListener fileTreeSelectionListener) {
        jTree.addTreeSelectionListener(e -> {
            var defaultMutableFileTreeNode = (DefaultMutableFileTreeNode) jTree.getLastSelectedPathComponent();
            if (defaultMutableFileTreeNode == null) {
                fileTreeSelectionListener.fileSelected(null);
                return;
            }
            fileTreeSelectionListener.fileSelected(defaultMutableFileTreeNode.file);
        });
    }

    public void addFileTrackerListener(FileTrackerListener fileTrackerListener) {
        trackerListeners.add(fileTrackerListener);
    }

    //TODO: should file tree be locked for changes when we init it? Is not this done by default?
    private void initFileTreeAndTreeWatcher() throws IOException {
        directoryToRespectiveNode.put(state.rootFile.toPath(), (DefaultMutableFileTreeNode) treeModel.getRoot());

        // register all directories and subdirectories and init Jtree
        Files.walkFileTree(state.rootFile.toPath(), this.visitor);
        watchServiceExecutor.execute(this::handleFileSystemEvents);
        /*SwingUtilities.invokeLater(() -> {
            treeModel.reload();
            state.deepestExpandedNodes.forEach(jTree::scrollPathToVisible);
        });*/
    }

    // monitors and handles events related to any changes in file system
    private void handleFileSystemEvents() {
        try {
            while (!isClosed.get()) {
                WatchKey watchKey = watchService.poll(5, TimeUnit.MINUTES);
                if (watchKey == null) {
                    continue;
                }
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path absolutePathOfRelatedFile =
                        ((Path) watchKey.watchable()).resolve((Path) event.context()).toAbsolutePath();
                    // processing of changed files is triggered inside of dispatcher, because each handler method potentially
                    // can change UI, however calling synchronization on lower levels creates additional complexity related
                    // to state synchronization between entities. Synchronization of the whole method significantly reduces code
                    // complexity. However, be careful with performance. File tree is not supposed to be frequently updated, but
                    // making any time-consuming tasks inside these method can dramatically impact UI responsiveness.

                    if (absolutePathOfRelatedFile.toFile().getParentFile().equals(state.rootFile)) {
                        if (ENTRY_CREATE.equals(kind)) {
                            SwingUtilities.invokeLater(() -> handleCreateNewFile(absolutePathOfRelatedFile));
                        } else if (ENTRY_DELETE.equals(kind)) {
                            SwingUtilities.invokeLater(() -> {
                                handleDeleteFile(absolutePathOfRelatedFile);
                                onFileDelete.accept(absolutePathOfRelatedFile);
                            });
                        } else if (ENTRY_MODIFY.equals(kind)) {
                            // Is there anything in the GUI we need to update? I've tested file renames and they work
                            // fine.
                            SwingUtilities.invokeLater(() -> handleModifyFile(absolutePathOfRelatedFile));
                        }
                    }
                }
                watchKey.reset();
            }
        } catch (InterruptedException e) {
            Log.warn("WatchService handler has been interrupted", e);
        } catch (ClosedWatchServiceException e) {
            Log.warn("Watch service has been closed. It is ok, if this happens after close method is called", e);
        }
    }

    private void handleModifyFile(Path pathToModifiedFile) {
        var modifiedFile = pathToModifiedFile.toFile();
        trackerListeners.forEach(l -> l.onAction(FileTrackerListener.Action.MODIFY, modifiedFile));
    }

    private void handleDeleteFile(Path context) {
        Path absolutePath = context.toAbsolutePath();
        DefaultMutableFileTreeNode removedNode = resolveNodeByPath(absolutePath);
        treeModel.removeNodeFromParent(removedNode);
        sortedNodes.remove(context);
        trackerListeners.forEach(l -> l.onAction(FileTrackerListener.Action.REMOVE, absolutePath.toFile()));
    }

    private void handleCreateNewFile(Path context) {
        Path parentPath = context.getParent().toAbsolutePath();
        DefaultMutableFileTreeNode node = resolveNodeByPath(parentPath);
        if (node == null) {
            Log.error("Added file for unknown node...");
            return;
        }
        File newFile = context.toFile();
        try {
            // register watcher if new file is directory
            if (newFile.isDirectory()) {
                Files.walkFileTree(newFile.toPath(), visitor);
                context.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                // tree is updated by visitor above, so no need to do update of model from this method
                return;
            }
        } catch (IOException ex) {
            Log.error("Failed to register watcher for " + context, ex);
        }
        var newNode = new DefaultMutableFileTreeNode(newFile);
        sortedNodes.put(context.toAbsolutePath(), newNode);
        treeModel.insertNodeInto(newNode, node, node.getChildCount());
        trackerListeners.forEach(l -> l.onAction(FileTrackerListener.Action.CREATE, newFile));
    }

    private DefaultMutableFileTreeNode resolveNodeByPath(Path path) {
        return sortedNodes.get(path);
    }

    /**
     * Expands the root node. Otherwise, the user has to expand it manually every time.
     * @param rootNode The root node.
     */
    private void expandRootDirectory(DefaultMutableFileTreeNode rootNode) {
        SwingUtilities.invokeLater(() -> {
            jTree.expandPath(new TreePath(rootNode.getPath()));
        });
    }

    @Override
    public void close() throws IOException {
        Log.info("JFileTree close triggered");
        isClosed.set(true);
        watchService.close();
        watchServiceExecutor.shutdown();
    }

    static class DefaultMutableFileTreeNode extends DefaultMutableTreeNode {

        private final File file;

        public DefaultMutableFileTreeNode(File file) {
            super(file.getName(), file.isDirectory());
            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }

    public static class State {

        public volatile File rootFile;
    }
}
