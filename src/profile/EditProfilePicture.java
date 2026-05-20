package profile;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import components.*;
import utils.*;

public class EditProfilePicture extends JDialog {

    private static final long serialVersionUID = 1L;
    private JLabel pictureLabel;
    
    private String currentImagePath = "/resources/defaultpictures/axolotl.jpg";
    
    private boolean isSaved = false;
    private List<ImageOption> optionsList = new ArrayList<>();
    private ImageOption selectedOption = null;
    private String tempSelectedFilePath = null;
    
    private String[] premadeImagePath = { 
        "/resources/defaultpictures/axolotl.jpg","/resources/defaultpictures/cattop.jpg","/resources/defaultpictures/ducktab.jpg",
        "/resources/defaultpictures/frog_gear.jpg","/resources/defaultpictures/hedgehog.jpg","/resources/defaultpictures/mr_rat.jpg",
        "/resources/defaultpictures/panda_pen.jpg","/resources/defaultpictures/parrot.jpg","/resources/defaultpictures/slim_dog.jpg"
    };

    public EditProfilePicture(JFrame parent, String userProfileImagePath) {
        super(parent, "Edit Profile Picture", true);
        
        if (userProfileImagePath != null && !userProfileImagePath.trim().isEmpty()) {
            this.currentImagePath = userProfileImagePath;
        }

        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        init();

        pack();
        setSize(650, getPreferredSize().height + 30);
        setLocationRelativeTo(parent);
        setVisible(true); 
    }

    /**
     * SMART AVATAR LOADER: Bypasses IconLoader to handle newly uploaded physical files AND Classpath files perfectly.
     */
    private Icon createCircularIcon(String path, int width, int height) {
        if (path == null || path.trim().isEmpty()) path = "/resources/defaultpictures/axolotl.jpg";
        Image img = null;
        try {
            File physicalFile = new File(System.getProperty("user.dir") + "/src" + path);
            if (physicalFile.exists()) {
                img = ImageIO.read(physicalFile);
            } else {
                java.net.URL url = getClass().getResource(path);
                if (url != null) img = ImageIO.read(url);
            }
        } catch (Exception e) {}

        if (img == null) {
            try {
                java.net.URL fallback = getClass().getResource("/resources/defaultpictures/axolotl.jpg");
                if (fallback != null) img = ImageIO.read(fallback);
            } catch (Exception e) {}
        }
        
        if (img == null) return new ImageIcon();

        BufferedImage circleBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillOval(0, 0, width, height);
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(img, 0, 0, width, height, null);
        g2.dispose();

        return new ImageIcon(circleBuffer);
    }

    private void init() {
        CustomPanel wrapper = new CustomPanel(new BorderLayout(0, 20));

        CustomPanel header = new CustomPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        // Use our new smart loader instead of IconLoader
        pictureLabel = new JLabel(createCircularIcon(currentImagePath, 150, 150));
        pictureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(pictureLabel);
        header.add(Box.createVerticalStrut(10));

        wrapper.add(header, BorderLayout.NORTH);

        CustomPanel center = new CustomPanel(new GridLayout(2, 5, 10, 10));

        ImageOption uploadOption = new ImageOption(null, true);
        optionsList.add(uploadOption); 
        center.add(uploadOption);
        
        for (String path : premadeImagePath) { 
            ImageOption opt = new ImageOption(path, false); 
            optionsList.add(opt); 
            center.add(opt); 
        }

        wrapper.add(center, BorderLayout.CENTER);

        CustomPanel actionWrapper = new CustomPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        CustomButton cancelButton = new CustomButton("Cancel", 10); 
        cancelButton.setPadding(8,20,8,20); 
        cancelButton.addActionListener(e->dispose());
        
        CustomButton saveButton = new CustomButton("Save", 10); 
        saveButton.setDefaultColor(Brand.PRIMARY_COLOR);
        saveButton.setHoverColor(Brand.PRIMARY_COLOR.darker()); 
        saveButton.setPadding(8,20,8,20);
        
        saveButton.addActionListener(e->{
            if (selectedOption != null && selectedOption.isUpload && tempSelectedFilePath != null) {
                try {
                    String destPath = saveImageToProfilePicturesDir(new File(tempSelectedFilePath));
                    if (destPath != null) currentImagePath = destPath;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to save picture!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return; 
                }
            }
            isSaved = true; 
            dispose();
        });
        
        actionWrapper.add(cancelButton); 
        actionWrapper.add(saveButton);

        wrapper.add(actionWrapper, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.CENTER);
    }

