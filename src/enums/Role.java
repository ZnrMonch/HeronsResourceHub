package enums;

public enum Role {

 

    SUPER_ADMIN(true,  true,  true,  true),   
    ADMIN      (false, true,  false, false),   
    UNKNOWN    (false, false, false, false);   

 
    private final boolean canCreate;   // add new records
    private final boolean canRead;     // view records
    private final boolean canUpdate;   // edit / update records
    private final boolean canDelete;   // archive or permanently delete records

  

    Role(boolean canCreate, boolean canRead, boolean canUpdate, boolean canDelete) {
        this.canCreate = canCreate;
        this.canRead   = canRead;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
    }

    public boolean canCreate() { return canCreate; }
    public boolean canRead()   { return canRead;   }
    public boolean canUpdate() { return canUpdate; }
    public boolean canDelete() { return canDelete; }

    
    public boolean isSuperAdmin() { return this == SUPER_ADMIN; }

    public boolean isAnyAdmin()   { return this == SUPER_ADMIN || this == ADMIN; }
   
    public static Role from(String roleString) {
        if (roleString == null) return UNKNOWN;
        switch (roleString.trim().toLowerCase()) {
            case "super_admin": return SUPER_ADMIN;
            case "admin":       return ADMIN;
            default:            return UNKNOWN;
        }
    }
}