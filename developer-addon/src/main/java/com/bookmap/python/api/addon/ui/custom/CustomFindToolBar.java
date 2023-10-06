package com.bookmap.python.api.addon.ui.custom;

/*
 * 09/20/2013
 *
 * FindToolBar - A toolbar for "find" operations in text areas.
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTAUI.license.txt file for details.
 */

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.fife.rsta.ui.AssistanceIconPanel;
import org.fife.rsta.ui.UIUtil;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.FindReplaceButtonsEnableResult;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.SearchComboBox;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rtextarea.SearchContext;

/**
 * A toolbar for search operations in a text editor application.  This provides
 * a more seamless experience than using a Find or Replace dialog.
 *
 * @author Robert Futrell
 * @version 0.5
 * @see FindDialog
 */
public class CustomFindToolBar extends FindToolBar {

    private SearchContext context;
    protected ToolBarListener listener;
    protected FindFieldListener findFieldListener;
    protected SearchComboBox findCombo;
    protected SearchComboBox replaceCombo;
    protected JButton findButton;
    protected JButton findPrevButton;
    protected JCheckBox matchCaseCheckBox;
    protected JCheckBox wholeWordCheckBox;
    protected JCheckBox regexCheckBox;
    protected JCheckBox markAllCheckBox;
    protected JCheckBox wrapCheckBox;
    private JLabel infoLabel;
    private Timer markAllTimer;

    /**
     * Flag to prevent double-modification of SearchContext when e.g. a
     * FindToolBar and ReplaceToolBar share the same SearchContext.
     */
    private boolean settingFindTextFromEvent;

    protected static final ResourceBundle SEARCH_MSG = ResourceBundle.getBundle("org.fife.rsta.ui.search.Search");
    protected static final ResourceBundle MSG = ResourceBundle.getBundle("org.fife.rsta.ui.search.SearchToolBar");

    /**
     * Creates the toolbar.
     *
     * @param listener An entity listening for search events.
     */
    public CustomFindToolBar(SearchListener listener) {
        super(listener);

        // Keep focus in this component when tabbing through search controls
        setFocusCycleRoot(true);

        markAllTimer = new Timer(300, new MarkAllEventNotifier());
        markAllTimer.setRepeats(false);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        addSearchListener(listener);
        this.listener = new ToolBarListener();

        // The user should set a shared instance between all subclass
        // instances, but to be safe we set individual ones.
        setSearchContext(new SearchContext());

        ComponentOrientation orientation = ComponentOrientation.
                getOrientation(getLocale());

        add(Box.createHorizontalStrut(5));

        add(createFieldPanel());

        Box rest = new Box(BoxLayout.LINE_AXIS);
        add(rest, BorderLayout.LINE_END);

        rest.add(Box.createHorizontalStrut(5));
        rest.add(createButtonPanel());
        rest.add(Box.createHorizontalStrut(15));

        JLabel infoLabel = new JLabel();
        rest.add(infoLabel);

        rest.add(Box.createHorizontalGlue());

        // Get ready to go.
        applyComponentOrientation(orientation);
        // Keep focus in this component when tabbing through search controls
    }

    /**
     * Adds a {@link SearchListener} to this toolbar.  This listener will
     * be notified when find or replace operations are triggered.
     *
     * @param l The listener to add.
     * @see #removeSearchListener(SearchListener)
     */
    public void addSearchListener(SearchListener l) {
        listenerList.add(SearchListener.class, l);
    }

