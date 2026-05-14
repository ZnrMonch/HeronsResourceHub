package main;

import utils.*;
import admin.*;
import auth.*;
import marketplace.Marketplace;
import pages.*;

public class Main {
	public static void main(String[] args) {
		FontLib.loadFonts();
		
<<<<<<< HEAD
<<<<<<< HEAD
		new Page(new AdminPanel());
=======
//		new Auth();
//		new Page(new AdminPanel());
		new Page(new Marketplace());
>>>>>>> 0af9b3c (drafted changes for main system)
=======
	      new Auth();
		  //new Page(new Profile()); 
>>>>>>> 556f0dc (sa log in and registration may changes ulit yan later)
	}
}