package com.bookmap.python.api.addon.ui.listeners;

import com.bookmap.python.api.addon.utils.Log;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public abstract class DocumentChangeListener implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
        redirectNewDocumentText(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        redirectNewDocumentText(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        redirectNewDocumentText(e);
    }

    private void redirectNewDocumentText(DocumentEvent event) {
        var doc = event.getDocument();
        try {
            onDocumentChanged(doc.getText(0, doc.getLength()));
        } catch (BadLocationException ex) {
            Log.error("Failed to redirect callback", ex);
            throw new RuntimeException(ex);
        }
    }

    public abstract void onDocumentChanged(String newText);
}
