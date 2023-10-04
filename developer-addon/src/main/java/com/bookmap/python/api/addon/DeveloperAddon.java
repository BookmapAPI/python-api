package com.bookmap.python.api.addon;

import com.bookmap.python.api.addon.exceptions.FailedToBuildException;
import com.bookmap.python.api.addon.services.BuildService;
import com.bookmap.python.api.addon.services.ContentFileSaver;
import com.bookmap.python.api.addon.services.DefaultBuildService;
import com.bookmap.python.api.addon.services.PythonEnvironmentValidator;
import com.bookmap.python.api.addon.services.PythonScriptSyntaxValidator;
import com.bookmap.python.api.addon.services.TextEditorFileSaver;
import com.bookmap.python.api.addon.settings.EditorTheme;
import com.bookmap.python.api.addon.settings.PythonApiSettings;
import com.bookmap.python.api.addon.ui.ExecutablesFileFilter;
import com.bookmap.python.api.addon.ui.JLabelLink;
import com.bookmap.python.api.addon.ui.custom.CustomCollapsibleSectionPanel;
import com.bookmap.python.api.addon.ui.custom.CustomFindToolBar;
import com.bookmap.python.api.addon.ui.custom.CustomReplaceToolBar;
import com.bookmap.python.api.addon.ui.filetree.JFileTree;
import com.bookmap.python.api.addon.ui.listeners.EditorSearchListener;
import com.bookmap.python.api.addon.ui.listeners.EditorStateListener;
import com.bookmap.python.api.addon.ui.listeners.EditorTextFileTrackerListener;
import com.bookmap.python.api.addon.ui.listeners.SavingTextEditorFileSelectionListener;
import com.bookmap.python.api.addon.utils.Log;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.FoldIndicatorStyle;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import velox.api.layer1.Layer1ApiFinishable;
import velox.api.layer1.Layer1ApiInstrumentSpecificEnabledStateProvider;
import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.Layer1CustomPanelsGetter;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1Attachable;
import velox.api.layer1.annotations.Layer1StrategyDateLicensed;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.common.DirectoryResolver;
import velox.api.layer1.messages.indicators.SettingsAccess;
import velox.api.layer1.settings.Layer1ConfigSettingsInterface;
import velox.gui.StrategyPanel;