    private void handleSelection(ImageOption selectedOption) {
        if (selectedOption.isUpload) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                tempSelectedFilePath = selectedFile.getAbsolutePath();
                
                // When uploading, we must read the physical file to preview it
                try {
                    Image uploadedImg = ImageIO.read(selectedFile);
                    BufferedImage circleBuffer = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = circleBuffer.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.fillOval(0, 0, 150, 150);
                    g2.setComposite(AlphaComposite.SrcIn);
                    g2.drawImage(uploadedImg, 0, 0, 150, 150, null);
                    g2.dispose();
                    pictureLabel.setIcon(new ImageIcon(circleBuffer));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                
                updateSelectionState(selectedOption);
                this.selectedOption = selectedOption;
            }
        } else {
            currentImagePath = selectedOption.imagePath;
            pictureLabel.setIcon(createCircularIcon(currentImagePath, 150, 150));
            updateSelectionState(selectedOption);
            this.selectedOption = selectedOption;
        }
    }

    private String saveImageToProfilePicturesDir(File uploadedImage) throws IOException {
        String projectDir = System.getProperty("user.dir");
        File profilePicturesDir = new File(projectDir, "src" + File.separator + "resources" + File.separator + "profilepictures");
        
        if (!profilePicturesDir.exists() && !profilePicturesDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + profilePicturesDir.getAbsolutePath());
        }
        
        String name = uploadedImage.getName();
        String extension = "";
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) extension = name.substring(lastDot).toLowerCase();
        
        if (!".jpg".equals(extension) && !".jpeg".equals(extension) && !".png".equals(extension) && !".gif".equals(extension))
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG, GIF allowed.");
            
        String fileName = "profile_" + System.currentTimeMillis() + extension;
        File destination = new File(profilePicturesDir, fileName);
        
        java.nio.file.Files.copy(uploadedImage.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return "/resources/profilepictures/" + fileName;
    }

    private void updateSelectionState(ImageOption selectedOption) {
        for (ImageOption opt : optionsList) {
            opt.setSelected(opt == selectedOption);
        }
    }

    public String getSavedImagePath() {
        return isSaved ? currentImagePath : null;
    }

    private class ImageOption extends JPanel {
        
        private static final long serialVersionUID = 1L;
        private String imagePath;
        private boolean isUpload;
        private boolean isHovered = false;
        private boolean isSelected = false;
        private Icon circularIcon;

        public ImageOption(String imagePath, boolean isUpload) {
            this.imagePath = imagePath;
            this.isUpload = isUpload;
            this.setOpaque(false);
            this.setPreferredSize(new Dimension(100, 100));
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (!isUpload && imagePath != null) {
                circularIcon = createCircularIcon(imagePath, 90, 90);
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    handleSelection(ImageOption.this);
                }
            });
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int imgOffset = 5;
            int imgSize = 90;

            if (isUpload) {
                g2.setColor(new Color(245, 245, 245));
                g2.fillOval(imgOffset, imgOffset, imgSize, imgSize);

                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(50, 30, 50, 70); 
                g2.drawLine(30, 50, 70, 50); 
            } else if (circularIcon != null) {
                circularIcon.paintIcon(this, g2, imgOffset, imgOffset);
            }

            if (isSelected) {
                g2.setColor(new Color(Brand.PRIMARY_COLOR.getRed(), Brand.PRIMARY_COLOR.getGreen(), Brand.PRIMARY_COLOR.getBlue(), 120));
                g2.fillOval(imgOffset, imgOffset, imgSize, imgSize);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(35, 50, 45, 60);
                g2.drawLine(45, 60, 65, 35);
            }

            int borderOffset = imgOffset - 2;
            int borderSize = imgSize + 4;

            if (isSelected) {
                g2.setColor(Brand.PRIMARY_COLOR);
                g2.setStroke(new BasicStroke(4));
            } else if (isHovered) {
                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(4));
            } else {
                g2.setColor(Color.LIGHT_GRAY);
                g2.setStroke(new BasicStroke(2));
            }

            g2.drawOval(borderOffset, borderOffset, borderSize, borderSize);
            g2.dispose();
        }
    }
}