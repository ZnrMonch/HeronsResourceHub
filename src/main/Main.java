package main;

import utils.*;
import admin.*;
import auth.*;
import pages.*;

public class Main {
	public static void main(String[] args) {
		FontLib.loadFonts();
		
		new Page(new Marketplace());
	}
}