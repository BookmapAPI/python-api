package com.bookmap.python.api.addon.ui.listeners;

import com.bookmap.python.api.addon.ui.filetree.JFileTree;
import java.io.File;

/**
 * This listener is supposed to be used with {@link JFileTree} class. It allows to handle file selection in the file
 * tree. Each time node is selected {@link FileTreeSelectionListener#fileSelected(File)} is triggered with respective
 * file. It supports only {@link javax.swing.tree.TreeSelectionModel#SINGLE_TREE_SELECTION}.
 */
public interface FileTreeSelectionListener {
    /**
     * Triggered each time node in file tree is selected.
     *
     * @param file respected file to a selected file node
     */
    void fileSelected(File file);

    /**
     * Returns selected file
     *
     * @return selected file or null if no selected
     */
    File getSelectedFile();
}
