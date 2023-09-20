package com.bookmap.python.api.addon.settings;

import velox.api.layer1.settings.StrategySettingsVersion;

@StrategySettingsVersion(currentVersion = 3, compatibleVersions = { 3 })
public class PythonApiSettings {

    private String pathToPythonExecutable;
    private String chosenTheme;
    private boolean codeFoldingEnabled;
    private boolean lineHighlightEnabled;
    private boolean lineNumbersEnabled;
    private boolean bracketMatchingAnimationEnabled;
    private boolean bookmarksEnabled;
    private boolean wordWrapEnabled;
    private boolean antiAliasingEnabled;
    private boolean markOccurrencesEnabled;
    private boolean tabLinesEnabled;

    public PythonApiSettings() {
        pathToPythonExecutable = null;
        chosenTheme = "Dark";
        codeFoldingEnabled = true;
        lineHighlightEnabled = true;
        lineNumbersEnabled = true;
        bracketMatchingAnimationEnabled = true;
        bookmarksEnabled = true;
        wordWrapEnabled = false;
        antiAliasingEnabled = true;
        markOccurrencesEnabled = true;
        tabLinesEnabled = false;
    }

    public PythonApiSettings(
        String pathToPythonExecutable,
        String chosenTheme,
        boolean codeFoldingEnabled,
        boolean lineHighlightEnabled,
        boolean lineNumbersEnabled,
        boolean bracketMatchingAnimationEnabled,
        boolean bookmarksEnabled,
        boolean wordWrapEnabled,
        boolean antiAliasingEnabled,
        boolean markOccurrencesEnabled,
        boolean tabLinesEnabled
    ) {
        this.pathToPythonExecutable = pathToPythonExecutable;
        this.chosenTheme = chosenTheme;
        this.codeFoldingEnabled = codeFoldingEnabled;
        this.lineHighlightEnabled = lineHighlightEnabled;
        this.lineNumbersEnabled = lineNumbersEnabled;
        this.bracketMatchingAnimationEnabled = bracketMatchingAnimationEnabled;
        this.bookmarksEnabled = bookmarksEnabled;
        this.wordWrapEnabled = wordWrapEnabled;
        this.antiAliasingEnabled = antiAliasingEnabled;
        this.markOccurrencesEnabled = markOccurrencesEnabled;
        this.tabLinesEnabled = tabLinesEnabled;
    }

    public String getPathToPythonExecutable() {
        return pathToPythonExecutable;
    }

    public void setPathToPythonExecutable(String pathToPythonExecutable) {
        this.pathToPythonExecutable = pathToPythonExecutable;
    }

    public String getChosenTheme() {
        return chosenTheme;
    }

    public void setChosenTheme(String chosenTheme) {
        this.chosenTheme = chosenTheme;
    }

    public boolean isCodeFoldingEnabled() {
        return codeFoldingEnabled;
    }

    public void setCodeFoldingEnabled(boolean codeFoldingEnabled) {
        this.codeFoldingEnabled = codeFoldingEnabled;
    }

    public boolean isLineHighlightEnabled() {
        return lineHighlightEnabled;
    }

    public void setLineHighlightEnabled(boolean lineHighlightEnabled) {
        this.lineHighlightEnabled = lineHighlightEnabled;
    }

    public boolean isLineNumbersEnabled() {
        return lineNumbersEnabled;
    }

    public void setLineNumbersEnabled(boolean lineNumbersEnabled) {
        this.lineNumbersEnabled = lineNumbersEnabled;
    }

    public boolean isBracketMatchingAnimationEnabled() {
        return bracketMatchingAnimationEnabled;
    }

    public void setBracketMatchingAnimationEnabled(boolean bracketMatchingAnimationEnabled) {
        this.bracketMatchingAnimationEnabled = bracketMatchingAnimationEnabled;
    }

    public boolean isBookmarksEnabled() {
        return bookmarksEnabled;
    }

    public void setBookmarksEnabled(boolean bookmarksEnabled) {
        this.bookmarksEnabled = bookmarksEnabled;
    }

    public boolean isWordWrapEnabled() {
        return wordWrapEnabled;
    }

    public void setWordWrapEnabled(boolean wordWrapEnabled) {
        this.wordWrapEnabled = wordWrapEnabled;
    }

    public boolean isAntiAliasingEnabled() {
        return antiAliasingEnabled;
    }

    public void setAntiAliasingEnabled(boolean antiAliasingEnabled) {
        this.antiAliasingEnabled = antiAliasingEnabled;
    }

    public boolean isMarkOccurrencesEnabled() {
        return markOccurrencesEnabled;
    }

    public void setMarkOccurrencesEnabled(boolean markOccurrencesEnabled) {
        this.markOccurrencesEnabled = markOccurrencesEnabled;
    }

    public boolean isTabLinesEnabled() {
        return tabLinesEnabled;
    }

    public void setTabLinesEnabled(boolean tabLinesEnabled) {
        this.tabLinesEnabled = tabLinesEnabled;
    }

    @Override
    public String toString() {
        return (
            "PythonApiSettings{" +
            "pathToPythonExecutable='" +
            pathToPythonExecutable +
            '\'' +
            ", chosenTheme='" +
            chosenTheme +
            '\'' +
            ", codeFoldingEnabled=" +
            codeFoldingEnabled +
            ", lineHighlightEnabled=" +
            lineHighlightEnabled +
            ", lineNumbersEnabled=" +
            lineNumbersEnabled +
            ", bracketMatchingAnimationEnabled=" +
            bracketMatchingAnimationEnabled +
            ", bookmarksEnabled=" +
            bookmarksEnabled +
            ", wordWrapEnabled=" +
            wordWrapEnabled +
            ", antiAliasingEnabled=" +
            antiAliasingEnabled +
            ", markOccurrencesEnabled=" +
            markOccurrencesEnabled +
            ", tabLinesEnabled=" +
            tabLinesEnabled +
            '}'
        );
    }
}
