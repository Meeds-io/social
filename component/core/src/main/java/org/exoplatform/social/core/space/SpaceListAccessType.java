package org.exoplatform.social.core.space;

public enum SpaceListAccessType {
  /** Gets the all spaces (for super user). */
  ALL,
  /** Gets the all spaces by filter. */
  ALL_FILTER,
  /** Gets the accessible spaces of the user. */
  ACCESSIBLE,
  /** Gets the accessible spaces of the user by filter. */
  ACCESSIBLE_FILTER,
  /** Gets the invited spaces of the user. */
  INVITED,
  /** Gets the invited spaces of the user by filter. */
  INVITED_FILTER,
  /** Gets the pending spaces of the user. */
  PENDING,
  /** Gets the pending spaces of the user by filter. */
  PENDING_FILTER,
  /** Gets the spaces which the user has the "member" role. */
  MEMBER,
  /** Gets the spaces which the user has the "member" role by filter. */
  MEMBER_FILTER,
  /** Gets the favorite spaces of a user by filter. */
  FAVORITE_FILTER,
  /** Gets the spaces which the user has the "manager" role. */
  MANAGER,
  /** Gets the spaces which the user has the "manager" role by filter. */
  MANAGER_FILTER,
  /** Gets the spaces which are visible and not include these spaces hidden */
  VISIBLE,
  /** Provides SpaceNavigation to get the lastest spaces accessed */
  LASTEST_ACCESSED,
  /** Provides Relationship of Users requesting to join a Space */
  PENDING_REQUESTS,
  /** Gets the spaces which are visited at least once */
  VISITED,
  /** Gets the common spaces between two users */
  COMMON
}