@Layer1Attachable
@Layer1StrategyName("Python API")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
@Layer1StrategyDateLicensed("BM.Addons.PythonAPI")
public class DeveloperAddon
    implements
        Layer1CustomPanelsGetter,
        Layer1ApiInstrumentSpecificEnabledStateProvider,
        Layer1ApiFinishable,
        Layer1ConfigSettingsInterface {

    private static final String VERSION = "0.1.1";
    private static final String ADDON_NAME = "Python API";

    private static File ROOT_DIR = DirectoryResolver.getBookmapDirectoryByName("Python").toFile();
    public static File BUILD_DIR = ROOT_DIR.toPath().resolve("build").toFile();

    private static final int MAXIMAL_ADDON_NAME_LENGTH = 64;
    private static final ExecutablesFileFilter EXECUTABLES_ONLY_FILE_FILTER = new ExecutablesFileFilter();
    // Addon should begin from the
    private static final Pattern ADDON_NAME_PATTERN = Pattern.compile("^[a-zA-Z][\\s_\\w]*$");

    // icons for settings panel
    private final ImageIcon saveImageIcon;
    private final ImageIcon playImageIcon;
    private final ImageIcon commandImageIcon;
    private final ImageIcon pythonImageIcon;

    private JFrame fullScreeEditorWindow;

    private final Map<String, AliasState> aliasToState = new ConcurrentHashMap<>();
    private final Layer1ApiProvider provider;

    private ContentFileSaver fileSaver;

    private JLabel titleLabel;
    private JPanel introPanel;
    private RTextScrollPane textEditorScrollPanel;

    private JButton saveButton;
    private JButton buildButton;
    private JCheckBox isTradingStrategyCheckBox;
    private final AtomicBoolean isTradingStrategy = new AtomicBoolean(false);

    private JMenuItem saveMenuItem;
    private JMenuItem buildMenuItem;

    private String currentOpenFileName;
    private volatile boolean switchingFiles;
    private HashSet<String> fileNamesWithUnsavedChanges = new HashSet<>();
    private HashMap<String, String> fileNameToUnsavedText = new HashMap<>();
    private SettingsAccess settingsAccess;
    private PythonApiSettings pythonApiSettings;
    private RSyntaxTextArea textArea;
    private FindToolBar findToolBar;
    private ReplaceToolBar replaceToolBar;

    private CustomCollapsibleSectionPanel collapsibleSectionPanel;

    private SearchListener searchListener;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DeveloperAddon(Layer1ApiProvider provider) {
        this.provider = provider;

        saveImageIcon = loadIcon("save.png");
        playImageIcon = loadIcon("play.png");
        commandImageIcon = loadIcon("command.png");
        pythonImageIcon = loadIcon("python.png");
    }

    private void resetState() {
        currentOpenFileName = null;
        switchingFiles = false;
        fileNamesWithUnsavedChanges = new HashSet<>();
        fileNameToUnsavedText = new HashMap<>();
    }

    private ImageIcon loadIcon(String imageFileName) {
        byte[] commandImageBuf;
        try {
            commandImageBuf = getClass().getResourceAsStream("/" + imageFileName).readAllBytes();
        } catch (Exception e) {
            Log.error(String.format("Failed to load '%s' icon", imageFileName));
            return null;
        }
        return new ImageIcon(commandImageBuf);
    }

    @Override
    public StrategyPanel[] getCustomGuiFor(String alias, String indicatorName) {
        return getOrCreateStrategyPanels(alias);
    }

    private void showFullScreenEditor(StrategyPanel infoPanel) throws IOException {
        if (fullScreeEditorWindow != null) {
            fullScreeEditorWindow.toFront();
            return;
        }

        resetState();

        JFrame jFrame = new JFrame("Bookmap code editor");
        jFrame.setLocationRelativeTo(infoPanel);
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        jFrame.addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // On window closing, warn about any unsaved files.
                    if (fileNamesWithUnsavedChanges.size() > 0) {
                        int result = JOptionPane.showConfirmDialog(
                            jFrame,
                            "You have unsaved changes. Are you sure you want to close the editor? Your changes will be lost.\n\n" +
                            "Unsaved files: " +
                            String.join(", ", fileNamesWithUnsavedChanges),
                            "Unsaved changes",
                            JOptionPane.YES_NO_OPTION
                        );
                        if (result == JOptionPane.NO_OPTION) {
                            return;
                        }
                    }
                    jFrame.dispose();
                }
            }
        );

        fullScreeEditorWindow = jFrame;
        jFrame.addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    Log.info("Closed editor window");
                    if (fullScreeEditorWindow != null) {
                        fullScreeEditorWindow.dispose();
                        fullScreeEditorWindow = null;
                    }
                    saveSettings();
                    super.windowClosed(e);
                }
            }
        );

        if (!ROOT_DIR.exists() && !ROOT_DIR.mkdir()) {
            Log.error("Failed to init default directory with addons...");
            jFrame.add(
                new JLabel(
                    "Failed to load workspace directory, try to restart the application or contact support@bookmap.com"
                )
            );
        } else {
            initEditorFrame(jFrame, new NewFileAction(), ROOT_DIR);
        }
        jFrame.revalidate();
        jFrame.pack();
        SwingUtilities.invokeLater(() -> jFrame.setVisible(true));

        copyExamplePythonFilesIfEmptyDir();
    }

    private void copyExamplePythonFilesIfEmptyDir() {
        try {
            int numPythonFiles = ROOT_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".py")).length;
            if (numPythonFiles == 0) {
                copyExamplePythonFile("cvd_addon.py");
                copyExamplePythonFile("hello_world.py");
                copyExamplePythonFile("liquidity_tracker.py");
                copyExamplePythonFile("mbo_test.py");
                copyExamplePythonFile("order_book_test.py");
                copyExamplePythonFile("simple_market_maker.py");
            }
        } catch (Exception e) {
            Log.error("Failed to copy example Python files.", e);
        }
    }

    private void copyExamplePythonFile(String fileName) {
        try (InputStream input = getClass().getResourceAsStream("/" + fileName)) {
            Files.copy(input, ROOT_DIR.toPath().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            Log.error(String.format("Failed to an example Python file '%s'", fileName));
        }
    }

    private void initEditorFrame(JFrame jFrame, ActionListener newFileAction, File rootDir) throws IOException {
        /*
         * Initial setup.
         * TODO: Organize the below code.
         */

        var minFrameSize = new Dimension(1000, 600);
        jFrame.setMinimumSize(minFrameSize);

        var jFileTree = new JFileTree(
            rootDir,
            newFileAction,
            deletedFilePath -> {
                // Close the code editor if the file we have open gets deleted.
                if (deletedFilePath.getFileName().toString().equals(currentOpenFileName)) {
                    openFile(null);
                }
            }
        );
        jFileTree.setMinimumSize(new Dimension(200, 600));
        textArea = createTextArea();
        textArea.setTabSize(4);
        textArea.setTabsEmulated(true);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        textArea.setCodeFoldingEnabled(pythonApiSettings.isCodeFoldingEnabled());
        textArea.setMarkOccurrences(pythonApiSettings.isMarkOccurrencesEnabled());
        textArea.setAntiAliasingEnabled(pythonApiSettings.isAntiAliasingEnabled());
        textArea.setLineWrap(pythonApiSettings.isWordWrapEnabled());
        textArea.setHighlightCurrentLine(pythonApiSettings.isLineHighlightEnabled());
        textArea.setAnimateBracketMatching(pythonApiSettings.isBracketMatchingAnimationEnabled());
        textArea.setPaintTabLines(pythonApiSettings.isTabLinesEnabled());

        initSearchDialogs();

        loadNonDefaultTheme();

        titleLabel = new JLabel("");
        // Set the title label to bold font.
        titleLabel.setFont(new Font(titleLabel.getFont().getFontName(), Font.BOLD, titleLabel.getFont().getSize()));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        introPanel = new JPanel();
        textEditorScrollPanel = new RTextScrollPane(textArea);
        textEditorScrollPanel.getGutter().setFoldIndicatorStyle(FoldIndicatorStyle.MODERN);
        textEditorScrollPanel.setLineNumbersEnabled(pythonApiSettings.isLineNumbersEnabled());
        textEditorScrollPanel.setIconRowHeaderEnabled(pythonApiSettings.isBookmarksEnabled());

        var editorStateListener = new EditorStateListener();
        textArea.getDocument().addDocumentListener(editorStateListener);
        fileSaver =
            new TextEditorFileSaver(textArea, editorStateListener, fileNameToUnsavedText, fileNamesWithUnsavedChanges);
        var fileSelectionListener = new SavingTextEditorFileSelectionListener(
            editorStateListener,
            fileSaver,
            textEditorScrollPanel,
            this::openFile,
            fileNameToUnsavedText
        );
        jFileTree.addFileTreeSelectionListener(fileSelectionListener);
        jFileTree.addFileTrackerListener(
            new EditorTextFileTrackerListener(
                editorStateListener,
                fileSelectionListener,
                fileSaver,
                textArea,
                fileNamesWithUnsavedChanges
            )
        );
        var rightComponent = new JPanel();
        rightComponent.setMinimumSize(new Dimension(500, 600));
        rightComponent.setLayout(new GridBagLayout());

        textArea
            .getDocument()
            .addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        if (!switchingFiles) {
                            boolean added = fileNamesWithUnsavedChanges.add(currentOpenFileName);
                            if (added) {
                                updateTitleLabel();
                            }
                        } else {
                            switchingFiles = false;
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        if (!switchingFiles) {
                            boolean added = fileNamesWithUnsavedChanges.add(currentOpenFileName);
                            if (added) {
                                updateTitleLabel();
                            }
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {}
                }
            );

        /*
         * Action listeners.
         */

        ActionListener saveAction = ev -> {
            File selectedFile = fileSelectionListener.getSelectedFile();
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(jFrame, "No file selected");
                return;
            }
            try {
                fileSaver.save(selectedFile);
            } catch (IOException e) {
                Log.error("Failed to save file", e);
                JOptionPane.showMessageDialog(jFrame, "Failed to save file");
                return;
            }

            Log.info("Saved file: " + selectedFile);

            updateTitleLabel();
        };

        ActionListener selectRuntimeAction = ev -> {
            var fileChooser = new JFileChooser();
            fileChooser.setFileFilter(EXECUTABLES_ONLY_FILE_FILTER);
            int resp = fileChooser.showDialog(jFrame, "Select");
            if (resp == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                var runtimeValidator = new PythonEnvironmentValidator();
                var validationFuture = runtimeValidator.validate(selectedFile.getAbsolutePath());
                String error;
                try {
                    error = validationFuture.get(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.warn("Validation interrupted");
                    return;
                } catch (ExecutionException | TimeoutException e) {
                    Log.error("Failed to check python runtime", e);
                    JOptionPane.showMessageDialog(
                        jFrame,
                        "Can't validate Python version. Is runtime selected correctly?",
                        "Failed to select runtime",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(jFrame, error, "Wrong runtime", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                pythonApiSettings.setPathToPythonExecutable(selectedFile.getAbsolutePath());

                JOptionPane.showMessageDialog(
                    jFrame,
                    String.format("Runtime '%s' selected successfully", selectedFile),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        };

        collapsibleSectionPanel = new CustomCollapsibleSectionPanel(textArea);
        var collapsibleSectionPanelConstrains = new GridBagConstraints();
        collapsibleSectionPanelConstrains.anchor = GridBagConstraints.FIRST_LINE_START;
        collapsibleSectionPanelConstrains.fill = GridBagConstraints.HORIZONTAL;
        collapsibleSectionPanelConstrains.gridy = 1;
        collapsibleSectionPanelConstrains.gridx = 0;
        collapsibleSectionPanelConstrains.gridwidth = 4;
        collapsibleSectionPanelConstrains.ipady = 0;
        collapsibleSectionPanelConstrains.weightx = 0;
        collapsibleSectionPanelConstrains.insets = new Insets(5, 5, 5, 5);
        rightComponent.add(collapsibleSectionPanel, collapsibleSectionPanelConstrains);
        collapsibleSectionPanel.add(textEditorScrollPanel);
        collapsibleSectionPanel.addBottomComponent(findToolBar);
        collapsibleSectionPanel.addBottomComponent(replaceToolBar);

        /*
         * Buttons.
         */

        saveButton = new JButton("Save", saveImageIcon);
        saveButton.setEnabled(false);
        saveButton.setToolTipText("Save code");
        var saveButtonConstraints = new GridBagConstraints();
        saveButtonConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        saveButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        saveButtonConstraints.gridy = 0;
        saveButtonConstraints.gridx = 0;
        saveButtonConstraints.ipady = 0;
        saveButtonConstraints.weightx = 0;
        saveButtonConstraints.insets = new Insets(5, 5, 5, 5);
        saveButton.addActionListener(saveAction);
        rightComponent.add(saveButton, saveButtonConstraints);

        buildButton = new JButton("Build", playImageIcon);
        BuildAction buildAction = new BuildAction(fileSelectionListener, jFrame);
        buildButton.setEnabled(false);
        buildButton.setToolTipText("Build addon");
        var buildButtonConstraints = new GridBagConstraints();
        buildButtonConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        buildButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buildButtonConstraints.gridy = 0;
        buildButtonConstraints.gridx = 1;
        buildButtonConstraints.ipady = 0;
        buildButtonConstraints.weightx = 0;
        buildButtonConstraints.insets = new Insets(5, 5, 5, 5);
        buildButton.addActionListener(buildAction);
        rightComponent.add(buildButton, buildButtonConstraints);

        isTradingStrategyCheckBox = new JCheckBox("Is trading strategy");
        isTradingStrategyCheckBox.setEnabled(false);
        // TODO: create extended JCheckBox with method isTradingStrategy() to avoid creating independent variable
        isTradingStrategyCheckBox.setSelected(isTradingStrategy.get());
        isTradingStrategyCheckBox.setToolTipText(
            "If your addon use any trading related functions, then check this checkbox"
        );
        var isTradingStrategyConstraints = new GridBagConstraints();
        isTradingStrategyConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        isTradingStrategyConstraints.fill = GridBagConstraints.VERTICAL;
        isTradingStrategyConstraints.gridy = 0;
        isTradingStrategyConstraints.gridx = 2;
        isTradingStrategyConstraints.ipady = 0;
        isTradingStrategyConstraints.weightx = 0;
        isTradingStrategyConstraints.insets = new Insets(5, 5, 5, 5);
        isTradingStrategyCheckBox.addActionListener(actionEvent -> {
            boolean current = isTradingStrategy.get();
            isTradingStrategy.set(!current);
        });
        rightComponent.add(isTradingStrategyCheckBox, isTradingStrategyConstraints);
        var selectRuntimeButton = new JButton("Set custom runtime", pythonImageIcon);
        selectRuntimeButton.setToolTipText(
            "Select Python runtime to use for addon execution. Uses `python3` by default."
        );
        var selectFileRuntimeConstraints = new GridBagConstraints();
        selectFileRuntimeConstraints.anchor = GridBagConstraints.LAST_LINE_START;
        selectFileRuntimeConstraints.gridy = 0;
        selectFileRuntimeConstraints.gridx = 3;
        selectFileRuntimeConstraints.ipady = 0;
        selectFileRuntimeConstraints.weightx = 0;
        selectFileRuntimeConstraints.insets = new Insets(5, 5, 5, 5);
        rightComponent.add(selectRuntimeButton, selectFileRuntimeConstraints);
        if (pythonApiSettings.getPathToPythonExecutable() != null) {
            selectRuntimeButton.setText(pythonApiSettings.getPathToPythonExecutable());
        }
        selectRuntimeButton.addActionListener(selectRuntimeAction);

        /*
         * Title.
         */

        var titleConstrains = new GridBagConstraints();
        titleConstrains.gridy = 1;
        titleConstrains.gridx = 0;
        titleConstrains.gridwidth = 4;
        titleConstrains.anchor = GridBagConstraints.NORTHWEST;
        titleConstrains.weightx = 1.0;
        titleConstrains.weighty = 0.0;
        rightComponent.add(titleLabel, titleConstrains);

        textEditorScrollPanel.setVisible(false);
        var textEditorConstrains = new GridBagConstraints();
        textEditorConstrains.gridy = 2;
        textEditorConstrains.gridx = 0;
        textEditorConstrains.gridwidth = 4;
        textEditorConstrains.fill = GridBagConstraints.BOTH;
        textEditorConstrains.weightx = 1.0;
        textEditorConstrains.weighty = 1.0;
        rightComponent.add(textEditorScrollPanel, textEditorConstrains);

        /*
         * Intro panel / code editor.
         */

        introPanel.setVisible(true);
        introPanel.setLayout(new BoxLayout(introPanel, BoxLayout.Y_AXIS));

        var introTitleLabel = new JLabel("Bookmap Python API (ALPHA)");
        introTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        introTitleLabel.setFont(new Font(introTitleLabel.getFont().getName(), Font.BOLD, 25));
        var introSubtitleLabel = new JLabel("Easily write custom indicators for Bookmap");
        introSubtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        introSubtitleLabel.setFont(new Font(introTitleLabel.getFont().getName(), Font.PLAIN, 20));
        var introTextLabel = new JLabel("Select a file in the menu to start editing.");
        introTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        introPanel.add(Box.createVerticalGlue());
        introPanel.add(Box.createHorizontalGlue());
        introPanel.add(introTitleLabel);
        introPanel.add(Box.createRigidArea(new Dimension(0, 10))); // adds a 5-pixel vertical gap
        introPanel.add(introSubtitleLabel);
        introPanel.add(Box.createRigidArea(new Dimension(0, 25))); // adds a 5-pixel vertical gap
        introPanel.add(introTextLabel);
        introPanel.add(Box.createHorizontalGlue());
        introPanel.add(Box.createVerticalGlue());

        rightComponent.add(introPanel, textEditorConstrains);

        /*
         * Menu.
         */

        var menuBar = new JMenuBar();

        var fileMenu = new JMenu("File");

        var newMenuItem = new JMenuItem("New Python File..");
        newMenuItem.addActionListener(new NewFileAction());
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(saveAction);
        buildMenuItem = new JMenuItem(buildAction);
        buildMenuItem.setEnabled(false);
        var openBuildFolderMenuItem = new JMenuItem("Open build folder");
        openBuildFolderMenuItem.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(rootDir.toPath().resolve("build").toFile());
            } catch (IOException ex) {
                Log.error("Failed to open build folder.", ex);
            }
        });
        var selectRuntimeMenuItem = new JMenuItem("Select Python runtime...");
        selectRuntimeMenuItem.addActionListener(selectRuntimeAction);
        var exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING)));

        fileMenu.add(newMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(buildMenuItem);
        fileMenu.add(openBuildFolderMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(selectRuntimeMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        // Source: https://github.com/bobbylight/RSyntaxTextArea/wiki/Example:-Adding-an-Edit-menu-for-the-editor
        var editMenu = new JMenu("Edit");
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.UNDO_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.REDO_ACTION)));
        editMenu.addSeparator();
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.CUT_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.COPY_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.PASTE_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.DELETE_ACTION)));
        editMenu.addSeparator();
        editMenu.add(new ShowFindToolBarAction(findToolBar));
        editMenu.add(new ShowReplaceToolBarAction(replaceToolBar));
        editMenu.addSeparator();
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.SELECT_ALL_ACTION)));
        menuBar.add(editMenu);

        // Source of the actions below:
        // https://github.com/bobbylight/RSyntaxTextArea/blob/master/RSyntaxTextAreaDemo/src/main/java/org/fife/ui/rsyntaxtextarea/demo/DemoRootPane.java
        var viewMenu = new JMenu("View");
        JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(new CodeFoldingAction());
        cbItem.setSelected(pythonApiSettings.isCodeFoldingEnabled());
        viewMenu.add(cbItem);
        cbItem = new JCheckBoxMenuItem(new ViewLineHighlightAction());
        cbItem.setSelected(pythonApiSettings.isLineHighlightEnabled());
        viewMenu.add(cbItem);
        cbItem = new JCheckBoxMenuItem(new ViewLineNumbersAction());
        cbItem.setSelected(pythonApiSettings.isLineNumbersEnabled());
        viewMenu.add(cbItem);
        cbItem = new JCheckBoxMenuItem(new AnimateBracketMatchingAction());
        cbItem.setSelected(pythonApiSettings.isBracketMatchingAnimationEnabled());
        viewMenu.add(cbItem);
        cbItem = new JCheckBoxMenuItem(new BookmarksAction());
        cbItem.setSelected(pythonApiSettings.isBookmarksEnabled());
        viewMenu.add(cbItem);
        cbItem = new JCheckBoxMenuItem(new WordWrapAction());
        cbItem.setSelected(pythonApiSettings.isWordWrapEnabled());
        viewMenu.add(cbItem);
        cbItem = new JCheckBoxMenuItem(new ToggleAntiAliasingAction());
        cbItem.setSelected(pythonApiSettings.isAntiAliasingEnabled());
        viewMenu.add(cbItem);
        cbItem = new JCheckBoxMenuItem(new MarkOccurrencesAction());
        cbItem.setSelected(pythonApiSettings.isMarkOccurrencesEnabled());
        viewMenu.add(cbItem);
        cbItem = new JCheckBoxMenuItem(new TabLinesAction());
        cbItem.setSelected(pythonApiSettings.isTabLinesEnabled());
        viewMenu.add(cbItem);
        menuBar.add(viewMenu);

        var themesButtonGroup = new ButtonGroup();
        var themesMenu = new JMenu("Themes");
        addThemeItem(
            EditorTheme.LIGHT.getName(),
            EditorTheme.LIGHT.getXmlName(),
            themesButtonGroup,
            themesMenu,
            isThemeChosen(EditorTheme.LIGHT.getName())
        );
        addThemeItem(
            EditorTheme.LIGHT_SYSTEM_SELECTION.getName(),
            EditorTheme.LIGHT_SYSTEM_SELECTION.getXmlName(),
            themesButtonGroup,
            themesMenu,
            isThemeChosen(EditorTheme.LIGHT_SYSTEM_SELECTION.getName())
        );
        addThemeItem(
            EditorTheme.DARK.getName(),
            EditorTheme.DARK.getXmlName(),
            themesButtonGroup,
            themesMenu,
            isThemeChosen(EditorTheme.DARK.getName())
        );
        addThemeItem(
            EditorTheme.DRUID.getName(),
            EditorTheme.DRUID.getXmlName(),
            themesButtonGroup,
            themesMenu,
            isThemeChosen(EditorTheme.DRUID.getName())
        );
        addThemeItem(
            EditorTheme.MONOKAI.getName(),
            EditorTheme.MONOKAI.getXmlName(),
            themesButtonGroup,
            themesMenu,
            isThemeChosen(EditorTheme.MONOKAI.getName())
        );
        addThemeItem(
            EditorTheme.ECLIPSE.getName(),
            EditorTheme.ECLIPSE.getXmlName(),
            themesButtonGroup,
            themesMenu,
            isThemeChosen(EditorTheme.ECLIPSE.getName())
        );
        addThemeItem(
            EditorTheme.IDEA.getName(),
            EditorTheme.IDEA.getXmlName(),
            themesButtonGroup,
            themesMenu,
            isThemeChosen(EditorTheme.IDEA.getName())
        );
        addThemeItem(
            EditorTheme.VISUAL_STUDIO.getName(),
            EditorTheme.VISUAL_STUDIO.getXmlName(),
            themesButtonGroup,
            themesMenu,
            isThemeChosen(EditorTheme.VISUAL_STUDIO.getName())
        );
        menuBar.add(themesMenu);

        var helpMenu = new JMenu("Help");

        var welcomeMenuItem = new JMenuItem("Welcome");
        welcomeMenuItem.addActionListener(e -> openFile(null));
        helpMenu.add(welcomeMenuItem);

        var openGuideMenuItem = new JMenuItem("Open Quick Guide");
        openGuideMenuItem.addActionListener(e -> {
            try {
                Desktop
                    .getDesktop()
                    .browse(new URI("https://docs.google.com/document/d/178YRno3iKKdbuvVjVh380ayR-VsSUlQGZt2tDFjjD3A"));
            } catch (Exception e2) {
                Log.error("Failed to open browser.", e2);
            }
        });
        helpMenu.add(openGuideMenuItem);

        var openGithubMenuItem = new JMenuItem("Open GitHub repository");
        openGithubMenuItem.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/BookmapAPI/python-api"));
            } catch (Exception e2) {
                Log.error("Failed to open browser.", e2);
            }
        });
        helpMenu.add(openGithubMenuItem);

        var aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                jFrame,
                "<html><b>Python API</b> - Write your custom Bookmap addons using Python" + "<br>Version " + VERSION,
                "About",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        jFrame.setJMenuBar(menuBar);

        /*
         * Keyboard shortcuts.
         */

        textArea.addKeyListener(
            new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                        saveAction.actionPerformed(null);
                    }
                }
            }
        );

        /*
         * Set up frame.
         */

        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jFileTree, rightComponent);
        splitPane.setMinimumSize(minFrameSize);
        splitPane.setOneTouchExpandable(true);
        jFrame.add(splitPane, BorderLayout.CENTER);
        jFrame.addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    try {
                        jFileTree.close();
                    } catch (IOException ex) {
                        Log.warn("Failed to clean JFileTree resources", ex);
                    }
                }
            }
        );

        jFrame.pack();
    }

    /**
     * Creates our Find and Replace toolbars.
     */
    private void initSearchDialogs() {
        searchListener = new EditorSearchListener(textArea);

        // Create toolbars and tie their search contexts together also.
        findToolBar = new CustomFindToolBar(searchListener);
        replaceToolBar = new CustomReplaceToolBar(searchListener);

        // This ties the properties of the two toolbars together (match case,
        // regex, etc.).
        SearchContext context = findToolBar.getSearchContext();
        replaceToolBar.setSearchContext(context);
    }

    private JMenuItem createMenuItem(Action action) {
        JMenuItem item = new JMenuItem(action);
        item.setToolTipText(null); // Swing annoyingly adds tool tip text to the menu item.
        return item;
    }

    private boolean isThemeChosen(String themeName) {
        return pythonApiSettings.getChosenTheme().equals(themeName);
    }

    private void addThemeItem(String name, String themeXml, ButtonGroup bg, JMenu menu, boolean selected) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(new ThemeAction(name, themeXml));
        item.setSelected(selected);
        bg.add(item);
        menu.add(item);
    }

    /**
     * A list of all provided themes is here:
     * https://github.com/bobbylight/RSyntaxTextArea/tree/master/RSyntaxTextArea/src/main/resources/org/fife/ui/rsyntaxtextarea/themes
     */
    private void loadNonDefaultTheme() {
        try {
            String pathToXml =
                "/org/fife/ui/rsyntaxtextarea/themes/" +
                EditorTheme.fromName(pythonApiSettings.getChosenTheme()).getXmlName();
            Theme theme = Theme.load(getClass().getResourceAsStream(pathToXml));
            theme.apply(textArea);
        } catch (IOException ioe) {
            Log.error("Failed to load theme", ioe);
        }
    }

    private void openFile(String fileName) {
        if (currentOpenFileName != null) {
            if (fileNamesWithUnsavedChanges.contains(currentOpenFileName)) {
                fileNameToUnsavedText.put(currentOpenFileName, textArea.getText());
            }
        }

        switchingFiles = true;

        currentOpenFileName = fileName;

        updateTitleLabel();

        if (fileName != null) {
            textEditorScrollPanel.setVisible(true);
            introPanel.setVisible(false);

            saveButton.setEnabled(true);
            buildButton.setEnabled(true);
            isTradingStrategyCheckBox.setEnabled(true);

            saveMenuItem.setEnabled(true);
            buildMenuItem.setEnabled(true);
        } else {
            textEditorScrollPanel.setVisible(false);
            introPanel.setVisible(true);

            saveButton.setEnabled(false);
            buildButton.setEnabled(false);

            saveMenuItem.setEnabled(false);
            buildMenuItem.setEnabled(false);
        }

        // Reset all edits after switching a file.
        // I call this in a couple of places in the code base. I'm not sure if all are actually needed.
        // Source: https://github.com/bobbylight/RSyntaxTextArea/issues/21
        // TODO: Unfortunately this resets the Undo stack entirely, so after switching to another file and back, you
        //       cannot undo any more. Can this be somehow fixed?
        textArea.discardAllEdits();
    }

    private void updateTitleLabel() {
        if (currentOpenFileName != null) {
            String starMaybe = fileNamesWithUnsavedChanges.contains(currentOpenFileName) ? "*" : "";
            titleLabel.setText(currentOpenFileName + starMaybe);
        } else {
            titleLabel.setText("");
        }
    }

    private StrategyPanel[] getOrCreateStrategyPanels(String alias) {
        var state = aliasToState.compute(
            alias,
            (al, st) -> {
                AliasState usingAliasState = st == null ? new AliasState() : st;
                if (usingAliasState.panels != null) {
                    Arrays.stream(usingAliasState.panels).forEach(p -> p.setEnabled(usingAliasState.isEnabled));
                    return usingAliasState;
                }
                var infoPanel = new StrategyPanel("Python API", false);
                var infoPanelLayout = new BoxLayout(infoPanel, BoxLayout.Y_AXIS);
                infoPanel.setLayout(infoPanelLayout);

                infoPanel.add(
                    new JLabel(
                        "<html>The Python API is in the early access alpha stage. It's not meant for production usage. " +
                        "Please report any issues or request features via our #python-api Discord channel.</html>"
                    )
                );

                infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

                infoPanel.add(
                    new JLabelLink(
                        "Please check our ",
                        "quick guide",
                        " to see how to use the Python API.",
                        "https://docs.google.com/document/d/178YRno3iKKdbuvVjVh380ayR-VsSUlQGZt2tDFjjD3A/edit#heading=h.sixk3ljpq3jv"
                    )
                );

                infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

                infoPanel.add(
                    new JLabelLink(
                        "Also check our ",
                        "GitHub repository",
                        " to find examples and API reference.",
                        "https://github.com/BookmapAPI/python-api"
                    )
                );

                infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

                var openEditorButton = new JButton("Open embedded editor", commandImageIcon);
                openEditorButton.addActionListener(e -> {
                    try {
                        showFullScreenEditor(infoPanel);
                    } catch (IOException ex) {
                        Log.error("Failed to show screen editor");
                    }
                });
                infoPanel.add(openEditorButton);

                infoPanel.setEnabled(usingAliasState.isEnabled);
                var configJPanel = new JPanel();
                configJPanel.setLayout(new GridBagLayout());
                usingAliasState.panels = new StrategyPanel[] { infoPanel };
                return usingAliasState;
            }
        );
        return state.panels;
    }

    private void buildAddon(Path script) throws FailedToBuildException {
        Log.info("Building " + script.toFile().getAbsolutePath());

        String addonName = script.getFileName().toString().replace(".py", "");
        String tcpPort = "any";
        String runtimePath = pythonApiSettings.getPathToPythonExecutable();

        if (runtimePath == null) {
            runtimePath = "python3";
        }

        Log.info("Build parameters; Name = " + addonName + ", tcp port = " + tcpPort + ", runtime = " + runtimePath);
        var pythonVersionValidator = new PythonEnvironmentValidator();
        try {
            Future<String> versionValidationFuture = pythonVersionValidator.validate(runtimePath);
            String respMsg = versionValidationFuture.get(5, TimeUnit.SECONDS);
            if (respMsg != null) {
                Log.error("Runtime does not look suitable, error " + respMsg);
                throw new FailedToBuildException("The Python runtime doesn't look suitable.\nError: " + respMsg);
            }
        } catch (ExecutionException | TimeoutException e) {
            Log.error("Failed to validated version ", e);
            throw new FailedToBuildException("Failed to validate the Python version. " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Log.warn("Build has been interrupted", e);
            throw new FailedToBuildException("Build has been interrupted", e);
        }

        var pythonScriptValidator = new PythonScriptSyntaxValidator(runtimePath);
        Future<String> validationTask = pythonScriptValidator.validate(script);
        try {
            String error = validationTask.get(10, TimeUnit.SECONDS);
            if (error != null) {
                throw new FailedToBuildException("Failed to build the script.\n" + error);
            }
        } catch (InterruptedException e) {
            Log.warn("Validation interrupted");
        } catch (TimeoutException | ExecutionException e) {
            Log.error("Error validating script", e);
            throw new FailedToBuildException("Failed to validate the script.", e);
        }

        BuildService<Path> buildService = new DefaultBuildService(tcpPort, runtimePath, isTradingStrategy.get());
        buildService.build(addonName, script);
    }

    private boolean testAddonName(String addonName) {
        if (addonName.length() > MAXIMAL_ADDON_NAME_LENGTH) {
            Log.warn("Entered addon name " + addonName);
            return false;
        }
        if (!addonName.matches(ADDON_NAME_PATTERN.pattern())) {
            Log.warn("Entered addon name " + addonName);
            return false;
        }

        return true;
    }

    private boolean testTcpPort(String tcpPort) {
        try {
            if (tcpPort.isBlank()) {
                Log.warn("Entered tcp port " + tcpPort);
                return false;
            }
            int port = Integer.parseInt(tcpPort);
            if (port < 0 || port > 65_535) {
                Log.warn("Entered tcp port " + port);
                return false;
            }
        } catch (NumberFormatException formatException) {
            Log.warn("Entered port " + tcpPort);
            return false;
        }

        return true;
    }

    private boolean testFile(File file) {
        return file != null && file.exists();
    }

    @Override
    public void finish() {
        Log.info("Developer addon is finished");
        if (fullScreeEditorWindow != null) {
            fullScreeEditorWindow.dispose();
            fullScreeEditorWindow = null;
        }
        Log.info("Shutdown executorService");
        executorService.shutdownNow();
        saveSettings();
    }

    private void triggerReloadOfPanels(String alias) {
        for (var panel : aliasToState.get(alias).panels) {
            panel.requestReload();
        }
    }

    @Override
    public void onStrategyCheckboxEnabled(String alias, boolean isEnabled) {
        aliasToState.compute(
            alias,
            (al, st) -> {
                AliasState aliasState = (st == null ? new AliasState() : st);
                aliasState.isEnabled = isEnabled;
                // onStrategyCheckboxEnabled might be called before the core requests GUI, thus if we want need to change panels
                // state on this callback, we need to create them first.
                aliasState.panels = getOrCreateStrategyPanels(alias);
                return aliasState;
            }
        );
        triggerReloadOfPanels(alias);
        if (!isEnabled) {
            if (fullScreeEditorWindow != null) {
                fullScreeEditorWindow.dispose();
            }
        }
    }

    @Override
    public boolean isStrategyEnabled(String alias) {
        return aliasToState.compute(
            alias,
            (al, st) -> {
                AliasState aliasState = (st == null ? new AliasState() : st);
                return aliasState;
            }
        )
            .isEnabled;
    }

    // This is a crunchy fix for a bug in RSyntaxTextArea. In short, it feels pretty badly being loaded by
    // multiple class loaders. As a result, once you reloaded this library via different class loader, you lose
    // possibility to type anything on in text are.
    //
    // Fix is taken from https://github.com/bobbylight/RSyntaxTextArea/issues/269, there you can find more details about
    // the issue as well.
    private RSyntaxTextArea createTextArea() {
        JTextComponent.removeKeymap("RTextAreaKeymap");
        var textArea = new RSyntaxTextArea(40, 1); // keep column to 1, so panel can be resized properly
        UIManager.put("RSyntaxTextAreaUI.actionMap", null);
        UIManager.put("RSyntaxTextAreaUI.inputMap", null);
        UIManager.put("RTextAreaUI.actionMap", null);
        UIManager.put("RTextAreaUI.inputMap", null);

        return textArea;
    }

    @Override
    public void acceptSettingsInterface(SettingsAccess settingsAccess) {
        this.settingsAccess = settingsAccess;
        this.pythonApiSettings =
            (PythonApiSettings) settingsAccess.getSettings(null, ADDON_NAME, PythonApiSettings.class);
    }

    private void saveSettings() {
        if (pythonApiSettings != null) {
            settingsAccess.setSettings(null, ADDON_NAME, pythonApiSettings, PythonApiSettings.class);
        }
    }

    private static class AliasState {

        public boolean isEnabled;
        public StrategyPanel[] panels;
    }

    // TODO: These actions below (and probably other GUI items) should probably moved to some other class.

    private class NewFileAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Log.info("Clicked on 'New Python file...'");
            String usersEnteredFilename = JOptionPane.showInputDialog(
                null,
                "New Python file:",
                "New file",
                JOptionPane.INFORMATION_MESSAGE
            );
            if (usersEnteredFilename == null || usersEnteredFilename.isBlank()) {
                Log.info("User did not enter file name");
                return;
            }

            Log.info("User entered file " + usersEnteredFilename);
            usersEnteredFilename = usersEnteredFilename.trim();
            usersEnteredFilename = usersEnteredFilename.replaceAll("\\s", "_");

            if (!usersEnteredFilename.endsWith(".py")) {
                usersEnteredFilename += ".py";
                Log.info("Appended .py, the file name is " + usersEnteredFilename);
            }

            try {
                Files.createFile(ROOT_DIR.toPath().resolve(usersEnteredFilename));
            } catch (FileAlreadyExistsException ex) {
                Log.warn("Failed to create file because it exists", ex);
                JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "File already exists",
                    JOptionPane.WARNING_MESSAGE
                );
            } catch (IOException ex) {
                Log.error("Failed to create a file", ex);
                JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Failed to create file",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Toggles whether matched brackets are animated.
     */
    private class AnimateBracketMatchingAction extends AbstractAction {

        AnimateBracketMatchingAction() {
            putValue(NAME, "Animate Bracket Matching");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setAnimateBracketMatching(!textArea.getAnimateBracketMatching());
            pythonApiSettings.setBracketMatchingAnimationEnabled(textArea.getAnimateBracketMatching());
            saveSettings();
        }
    }

    /**
     * Toggles whether bookmarks are enabled.
     */
    private class BookmarksAction extends AbstractAction {

        BookmarksAction() {
            putValue(NAME, "Bookmarks");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textEditorScrollPanel.setIconRowHeaderEnabled(!textEditorScrollPanel.isIconRowHeaderEnabled());
            pythonApiSettings.setBookmarksEnabled(textEditorScrollPanel.isIconRowHeaderEnabled());
            saveSettings();
        }
    }

    /**
     * Changes the syntax style to a new value.
     */
    private class ChangeSyntaxStyleAction extends AbstractAction {

        private String res;
        private String style;

        ChangeSyntaxStyleAction(String name, String res, String style) {
            putValue(NAME, name);
            this.res = res;
            this.style = style;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //            setText(res);
            textArea.setCaretPosition(0);
            textArea.setSyntaxEditingStyle(style);
        }
    }

    /**
     * Toggles whether code folding is enabled.
     */
    private class CodeFoldingAction extends AbstractAction {

        CodeFoldingAction() {
            putValue(NAME, "Code Folding");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setCodeFoldingEnabled(!textArea.isCodeFoldingEnabled());
            pythonApiSettings.setCodeFoldingEnabled(textArea.isCodeFoldingEnabled());
            saveSettings();
        }
    }

    /**
     * Toggles whether "mark occurrences" is enabled.
     */
    private class MarkOccurrencesAction extends AbstractAction {

        MarkOccurrencesAction() {
            putValue(NAME, "Mark Occurrences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setMarkOccurrences(!textArea.getMarkOccurrences());
            pythonApiSettings.setMarkOccurrencesEnabled(textArea.getMarkOccurrences());
            saveSettings();
        }
    }

    /**
     * Toggles whether "tab lines" are enabled.
     */
    private class TabLinesAction extends AbstractAction {

        TabLinesAction() {
            putValue(NAME, "Tab Lines");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setPaintTabLines(!textArea.getPaintTabLines());
            pythonApiSettings.setTabLinesEnabled(textArea.getPaintTabLines());
            saveSettings();
        }
    }

    /**
     * Changes the theme.
     */
    private class ThemeAction extends AbstractAction {

        private final String xml;

        ThemeAction(String name, String xml) {
            putValue(NAME, name);
            this.xml = xml;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            InputStream in = getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/" + xml);
            Log.debug("Theme changed to " + e.getActionCommand());
            pythonApiSettings.setChosenTheme(e.getActionCommand());
            saveSettings();
            try {
                Theme theme = Theme.load(in);
                theme.apply(textArea);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Toggles anti-aliasing.
     */
    private class ToggleAntiAliasingAction extends AbstractAction {

        ToggleAntiAliasingAction() {
            putValue(NAME, "Anti-Aliasing");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setAntiAliasingEnabled(!textArea.getAntiAliasingEnabled());
            pythonApiSettings.setAntiAliasingEnabled(textArea.getAntiAliasingEnabled());
            saveSettings();
        }
    }

    /**
     * Toggles whether the current line is highlighted.
     */
    private class ViewLineHighlightAction extends AbstractAction {

        ViewLineHighlightAction() {
            putValue(NAME, "Current Line Highlight");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setHighlightCurrentLine(!textArea.getHighlightCurrentLine());
            pythonApiSettings.setLineHighlightEnabled(textArea.getHighlightCurrentLine());
            saveSettings();
        }
    }

    /**
     * Toggles line number visibility.
     */
    private class ViewLineNumbersAction extends AbstractAction {

        ViewLineNumbersAction() {
            putValue(NAME, "Line Numbers");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textEditorScrollPanel.setLineNumbersEnabled(!textEditorScrollPanel.getLineNumbersEnabled());
            pythonApiSettings.setLineNumbersEnabled(textEditorScrollPanel.getLineNumbersEnabled());
            saveSettings();
        }
    }

    /**
     * Toggles word wrap.
     */
    private class WordWrapAction extends AbstractAction {

        WordWrapAction() {
            putValue(NAME, "Word Wrap");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setLineWrap(!textArea.getLineWrap());
            pythonApiSettings.setWordWrapEnabled(textArea.getLineWrap());
            saveSettings();
        }
    }

    /**
     * Builds an addon
     */
    private class BuildAction extends AbstractAction {

        SavingTextEditorFileSelectionListener fileSelectionListener;
        JFrame jFrame;

        BuildAction(SavingTextEditorFileSelectionListener fileSelectionListener, JFrame jFrame) {
            super("Build addon");
            this.fileSelectionListener = fileSelectionListener;
            this.jFrame = jFrame;
            int c = InputEvent.CTRL_DOWN_MASK;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, c));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File selectedFile = fileSelectionListener.getSelectedFile();
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(jFrame, "No file selected");
                return;
            }

            if (!selectedFile.getName().endsWith(".py")) {
                JOptionPane.showMessageDialog(jFrame, "The file name must end with .py");
                return;
            }

            try {
                fileSaver.save(selectedFile);
            } catch (IOException ex) {
                Log.error("Failed to save file", ex);
                JOptionPane.showMessageDialog(jFrame, "Failed to save file");
                return;
            }

            executorService.execute(() -> {
                try {
                    buildAddon(selectedFile.toPath());
                } catch (Exception ex) {
                    Log.error("Failed to build addon.", ex);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            jFrame,
                            String.format("Failed to build addon:\n%s", ex.getMessage())
                        );
                    });
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        jFrame,
                        "Build success.\n\n" +
                        "You can find your addon JAR file by opening 'File' -> 'Open build folder' here in the code editor.\n\n" +
                        "To load your addon, open the main Bookmap window, go under 'Settings' -> 'Configure addons' and add your addon JAR file.\n",
                        "Build",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            });
        }
    }

    /**
     * Shows the Find toolbar.
     */
    private class ShowFindToolBarAction extends AbstractAction {

        private final FindToolBar findToolBar;

        ShowFindToolBarAction(FindToolBar findToolBar) {
            super("Find");
            this.findToolBar = findToolBar;
            int c = InputEvent.CTRL_DOWN_MASK;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            collapsibleSectionPanel.showBottomComponent(findToolBar);
        }
    }

    /**
     * Shows the Replace toolbar.
     */
    private class ShowReplaceToolBarAction extends AbstractAction {

        private final ReplaceToolBar replaceToolBar;

        ShowReplaceToolBarAction(ReplaceToolBar replaceToolBar) {
            super("Replace");
            this.replaceToolBar = replaceToolBar;
            int c = InputEvent.CTRL_DOWN_MASK;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, c));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            collapsibleSectionPanel.showBottomComponent(replaceToolBar);
        }
    }
}
