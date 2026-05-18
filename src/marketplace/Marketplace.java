package marketplace;

import java.awt.*;
import components.*;
import database.UserRecord;

public class Marketplace extends CustomTabbedPane {
    private static final long serialVersionUID = 1L;
    
    public Marketplace() {
        setBackground(Color.WHITE);
        setRadius(20);    
        
        addTab("MARKETPLACE", "/resources/icons/marketplace.png", new MarketplaceTabView(createUser()));        
        addTab("SHARING CENTER", "/resources/icons/sharing.png", new SharingTabView(createUser()));
        addTab("BARTER TRADING", "/resources/icons/barter.png", new TradingTabView(createUser()));
    }
    
    public UserRecord createUser() {
        UserRecord dummyUser = new UserRecord();
        
        dummyUser.userId = 3; 
        dummyUser.systemRole = "end_user";
        dummyUser.studentId = "K12360080";
        dummyUser.umakEmailAddress = "renzjan.moncinilla@umak.edu.ph";
        dummyUser.password = "securePassword123";
        dummyUser.college = "CCIS";
        dummyUser.yearLevel = "First Year";
        dummyUser.courseProgram = "BS in Computer Science";
        dummyUser.firstName = "Renzjan";
        dummyUser.lastName = "Moncinilla";
        dummyUser.karmaScore = 100;
        dummyUser.profileImage = "/resources/images/profiles/default.png";
        dummyUser.contactNum = 639123456789L;
        dummyUser.homeAddress = "Taguig City";
        dummyUser.gcashNum = 639123456789L;
        dummyUser.mayaNum = 639987654321L;
        dummyUser.mastercardNum = "5123456789012345";
        dummyUser.visaNum = "4123456789012345";
        
        return dummyUser;
    }
}