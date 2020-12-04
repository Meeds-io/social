package org.exoplatform.social.core.model;

public class SpaceExternalInvitation {

    /**
     * The invitation id.
     */
    private Long invitationId;

    /**
     * The space id.
     */
    private String spaceId;

    /**
     * The user email.
     */
    private String userEmail;

    /**
     * The token id.
     */
    private String tokenId;

    /**
     * is expired token.
     */
    private boolean isExpired;

    public Long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }
}
