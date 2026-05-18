package main;

import utils.*;
import admin.*;
import auth.*;
import marketplace.Marketplace;
import pages.*;

public class Main {
	public static void main(String[] args) {
		FontLib.loadFonts();

		// Temporary
		  SessionManager.login(8, "admin", "Jasmine Claire Martizano");
		  
//	      new Auth();
//		  new Page(new Marketplace());
//		  new Page(new Profile()); 
	      new Page(new AdminPanel());


	}
}