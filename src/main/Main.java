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
		

		new Page(new AdminPanel());


=======

//	      new Auth();
		  new Page(new Marketplace());
//		  new Page(new Profile()); 
//	      new Page(new AdminPanel());
>>>>>>> 67c30995b8a1a82a0648cac47d1ba74cea853876

	}
}