package com.bookmap.python.api.addon.services;

import com.bookmap.python.api.addon.ui.listeners.EditorStateListener;
import com.bookmap.python.api.addon.utils.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JTextArea;

public class TextEditorFileSaver implements ContentFileSaver {

    private final JTextArea editor;
    private final EditorStateListener stateListener;
    private final HashMap<String, String> fileNameToUnsavedText;
    private final HashSet<String> fileNamesWithUnsavedChanges;

    public TextEditorFileSaver(
        JTextArea area,
        EditorStateListener stateListener,
        HashMap<String, String> fileNameToUnsavedText,
        HashSet<String> fileNamesWithUnsavedChanges
    ) {
        this.editor = area;
        this.stateListener = stateListener;
        this.fileNameToUnsavedText = fileNameToUnsavedText;
        this.fileNamesWithUnsavedChanges = fileNamesWithUnsavedChanges;
    }

    @Override
    public void save(File file) throws IOException {
        Log.info("Saving file: " + file.getName());

        String textToSave = editor.getText();
        stateListener.refreshEditorState(textToSave);
        Files.writeString(
            file.getAbsoluteFile().toPath(),
            textToSave,
            new StandardOpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING }
        );

        fileNameToUnsavedText.remove(file.getName());
        fileNamesWithUnsavedChanges.remove(file.getName());
    }
}
