package com.bookmap.python.api.addon.ui.listeners;

import com.bookmap.python.api.addon.services.ContentFileSaver;
import com.bookmap.python.api.addon.utils.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.function.Consumer;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Responsible for depicting file text in text area.
 */
public class SavingTextEditorFileSelectionListener implements FileTreeSelectionListener {

    private volatile File selectedFile;

    private final EditorStateListener editorStateListener;
    private final ContentFileSaver contentFileSaver;
    private final RTextScrollPane textEditorScrollPanel;
    private final Consumer<String> onFileSelect;
    private final HashMap<String, String> fileNameToUnsavedText;

    public SavingTextEditorFileSelectionListener(
        EditorStateListener editorStateListener,
        ContentFileSaver contentFileSaver,
        RTextScrollPane textEditorScrollPanel,
        Consumer<String> onFileSelect,
        HashMap<String, String> fileNameToUnsavedText
    ) {
        this.editorStateListener = editorStateListener;
        this.contentFileSaver = contentFileSaver;
        this.textEditorScrollPanel = textEditorScrollPanel;
        this.onFileSelect = onFileSelect;
        this.fileNameToUnsavedText = fileNameToUnsavedText;
    }

    @Override
    public void fileSelected(File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            return;
        }

        selectedFile = file;
        try {
            String text = fileNameToUnsavedText.getOrDefault(file.getName(), null);
            if (text == null) {
                text = Files.readString(file.toPath());
            }

            onFileSelect.accept(file.getName());

            textEditorScrollPanel.getTextArea().setText(text);
            textEditorScrollPanel.getTextArea().setCaretPosition(0);
            editorStateListener.refreshEditorState(text);
            textEditorScrollPanel.getTextArea().discardAllEdits();
        } catch (IOException e) {
            Log.error("Failed to operate with file", e);
        }
    }

    @Override
    public File getSelectedFile() {
        return selectedFile;
    }
}
