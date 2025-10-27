package pension_management_system.pension.member.entity;


import lombok.Getter;

/**
 * MemberStatus Enum - Defines the various states a pension member can be in
 * <p>
 * Usage: Used in Member entity to track member's current status
 * <p>
 * Status Flow:
 * ACTIVE → SUSPENDED → ACTIVE (can be reactivated)
 * ACTIVE → INACTIVE → ACTIVE (can be reactivated)
 * ACTIVE → RETIRED (usually final status)
 * Any status → TERMINATED (final status, cannot be changed)
 */

@Getter
public enum MemberStatus {
    /**
     * ACTIVE - Member is currently active and can make contributions
     * This is the default status for new members
     */
    ACTIVE("Active", "Member is active and can make contributions"),
    /**
     * INACTIVE - Member is temporarily inactive
     * Cannot make contributions until reactivated
     * Reasons: Temporary leave, payment pause, etc.
     */
    INACTIVE("Inactive", "Member is temporarily inactive"),
    /**
     * SUSPENDED - Member has been suspended due to rule violations
     * Cannot make contributions or claim benefits
     * Requires admin action to reactivate
     */
    SUSPENDED("Suspended", "Member account is suspended"),
    /**
     * RETIRED - Member has retired and is claiming/claimed benefits
     * Can no longer make new contributions
     */
    RETIRED("Retired", "Member has retired"),

    /**
     * TERMINATED - Member account has been permanently closed
     * This is a final status - cannot be changed
     * Reasons: Left employment, transferred pension, etc.
     */
    TERMINATED("Terminated", "Member account is permanently closed");


    /**
     * -- GETTER --
     *  Get user-friendly display name
     *  * @return Display name (e.g., "Active")
     */
    // Field sto store display name and description
    private final String displayName;
    /*
     * Get detailed description of this status
     * @return Description text
     */
    private final String description;

    /**
     * Constructor - Called when enum constants are created
     * @param displayName User-friendly name
     * @param description What this status means
     */

    MemberStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Check if this status allows making contributions
     * @return true if member can contribute, false otherwise
     */
    public Boolean canMakeContributions() {
        return this == ACTIVE; // Only Active member can make contribution
    }
    /**
     * Check if this status allows claiming benefits
     * @return true if member can claim benefits
     */
    public Boolean canClaimBenefits() {
        // Only ACTIVE and RETIRED members can claim benefits
        return this == INACTIVE || this == SUSPENDED;
    }
    /**
     * Check if status is final (cannot be changed)
     * @return true if this is a final status
     */
    public Boolean isFinalStatus() {
        return this == TERMINATED; // TERMINATED is permanent
    }
    /**
     * Get all active statuses (statuses that indicate member is operational)
     * @return Array of active statuses
     */
    public static MemberStatus[] getActiveStatuses() {
        return new MemberStatus[]{ACTIVE, RETIRED};
    }


}
