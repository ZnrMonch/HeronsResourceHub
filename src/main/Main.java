package main;

import utils.*;
import admin.*;
import auth.*;
import marketplace.Marketplace;
import pages.*;
import database.UserRecord;

public class Main {
	
	public static UserRecord createUser() {
		UserRecord dummyUser = new UserRecord();
		
		dummyUser.user_id = 3; 
		dummyUser.system_role = "end_user";
		dummyUser.student_id = "K12360080";
		dummyUser.umak_email_address = "renzjan.moncinilla@umak.edu.ph";
		dummyUser.password = "securePassword123";
		dummyUser.college = "CCIS";
		dummyUser.year_level = "First Year";
		dummyUser.course_program = "BS in Computer Science";
		dummyUser.first_name = "Renzjan";
		dummyUser.last_name = "Moncinilla";
		dummyUser.karma_score = 100;
		dummyUser.profile_image = "/resources/images/profiles/default.png";
		dummyUser.contact_num = "0909 099 9191";
		dummyUser.home_address = "Taguig City";       
		return dummyUser;
	}

	public static void main(String[] args) {
		FontLib.loadFonts();

		UserRecord user = createUser();
		new Page(new Marketplace(user), user);
	}
}