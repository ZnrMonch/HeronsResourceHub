package auth;

import java.awt.*;
import javax.swing.*;
import components.*;

public class Auth extends JFrame {
	private CustomPanel contentPane;
	
    public Auth() {
    	setTitle("Authentication");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setExtendedState(MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(1100, 900));
		setPreferredSize(new Dimension(1100, 1000));
		
		contentPane = new CustomPanel("/resources/images/umak_img.jpg");
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);
		
		showLogin();
		
		setVisible(true);
    }
    
    public void showLogin() {
    	contentPane.removeAll();
    	contentPane.add(new Login(this));
    	contentPane.revalidate();
    	contentPane.repaint();
    }
    
    public void showRegistration() {
    	contentPane.removeAll();
    	contentPane.add(new Registration(this));
    	contentPane.revalidate();
    	contentPane.repaint();
    }
}