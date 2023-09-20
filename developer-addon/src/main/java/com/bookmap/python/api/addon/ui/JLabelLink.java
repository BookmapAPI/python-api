package com.bookmap.python.api.addon.ui;

import com.bookmap.python.api.addon.utils.Log;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JLabel;

/**
 * A label with URL-link that opens in default browser on mouse click
 * <p>
 * This class is taken from websocket-l0-framework
 */
public class JLabelLink extends JLabel {

    public JLabelLink(String labelText, String linkText, String labelText2, String url) {
        this.setText(
                "<html>" +
                labelText +
                "<font color=#1690c9><a href=\"\">" +
                linkText +
                "</a></font>" +
                labelText2 +
                "</html>"
            );
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI(url));
                        } catch (URISyntaxException | IOException ex) {
                            Log.error("Error opening desktop", ex);
                        }
                    }
                }
            );
    }

    public JLabelLink(String linkText, String url) {
        this("", linkText, "", url);
    }

    public JLabelLink(String labelText, String linkText, String url) {
        this(labelText, linkText, "", url);
    }
}
