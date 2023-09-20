package com.bookmap.api.rpc.server.ui.listeners;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * Improvement of {@link DocumentListener} allowing to avoid redundancy of the interface
 * incorporating three callbacks into the one.
 */
public abstract class DocumentListenerAdapter implements DocumentListener {

	private  static final String EMTPY_STRING = "";

	public final void insertUpdate(DocumentEvent e) {
		callOnReplace(e);
	}

	public final void removeUpdate(DocumentEvent e) {
		callOnReplace(e);
	}

	public final void changedUpdate(DocumentEvent e) {
		callOnReplace(e);
	}

	private void callOnReplace(DocumentEvent event) {
		try {
			var doc = event.getDocument();
			if (doc.getLength() > 0) {
				onReplace(event.getDocument().getText(0, doc.getLength()));
			} else {
				onReplace(EMTPY_STRING);
			}
		} catch (BadLocationException ex) {
			throw new RuntimeException(ex);
		}
	}

	public abstract void onReplace(String newText);
}
