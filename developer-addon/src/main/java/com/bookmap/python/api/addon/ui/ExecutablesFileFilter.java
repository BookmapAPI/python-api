package com.bookmap.python.api.addon.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ExecutablesFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.canExecute();
    }

    @Override
    public String getDescription() {
        return "Only executable files";
    }
}
