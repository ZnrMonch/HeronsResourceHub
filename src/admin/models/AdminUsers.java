package admin.models;

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

    public int getUserId() { return userId; }
    public String getSystemRole() { return systemRole; }
    public String getStudentId(){ return studentId; }
    public String getEmail(){ return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getCollege() { return college; }
    public String getYearLevel(){ return yearLevel; }
    public String getCourseProgram(){ return courseProgram; }
    public int getKarmaScore(){ return karmaScore; }
    public boolean isArchived(){ return archived; }

    public void setUserId(int userId){ this.userId = userId; }
    public void setSystemRole(String systemRole) { this.systemRole = systemRole; }
    public void setStudentId(String studentId)  { this.studentId = studentId; }
    public void setEmail(String email)  { this.email = email; }
    public void setFirstName(String firstName)  { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setCollege(String college){ this.college = college; }
    public void setYearLevel(String yearLevel){ this.yearLevel = yearLevel; }
    public void setCourseProgram(String cp)  { this.courseProgram = cp; }
    public void setKarmaScore(int karmaScore) { this.karmaScore = karmaScore; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}