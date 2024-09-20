package org.exoplatform.social.core.model;

import lombok.Data;

@Data
public class SpaceExternalInvitation {

  /**
   * The invitation id.
   */
  private Long    invitationId;

  /**
   * The space id.
   */
  private String  spaceId;

  /**
   * The user email.
   */
  private String  userEmail;

  /**
   * The token id.
   */
  private String  tokenId;

  /**
   * is expired token.
   */
  private boolean isExpired;

  private long    createdDate;
}