    protected Container createButtonPanel() {
        Box panel = new Box(BoxLayout.LINE_AXIS);
        createFindButtons();

        JPanel filler = new JPanel(new BorderLayout());
        filler.setBorder(BorderFactory.createEmptyBorder());
        filler.add(findButton); //bp);
        panel.add(filler);
        panel.add(Box.createHorizontalStrut(6));

        matchCaseCheckBox = createCB("MatchCase");
        panel.add(matchCaseCheckBox);

        regexCheckBox = createCB("RegEx");
        panel.add(regexCheckBox);

        wholeWordCheckBox = createCB("WholeWord");
        panel.add(wholeWordCheckBox);

        markAllCheckBox = createCB("MarkAll");
        panel.add(markAllCheckBox);

        wrapCheckBox = createCB("Wrap");
        panel.add(wrapCheckBox);

        return panel;
    }

    protected JCheckBox createCB(String key) {
        JCheckBox cb = new JCheckBox(SEARCH_MSG.getString(key));
        cb.setBorder(new EmptyBorder(0, 5, 0, 5));
        cb.addActionListener(listener);
        cb.addMouseListener(listener);
        return cb;
    }

    /**
     * Wraps the specified component in a panel with a leading "content assist
     * available" icon in front of it.
     *
     * @param comp The component with content assist.
     * @return The wrapper panel.
     */
    protected Container createContentAssistablePanel(JComponent comp) {
        JPanel temp = new JPanel(new BorderLayout());
        temp.add(comp);
        AssistanceIconPanel aip = new AssistanceIconPanel(comp);
        temp.add(aip, BorderLayout.LINE_START);
        return temp;
    }

    protected Container createFieldPanel() {
        findFieldListener = new FindFieldListener();
        JPanel temp = new JPanel(new BorderLayout());

        findCombo = new SearchComboBox(this, false);
        JTextComponent findField = UIUtil.getTextComponent(findCombo);
        findFieldListener.install(findField);
        temp.add(createContentAssistablePanel(findCombo));

        return temp;
    }

    /**
     * Creates the buttons for this toolbar.
     */
    protected void createFindButtons() {
        findPrevButton = new JButton(MSG.getString("FindPrev"));
        makeEnterActivateButton(findPrevButton);
        findPrevButton.setActionCommand("FindPrevious");
        findPrevButton.addActionListener(listener);
        findPrevButton.setEnabled(false);

        findButton =
            new JButton(SEARCH_MSG.getString("Find")) {
                @Override
                public Dimension getPreferredSize() {
                    return findPrevButton.getPreferredSize(); // Always bigger
                }
            };
        makeEnterActivateButton(findButton);
        findButton.setToolTipText(MSG.getString("Find.ToolTip"));
        findButton.setActionCommand("FindNext");
        findButton.addActionListener(listener);
        findButton.setEnabled(false);
    }

    /**
     * Forces a "mark all" event to be sent out, if "mark all" is enabled.
     *
     * @param delay If the delay should be honored.
     */
    protected void doMarkAll(boolean delay) {
        if (context.getMarkAll() && !settingFindTextFromEvent) {
            if (delay) {
                markAllTimer.restart();
            } else {
                fireMarkAllEvent();
            }
        }
    }

    void doSearch(boolean forward) {
        if (forward) {
            findButton.doClick(0);
        } else {
            findPrevButton.doClick(0);
        }
    }

    /**
     * Fires a "mark all" search event.
     */
    private void fireMarkAllEvent() {
        SearchEvent se = new SearchEvent(this, SearchEvent.Type.MARK_ALL, context);
        fireSearchEvent(se);
    }

    /**
     * Notifies all listeners that have registered interest in notification of
     * this event type. The event instance is lazily created using the
     * <code>event</code> parameter.
     *
     * @param e The <code>ActionEvent</code> object coming from a
     *        child component.
     */
    protected void fireSearchEvent(SearchEvent e) {
        // Process the listeners last to first, notifying
        // those that are interested in this event
        SearchListener[] listeners = listenerList.getListeners(SearchListener.class);
        int count = listeners == null ? 0 : listeners.length;
        for (int i = count - 1; i >= 0; i--) {
            listeners[i].searchEvent(e);
        }
    }

    protected String getFindText() {
        return UIUtil.getTextComponent(findCombo).getText();
    }

