// ══════════════════════════════════════════════════════════════════════════════
// AdminLogs.java
// ──────────────────────────────────────────────────────────────────────────────
// Plain model (data-holder) class for a single log entry.
//
// One instance of this class = one row retrieved from any of the four log tables.
// LogsDatabase.mapRow() fills it; AdminLogsServices passes it up to the UI layer.
//
// Fields are kept exactly as they were — no new columns added.
// ══════════════════════════════════════════════════════════════════════════════
package admin.models;

public class AdminLogs {

    // ── Fields ────────────────────────────────────────────────────────────────

    /** The row's primary key (log_id / transaction_id / reputation_id). */
    private int id;

    /**
     * The ENUM action value stored in the DB (e.g. "Create", "Borrow-Approved").
     * Aliased as "log_type" in every SELECT so mapRow() always finds this column.
     */
    private String logType;

    /** FK to the user involved in this event. 0 means unknown / system. */
    private int userId;

    /**
     * FK to the item involved. 0 for log types that don't track items
     * (users_log, reputation_log) — LogsDatabase SELECTs "0 AS item_id" for those.
     */
    private int itemId;

    /** The human-readable reason / description text stored in the reason column. */
    private String description;

    /**
     * Timestamp as a String (format: "YYYY-MM-DD HH:MM:SS").
     * Null for transaction_log rows because that table has no timestamp column.
     */
    private String createdAt;

    /**
     * True when this log was retrieved from an archive query.
     * Not stored in the DB — set by the service layer if needed.
     */
    private boolean archived;


    // ── Getters ───────────────────────────────────────────────────────────────

    public int     getId()          { return id;          }
    public String  getLogType()     { return logType;     }
    public int     getUserId()      { return userId;      }
    public int     getItemId()      { return itemId;      }
    public String  getDescription() { return description; }
    public String  getCreatedAt()   { return createdAt;   }
    public boolean isArchived()     { return archived;    }


    // ── Setters ───────────────────────────────────────────────────────────────

    public void setId(int id)                   { this.id          = id;          }
    public void setLogType(String logType)       { this.logType     = logType;     }
    public void setUserId(int userId)            { this.userId      = userId;      }
    public void setItemId(int itemId)            { this.itemId      = itemId;      }
    public void setDescription(String desc)      { this.description = desc;        }
    public void setCreatedAt(String createdAt)   { this.createdAt   = createdAt;   }
    public void setArchived(boolean archived)    { this.archived    = archived;    }
}