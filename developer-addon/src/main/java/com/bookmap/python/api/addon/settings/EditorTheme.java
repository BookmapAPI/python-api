package com.bookmap.python.api.addon.settings;

public enum EditorTheme {
    LIGHT("Light", "default.xml"),
    LIGHT_SYSTEM_SELECTION("Light (System Selection)", "default-alt.xml"),
    DARK("Dark", "dark.xml"),
    DRUID("Druid", "druid.xml"),
    MONOKAI("Monokai", "monokai.xml"),
    ECLIPSE("Eclipse", "eclipse.xml"),
    IDEA("IDEA", "idea.xml"),
    VISUAL_STUDIO("Visual Studio", "vs.xml");

    private final String name;
    private final String xmlName;

    EditorTheme(String name, String xmlName) {
        this.name = name;
        this.xmlName = xmlName;
    }

    public String getName() {
        return name;
    }

    public String getXmlName() {
        return xmlName;
    }

    public static EditorTheme fromName(String themeName) {
        for (EditorTheme theme : EditorTheme.values()) {
            if (theme.name.equalsIgnoreCase(themeName)) {
                return theme;
            }
        }
        return EditorTheme.DARK; // Default theme
    }
}
