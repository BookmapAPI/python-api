package com.bookmap.python.api.addon.ui.listeners;

import com.bookmap.python.api.addon.utils.Log;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class EditorStateListener implements DocumentListener {

    private String savedText;
    private final StringBuilder pendingChanges = new StringBuilder();

    @Override
    public void insertUpdate(DocumentEvent e) {
        validateSwingEventLoopThread();
        int beginIndex = e.getOffset();
        try {
            String insertedText = e.getDocument().getText(beginIndex, e.getLength());
            pendingChanges.insert(beginIndex, insertedText);
        } catch (BadLocationException ex) {
            Log.error("Failed to get a text from the document. Error with location of the text", ex);
            throw new IllegalStateException("Failed to update editor state", ex);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateSwingEventLoopThread();
        int beginIndex = e.getOffset();
        int endIndex = beginIndex + e.getLength();
        pendingChanges.delete(beginIndex, endIndex);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // ignore
    }

    /**
     * Sets new text to the state and refreshes pending changes setting them to the same value
     *
     * @param savedText new text of the editor
     */
    public void refreshEditorState(String savedText) {
        validateSwingEventLoopThread();
        this.savedText = savedText;
        pendingChanges.setLength(0);
        pendingChanges.append(savedText);
    }

    /**
     * @return true if unsaved changes persist, otherwise false
     */
    public boolean areChangesUnsaved() {
        validateSwingEventLoopThread();
        return !pendingChanges.toString().equals(savedText);
    }

    private void validateSwingEventLoopThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Thread call is made not from Swing event dispatcher!");
        }
    }
}