    /**
     * Returns the delay between when the user types and when a "mark all"
     * event is fired (assuming "mark all" is enabled), in milliseconds.
     *
     * @return The delay.
     * @see #setMarkAllDelay(int)
     */
    public int getMarkAllDelay() {
        return markAllTimer.getInitialDelay();
    }

    protected String getReplaceText() {
        if (replaceCombo == null) {
            return null;
        }
        return UIUtil.getTextComponent(replaceCombo).getText();
    }

    /**
     * Returns the search context for this toolbar.
     *
     * @return The search context.
     * @see #setSearchContext(SearchContext)
     */
    public SearchContext getSearchContext() {
        return context;
    }

    /**
     * Called when the regex checkbox is clicked (or its value is modified
     * via a change to the search context).  Subclasses can override
     * to add custom behavior, but should call the super implementation.
     */
    protected void handleRegExCheckBoxClicked() {
        handleToggleButtons();
        // "Content assist" support
        boolean b = regexCheckBox.isSelected();
        findCombo.setAutoCompleteEnabled(b);
    }

    /**
     * Creates a search event object and notifies all registered listeners.
     *
     * @param e The event.
     */
    protected void handleSearchAction(ActionEvent e) {
        SearchEvent.Type type = null;
        boolean forward = true;
        String action = e.getActionCommand();
        // JTextField returns *_DOWN_* modifiers, JButton returns the others (!)
        int allowedModifiers =
            InputEvent.CTRL_DOWN_MASK |
            InputEvent.SHIFT_DOWN_MASK | // field
            InputEvent.CTRL_MASK |
            InputEvent.SHIFT_MASK; // JButton

        if ("FindNext".equals(action)) {
            type = SearchEvent.Type.FIND;
            int mods = e.getModifiers();
            forward = (mods & allowedModifiers) == 0;
            // Add the item to the combo box's list, if it isn't already there.
            JTextComponent tc = UIUtil.getTextComponent(findCombo);
            findCombo.addItem(tc.getText());
        } else if ("FindPrevious".equals(action)) {
            type = SearchEvent.Type.FIND;
            forward = false;
            // Add the item to the combo box's list, if it isn't already there.
            JTextComponent tc = UIUtil.getTextComponent(findCombo);
            findCombo.addItem(tc.getText());
        } else if ("Replace".equals(action)) {
            type = SearchEvent.Type.REPLACE;
            int mods = e.getModifiers();
            forward = (mods & allowedModifiers) == 0;
            // Add the item to the combo box's list, if it isn't already there.
            JTextComponent tc = UIUtil.getTextComponent(findCombo);
            findCombo.addItem(tc.getText());
            tc = UIUtil.getTextComponent(replaceCombo);
            replaceCombo.addItem(tc.getText());
        } else if ("ReplaceAll".equals(action)) {
            type = SearchEvent.Type.REPLACE_ALL;
            // Add the item to the combo box's list, if it isn't already there.
            JTextComponent tc = UIUtil.getTextComponent(findCombo);
            findCombo.addItem(tc.getText());
            tc = UIUtil.getTextComponent(replaceCombo);
            replaceCombo.addItem(tc.getText());
        }

        context.setSearchFor(getFindText());
        if (replaceCombo != null) {
            context.setReplaceWith(replaceCombo.getSelectedString());
        }

        // If the ask is to search in the opposite direction from the UI's
        // current direction, use a cloned search context to avoid updating
        // the UI when we shouldn't (e.g. from a keyboard shortcut to search
        // backward).
        SearchContext contextToFire = context;
        if (forward != context.getSearchForward()) {
            contextToFire = context.clone();
            contextToFire.setSearchForward(forward);
        }

        SearchEvent se = new SearchEvent(this, type, contextToFire);
        fireSearchEvent(se);
        handleToggleButtons(); // Replace button could toggle state
    }

