package main;

import utils.*;
import admin.*;
import auth.*;
import marketplace.Marketplace;
import pages.*;
import database.UserRecord;

public class Main {
	public static void main(String[] args) {
		FontLib.loadFonts();

		new Auth();
		
	}
}