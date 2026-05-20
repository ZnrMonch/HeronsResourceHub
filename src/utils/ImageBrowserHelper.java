package utils;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.SwingUtilities;

public class ImageBrowserHelper {

    public static String browse(Window owner) {
        Frame frame = (owner instanceof Frame) ? (Frame) owner : null;
        FileDialog fileDialog = new FileDialog(frame, "Select Image", FileDialog.LOAD);
        fileDialog.setFilenameFilter((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg") || lower.endsWith(".gif");
        });
        fileDialog.setVisible(true);
        String dir  = fileDialog.getDirectory();
        String file = fileDialog.getFile();
        return (dir != null && file != null) ? dir + file : "";
    }
}