    /**
     * Returns whether any action-related buttons (Find Next, Replace, etc.)
     * should be enabled.  Subclasses can call this method when the "Find What"
     * or "Replace With" text fields are modified.  They can then
     * enable/disable any components as appropriate.
     *
     * @return Whether the buttons should be enabled.
     */
    protected FindReplaceButtonsEnableResult handleToggleButtons() {
        FindReplaceButtonsEnableResult result = new FindReplaceButtonsEnableResult(true, null);

        String text = getFindText();
        if (text.length() == 0) {
            result = new FindReplaceButtonsEnableResult(false, null);
        } else if (regexCheckBox.isSelected()) {
            try {
                Pattern.compile(text);
            } catch (PatternSyntaxException pse) {
                result = new FindReplaceButtonsEnableResult(false, pse.getMessage());
            }
        }

        boolean enable = result.getEnable();
        findButton.setEnabled(enable);
        findPrevButton.setEnabled(enable);

        // setBackground doesn't show up with XP Look and Feel!
        JTextComponent tc = UIUtil.getTextComponent(findCombo);
        tc.setForeground(enable ? UIManager.getColor("TextField.foreground") : UIUtil.getErrorTextForeground());

        String tooltip = getToolTip(result);
        tc.setToolTipText(tooltip); // Always set, even if null

        return result;
    }

    public static String getToolTip(FindReplaceButtonsEnableResult res) {
        String tooltip = res.getError();
        if (tooltip != null && tooltip.indexOf('\n') > -1) {
            tooltip = tooltip.replaceFirst("\\\n", "</b><br><pre>");
            tooltip = "<html><b>" + tooltip;
        }
        return tooltip;
    }

    /**
     * Initializes the UI in this toolbar from a search context.  This is
     * called whenever a new search context is installed on this toolbar
     * (which should practically be never).
     */
    private void initUIFromContext() {
        if (findCombo == null) { // First time through, stuff not initialized yet
            return;
        }
        setFindText(context.getSearchFor());
        if (replaceCombo != null) {
            setReplaceText(context.getReplaceWith());
        }
        matchCaseCheckBox.setSelected(context.getMatchCase());
        wholeWordCheckBox.setSelected(context.getWholeWord());
        regexCheckBox.setSelected(context.isRegularExpression());
        markAllCheckBox.setSelected(context.getMarkAll());
        wrapCheckBox.setSelected(context.getSearchWrap());
    }

    /**
     * Makes the Enter key activate the button.  In Swing, this is a
     * complicated thing.  It's LAF-dependent whether this works
     * automatically; on most LAFs, it doesn't happen.  In WindowsLookAndFeel
     * it does, but *only* if the current window has a "default" button
     * specified.  Since these toolbars will typically be used in "main"
     * application windows, which don't have default buttons, we'll just
     * enable this property here and now.
     *
     * @param button The button that should respond to the Enter key.
     */
    protected void makeEnterActivateButton(JButton button) {
        InputMap im = button.getInputMap();

        // Make "enter" being typed simulate clicking
        im.put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        im.put(KeyStroke.getKeyStroke("released ENTER"), "released");

        // Make "shift+enter" being typed simulate clicking also.  The listener
        // will handle the backwards searching.  Not sure why the commented-out
        // versions don't work, possibly SHIFT_MASK vs. SHIFT_DOWN_MASK issue.
        //im.put(KeyStroke.getKeyStroke("pressed SHIFT ENTER"), "pressed");
        //im.put(KeyStroke.getKeyStroke("released SHIFT ENTER"), "released");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK, false), "pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK, true), "released");
    }

    /**
     * Removes a {@link SearchListener} from this toolbar.
     *
     * @param l The listener to remove
     * @see #addSearchListener(SearchListener)
     */
    public void removeSearchListener(SearchListener l) {
        listenerList.remove(SearchListener.class, l);
    }

