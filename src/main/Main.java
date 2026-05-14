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

//		new Auth();
//		new Page(new AdminPanel());
		new Page(new Marketplace());
=======
		
	      new Auth();
		  //new Page(new Marketplace());
		  //new Page(new Profile()); 
	      //new Page(new AdminPanel());

>>>>>>> branch 'master' of https://github.com/ZnrMonch/HeronsResourceHub
	}
}