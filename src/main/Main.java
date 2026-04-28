package main;

import utils.*;
import auth.*;
import pages.*;

public class Main {
	public static void main(String[] args) {
		FontLib.loadFonts();
		
		new Auth();
	}
}