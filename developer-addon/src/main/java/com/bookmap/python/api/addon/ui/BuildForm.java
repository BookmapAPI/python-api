package com.bookmap.python.api.addon.ui;

import com.bookmap.python.api.addon.ui.listeners.DocumentChangeListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class BuildForm extends JDialog {

    // used to point that component should be always at the bottom of the GridBagLayout
    private static final int MAX_Y_POSITION = 1024;

    private final Map<String, Component> valueComponents = new HashMap<>();
    private final Map<String, Boolean> keyToAutomaticallySetComponents = new HashMap<>();
    private final Map<String, Boolean> keyToValidField = new HashMap<>();
    private final JButton button = new JButton("Ok");

    public boolean isOkClicked = false;

    public BuildForm(String title, boolean isModal) {
        super((Frame) null, title, isModal);
        // dispose window on button
        button.addActionListener(l -> {
            isOkClicked = true;
            setVisible(false);
            dispose();
        });

        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(480, 320));
        setResizable(false);
        button.setEnabled(false);

        var okConstraints = new GridBagConstraints();
        okConstraints.gridx = 0;
        okConstraints.weightx = 1;
        okConstraints.weighty = 1;
        okConstraints.gridy = MAX_Y_POSITION;
        okConstraints.anchor = GridBagConstraints.WEST;
        okConstraints.insets = new Insets(5, 10, 5, 0);
        add(button, okConstraints);
    }

    //TODO: do a builder from it maybe?
    public void addStringField(
        String key,
        String title,
        String defaultValue,
        boolean setAutomaticallySupported,
        Predicate<String> isValidField
    ) {
        if (valueComponents.containsKey(key)) {
            throw new IllegalStateException("String field with key = " + key + " is already in the form");
        }
        add(new JLabel(title), createConstraints(0));
        var field = new JTextField(defaultValue == null ? "" : defaultValue);
        if (setAutomaticallySupported) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.addChangeListener(e -> {
                boolean selected = checkBox.isSelected();
                field.setEnabled(!selected);
                keyToAutomaticallySetComponents.put(key, selected);
                if (selected) {
                    keyToValidField.put(key, true);
                } else {
                    keyToValidField.put(key, isValidField == null || isValidField.test(field.getText()));
                }
                validateOkButton();
            });
            checkBox.setSelected(false);
            checkBox.setToolTipText("Set automatically");
            add(checkBox, createConstraints(1));
            validateOkButton();
        }

        if (isValidField != null) {
            field
                .getDocument()
                .addDocumentListener(
                    new DocumentChangeListener() {
                        @Override
                        public void onDocumentChanged(String newText) {
                            if (keyToAutomaticallySetComponents.getOrDefault(key, false)) {
                                keyToValidField.put(key, true);
                                button.setEnabled(true);
                            } else {
                                boolean isStrValid = isValidField.test(newText);
                                keyToValidField.put(key, isStrValid);
                                button.setEnabled(isStrValid);
                            }
                            validateOkButton();
                        }
                    }
                );
            keyToValidField.put(key, isValidField.test(field.getText()));
        } else {
            keyToValidField.put(key, true);
        }

        add(field, createConstraints(2));
        valueComponents.put(key, field);
        validateOkButton();
    }

    public void addFileChooserField(
        String key,
        String title,
        String buttonText,
        FileFilter fileFilter,
        boolean setAutomaticallySupported,
        Predicate<File> isValidField
    ) {
        if (valueComponents.containsKey(key)) {
            throw new IllegalStateException("File chooser field with key = " + key + " is already in the form");
        }
        add(new JLabel(title), createConstraints(0));
        var fileChooser = new JFileChooser();
        fileChooser.setFileFilter(fileFilter);
        var selectFileButton = new JButton(buttonText);
        add(selectFileButton);

        // TODO: move default values to generic code
        if (setAutomaticallySupported) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.addChangeListener(e -> {
                boolean selected = checkBox.isSelected();
                selectFileButton.setEnabled(!selected);
                keyToAutomaticallySetComponents.put(key, selected);
                if (selected) {
                    keyToValidField.put(key, true);
                } else {
                    keyToValidField.put(key, isValidField == null || isValidField.test(fileChooser.getSelectedFile()));
                }
                validateOkButton();
            });
            checkBox.setSelected(false);
            checkBox.setToolTipText("Set automatically");
            add(checkBox, createConstraints(1));
            keyToValidField.put(key, false);
            keyToAutomaticallySetComponents.put(key, false);
            validateOkButton();
        }
        selectFileButton.addActionListener(l -> {
            int resp = fileChooser.showDialog(button, "Select");
            keyToValidField.put(key, isValidField == null || isValidField.test(fileChooser.getSelectedFile()));
            validateOkButton();
            if (resp == JFileChooser.APPROVE_OPTION) {
                selectFileButton.setText(fileChooser.getSelectedFile().getName());
            }
        });
        add(selectFileButton, createConstraints(2));
        valueComponents.put(key, fileChooser);
    }

    public ResponseForm showDialog() {
        pack();
        setVisible(true);
        if (!isOkClicked) {
            return null;
        }
        Map<String, ResponseForm.FieldResponse> respFields = new HashMap<>();
        for (Map.Entry<String, Component> componentEntry : valueComponents.entrySet()) {
            Component component = componentEntry.getValue();
            String key = componentEntry.getKey();
            if (component instanceof JTextField) {
                String text = ((JTextField) component).getText();
                boolean selectedAutomatically = keyToAutomaticallySetComponents.getOrDefault(key, false);
                respFields.put(key, new ResponseForm.FieldResponse(selectedAutomatically, text));
                continue;
            }
            if (component instanceof JFileChooser) {
                File file = ((JFileChooser) component).getSelectedFile();
                boolean selectedAutomatically = keyToAutomaticallySetComponents.getOrDefault(key, false);
                respFields.put(key, new ResponseForm.FieldResponse(selectedAutomatically, file));
            }
        }

        return new ResponseForm(respFields);
    }

    private void validateOkButton() {
        if (!keyToValidField.containsValue(Boolean.FALSE)) {
            button.setEnabled(true);
            return;
        }
        button.setEnabled(false);
    }

    private GridBagConstraints createConstraints(int xPosition) {
        var constaints = new GridBagConstraints();
        constaints.gridx = xPosition;
        constaints.gridy = valueComponents.size();
        constaints.anchor = GridBagConstraints.EAST;
        constaints.insets = new Insets(5, 10, 5, 10);
        // give less space for label and more space for field
        constaints.weightx = 1;
        constaints.weighty = 1;
        constaints.fill = GridBagConstraints.HORIZONTAL;
        if (xPosition < 2) {
            constaints.fill = GridBagConstraints.NONE;
            constaints.weightx = 0.26;
            constaints.anchor = GridBagConstraints.WEST;
            constaints.insets = new Insets(5, 10, 5, 0);
        }

        return constaints;
    }

    public static class ResponseForm {

        public final Map<String, FieldResponse> response;

        private ResponseForm(Map<String, FieldResponse> response) {
            this.response = response;
        }

        public static class FieldResponse {

            public final boolean setAutomatically;
            private final Object value;

            public FieldResponse(boolean setAutomatically, Object value) {
                this.setAutomatically = setAutomatically;
                this.value = value;
            }

            // unsafe case, but type should be kept with FieldResponse.type value
            public <T> T getValue(Class<T> tClass) {
                return tClass.cast(value);
            }
        }
    }
}