    /**
     * Makes the find field on this toolbar request focus.  If it is already
     * focused, its text is selected.
     */
    @Override
    public boolean requestFocusInWindow() {
        JTextComponent findField = UIUtil.getTextComponent(findCombo);
        findField.selectAll();
        return findField.requestFocusInWindow();
    }

    /**
     * Callback called when a contained combo box has its LookAndFeel
     * modified.  This is a hack for us to add listeners back to it.
     *
     * @param combo The combo box.
     */
    void searchComboUpdateUICallback(SearchComboBox combo) {
        findFieldListener.install(UIUtil.getTextComponent(combo));
    }

    /**
     * Sets the image to display by this dialog's text fields when content
     * assistance is available.
     *
     * @param image The image.  If this is {@code null}, a default image
     *        (a light bulb) is used.  This should be kept small, around
     *        8x8 for standard resolution monitors.
     */
    public void setContentAssistImage(Image image) {
        findCombo.setContentAssistImage(image);
    }

    protected void setFindText(String text) {
        UIUtil.getTextComponent(findCombo).setText(text);
        //findCombo.setSelectedItem(text);
    }

    /**
     * Sets the delay between when the user types and when a "mark all"
     * event is fired (assuming "mark all" is enabled), in milliseconds.
     *
     * @param millis The new delay.  This should be &gt;= zero.
     * @see #getMarkAllDelay()
     */
    public void setMarkAllDelay(int millis) {
        markAllTimer.setInitialDelay(millis);
    }

    protected void setReplaceText(String text) {
        if (replaceCombo != null) {
            UIUtil.getTextComponent(replaceCombo).setText(text);
            //replaceCombo.setSelectedItem(text);
        }
    }

    /**
     * Sets the search context for this toolbar.  You'll usually want to call
     * this method for all toolbars and give them the same search context,
     * so that their options (match case, etc.) stay in sync with one another.
     *
     * @param context The new search context.  This cannot be <code>null</code>.
     * @see #getSearchContext()
     */
    public void setSearchContext(SearchContext context) {
        if (this.context != null) {
            this.context.removePropertyChangeListener(listener);
        }
        this.context = context;
        this.context.addPropertyChangeListener(listener);
        initUIFromContext();
    }

