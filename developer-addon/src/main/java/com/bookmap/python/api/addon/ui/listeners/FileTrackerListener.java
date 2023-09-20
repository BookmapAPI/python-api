package com.bookmap.python.api.addon.ui.listeners;

import com.bookmap.python.api.addon.ui.filetree.JFileTree;
import java.io.File;

public interface FileTrackerListener {
    /**
     * Called each time when {@link Action } happen with file tracked by {@link JFileTree}
     *
     * @param action specifies what exactly happened with the file
     * @param file   reference to exact file
     */
    void onAction(Action action, File file);

    enum Action {
        CREATE,
        MODIFY,
        REMOVE,
    }
}
