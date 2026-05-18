package admin.models;

import java.sql.Timestamp;

public class AdminUsers {

	private int userId;
	private String systemRole;
	private String studentId;
	private String email;
	private String firstName;
	private String lastName;
	private String college;
	private String yearLevel;
	private String courseProgram;
	private int karmaScore;
	private boolean archived;
	private String homeAddress;
	private Timestamp archivedAt;

	private String contactNumber;
	private String gcashNum;
	private String mayaNum;
	private String mastercardNum;
	private String visaNum;

	// Getters
	public int getUserId() {
		return userId;
	}

	public String getSystemRole() {
		return systemRole;
	}

	public String getStudentId() {
		return studentId;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getCollege() {
		return college;
	}

	public String getYearLevel() {
		return yearLevel;
	}

	public String getCourseProgram() {
		return courseProgram;
	}

	public int getKarmaScore() {
		return karmaScore;
	}

	public boolean isArchived() {
		return archived;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public String getGcashNum() {
		return gcashNum;
	}

	public String getMayaNum() {
		return mayaNum;
	}

	public String getMastercardNum() {
		return mastercardNum;
	}

	public String getVisaNum() {
		return visaNum;
	}

	public void setArchivedAt(Timestamp archivedAt) {
		this.archivedAt = archivedAt;
	}

	public String getFullName() {
		return firstName + " " + lastName;
	}

	public String getHomeAddress() {
		return homeAddress;
	}

	public Timestamp getArchivedAt() {
		return archivedAt;
	}

	// Setters
	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setSystemRole(String systemRole) {
		this.systemRole = systemRole;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setCollege(String college) {
		this.college = college;
	}

	public void setYearLevel(String yearLevel) {
		this.yearLevel = yearLevel;
	}

	public void setCourseProgram(String cp) {
		this.courseProgram = cp;
	}

	public void setKarmaScore(int karmaScore) {
		this.karmaScore = karmaScore;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public void setContactNumber(String v) {
		this.contactNumber = v;
	}

	public void setGcashNum(String v) {
		this.gcashNum = v;
	}

	public void setMayaNum(String v) {
		this.mayaNum = v;
	}

	public void setMastercardNum(String v) {
		this.mastercardNum = v;
	}

	public void setVisaNum(String v) {
		this.visaNum = v;
	}

	public void setHomeAddress(String homeAddress) {
		this.homeAddress = homeAddress;
	}

}