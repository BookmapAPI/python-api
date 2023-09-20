package com.bookmap.python.api.addon.ui.listeners;

import com.bookmap.python.api.addon.services.ContentFileSaver;
import com.bookmap.python.api.addon.utils.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;
import javax.swing.JOptionPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Listener that updates editor text according to changes made in the file
 */
public class EditorTextFileTrackerListener implements FileTrackerListener {

    private final EditorStateListener stateListener;
    private final FileTreeSelectionListener fileTreeSelectionListener;
    private final ContentFileSaver contentFileSaver;
    private final RSyntaxTextArea editorArea;
    private final HashSet<String> fileNamesWithUnsavedChanges;
    private boolean isDialogShown = false;

    public EditorTextFileTrackerListener(
        EditorStateListener stateListener,
        FileTreeSelectionListener fileTreeSelectionListener,
        ContentFileSaver contentFileSaver,
        RSyntaxTextArea editorArea,
        HashSet<String> fileNamesWithUnsavedChanges
    ) {
        this.stateListener = stateListener;
        this.fileTreeSelectionListener = fileTreeSelectionListener;
        this.contentFileSaver = contentFileSaver;
        this.editorArea = editorArea;
        this.fileNamesWithUnsavedChanges = fileNamesWithUnsavedChanges;
    }

    @Override
    public void onAction(Action action, File file) {
        // we are interested in only modify events here
        if (action != FileTrackerListener.Action.MODIFY) {
            return;
        }

        // ignore if not selected file modified
        if (!file.equals(fileTreeSelectionListener.getSelectedFile())) {
            Log.info(
                "Selected file does not match modified file, selected: " +
                fileTreeSelectionListener.getSelectedFile() +
                ", modified: " +
                file
            );
            return;
        }

        // if there is no pending changes, we just synchronize editor window with file
        if (!stateListener.areChangesUnsaved()) {
            applyChangesFromFile(file);
            return;
        }

        if (isDialogShown) {
            return;
        }
        isDialogShown = true;

        // note that once we show modal window, focus to editor is lost, thus local editor changes are applied and saved to disk. So, to remember previous
        // set of changes, we need to track them here.
        String text;
        try {
            text = getFileContent(file);
        } catch (NoSuchFileException e) {
            Log.info(String.format("The file %s has been deleted.", file));
            text = "";
            fileNamesWithUnsavedChanges.remove(file.getName());
            return;
        } catch (IOException e) {
            throw new RuntimeException("Failed to apply changes from file.", e);
        }
        // If editing happens from two or more sources, sometimes there races can appear when some changes from external source are applies on disk,
        // but locally you make completely different set of changes. In this case, it is unclear what exactly should be a final version of the file.
        // So, by default we apply local changes from the editor, however show modal window proposing to revert everything to disk changes.

        int result = JOptionPane.showOptionDialog(
            null,
            "File on disk has been changed and differs from your local changes." +
            "\nWould you like to reload the file from disk? Your local changes here will be discarded.",
            "File conflict: " + file.getName(),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            null,
            null
        );

        isDialogShown = false;
        if (result == JOptionPane.YES_OPTION) {
            applyNewText(text);
            try {
                contentFileSaver.save(file);
            } catch (IOException e) {
                Log.error("Failed to apply changes", e);
                throw new RuntimeException("Failed to overwrite changes", e);
            }
        }
    }

    private void applyNewText(String text) {
        editorArea.setText(text);
        stateListener.refreshEditorState(text);
        editorArea.discardAllEdits();
    }

    private void applyChangesFromFile(File file) {
        String text;
        try {
            text = getFileContent(file);
        } catch (NoSuchFileException e) {
            Log.info(String.format("The file %s has been deleted.", file));
            text = "";
            fileNamesWithUnsavedChanges.remove(file.getName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to apply changes from file.", e);
        }
        if (!editorArea.getText().equals(text)) {
            applyNewText(text);
        }
    }

    private String getFileContent(File file) throws IOException {
        return Files.readString(file.toPath());
    }
}