    /**
     * Listens for events in this toolbar.  Keeps the UI in sync with the
     * search context and vice versa.
     */
    private class ToolBarListener extends MouseAdapter implements ActionListener, PropertyChangeListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source == matchCaseCheckBox) {
                context.setMatchCase(matchCaseCheckBox.isSelected());
                if (markAllCheckBox.isSelected()) {
                    doMarkAll(false);
                }
            } else if (source == wholeWordCheckBox) {
                context.setWholeWord(wholeWordCheckBox.isSelected());
                if (markAllCheckBox.isSelected()) {
                    doMarkAll(false);
                }
            } else if (source == regexCheckBox) {
                context.setRegularExpression(regexCheckBox.isSelected());
                if (markAllCheckBox.isSelected()) {
                    doMarkAll(false);
                }
            } else if (source == markAllCheckBox) {
                context.setMarkAll(markAllCheckBox.isSelected());
                fireMarkAllEvent(); // Force an event to be fired
            } else if (source == wrapCheckBox) {
                context.setSearchWrap(wrapCheckBox.isSelected());
            } else {
                handleSearchAction(e);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() instanceof JCheckBox) { // Always true
                findFieldListener.selectAll = false;
                findCombo.requestFocusInWindow();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // A property changed on the context itself.
            String prop = e.getPropertyName();

            if (SearchContext.PROPERTY_MATCH_CASE.equals(prop)) {
                boolean newValue = (Boolean) e.getNewValue();
                matchCaseCheckBox.setSelected(newValue);
            } else if (SearchContext.PROPERTY_MATCH_WHOLE_WORD.equals(prop)) {
                boolean newValue = (Boolean) e.getNewValue();
                wholeWordCheckBox.setSelected(newValue);
            }
            //else if (SearchContext.PROPERTY_SEARCH_FORWARD.equals(prop)) {
            //	boolean newValue = ((Boolean)e.getNewValue()).booleanValue();
            //	...
            //}
            //else if (SearchContext.PROPERTY_SELECTION_ONLY.equals(prop)) {
            //	boolean newValue = ((Boolean)e.getNewValue()).booleanValue();
            //	...
            //}
            else if (SearchContext.PROPERTY_USE_REGEX.equals(prop)) {
                boolean newValue = (Boolean) e.getNewValue();
                regexCheckBox.setSelected(newValue);
                handleRegExCheckBoxClicked();
            } else if (SearchContext.PROPERTY_MARK_ALL.equals(prop)) {
                boolean newValue = (Boolean) e.getNewValue();
                markAllCheckBox.setSelected(newValue);
                // firing event handled in ActionListener, to prevent "other"
                // toolbar from firing a second event
            } else if (SearchContext.PROPERTY_SEARCH_FOR.equals(prop)) {
                String newValue = (String) e.getNewValue();
                String oldValue = getFindText();
                // Prevents IllegalStateExceptions
                if (!newValue.equals(oldValue)) {
                    settingFindTextFromEvent = true;
                    setFindText(newValue);
                    settingFindTextFromEvent = false;
                }
            } else if (SearchContext.PROPERTY_REPLACE_WITH.equals(prop)) {
                String newValue = (String) e.getNewValue();
                String oldValue = getReplaceText();
                // Prevents IllegalStateExceptions
                if (!newValue.equals(oldValue)) {
                    settingFindTextFromEvent = true;
                    setReplaceText(newValue);
                    settingFindTextFromEvent = false;
                }
            } else if (SearchContext.PROPERTY_SEARCH_WRAP.equals(prop)) {
                boolean newValue = (Boolean) e.getNewValue();
                wrapCheckBox.setSelected(newValue);
            }
        }
    }

    /**
     * Listens for events in the Find (and Replace, in the subclass) search
     * field.
     */
    protected class FindFieldListener extends KeyAdapter implements DocumentListener, FocusListener {

        protected boolean selectAll;

        @Override
        public void changedUpdate(DocumentEvent e) {}

        @Override
        public void focusGained(FocusEvent e) {
            JTextField field = (JTextField) e.getComponent();
            if (selectAll) {
                field.selectAll();
            }
            selectAll = true;
        }

        @Override
        public void focusLost(FocusEvent e) {}

        protected void handleDocumentEvent(DocumentEvent e) {
            handleToggleButtons();
            if (!settingFindTextFromEvent) {
                JTextComponent findField = UIUtil.getTextComponent(findCombo);
                if (e.getDocument() == findField.getDocument()) {
                    context.setSearchFor(findField.getText());
                    if (context.getMarkAll()) {
                        doMarkAll(true);
                    }
                } else { // Replace field's document
                    JTextComponent replaceField = UIUtil.getTextComponent(replaceCombo);
                    context.setReplaceWith(replaceField.getText());
                    // Don't re-fire "mark all" events for "replace" text edits
                }
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            handleDocumentEvent(e);
        }

        public void install(JTextComponent field) {
            field.getDocument().addDocumentListener(this);
            field.addKeyListener(this);
            field.addFocusListener(this);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == '\n') {
                int mod = e.getModifiers();
                int ctrlShift = InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK;
                boolean forward = (mod & ctrlShift) == 0;
                doSearch(forward);
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            handleDocumentEvent(e);
        }
    }

    /**
     * Called when the user edits the "Find" field's contents, after a slight
     * delay.  Fires a "mark all" search event for applications that want to
     * display "mark all" results on the fly.
     */
    private class MarkAllEventNotifier implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            fireMarkAllEvent();
        }
    }
}
