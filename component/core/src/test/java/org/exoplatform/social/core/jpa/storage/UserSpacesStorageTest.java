/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage;

import static org.junit.Assert.assertThrows;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.storage.cache.model.data.ListSpacesData;
import org.exoplatform.social.core.storage.cache.model.key.ListSpacesKey;

import java.util.List;

import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;

import io.meeds.social.space.constant.UserSpacesScope;

/**
 * The profile spaces listing at storage level, run against the real
 * {@link SocialStorageCacheService} caches — never mocks. A drift in the cache
 * key of this listing does not merely miss the cache: the key carries an access
 * control discriminator (the viewer and the scope), so a drift can serve one
 * viewer's filtered list to another. Only a real cache can catch that.
 */
public class UserSpacesStorageTest extends AbstractCoreTest {

  private static final String       OWNER   = "spacesowner";

  private static final String       VIEWER_A = "viewera";

  private static final String       VIEWER_B = "viewerb";

  private SpaceStorage              spaceStorage;

  private Space                     alphaSpace;

  private Space                     betaSpace;

  private Space                     gammaSpace;

  private Space                     deltaSpace;

  private SocialStorageCacheService cacheService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    cacheService = getService(SocialStorageCacheService.class);
    cacheService.getSpacesCache().clearCache();
    cacheService.getSpacesCountCache().clearCache();
  }

  public void testOwnProfileListsHiddenSpacesToo() {
    createSpaces();

    List<Space> spaces = getUserSpaces(OWNER, UserSpacesScope.ALL);

    assertEquals(4, spaces.size());
    assertEquals(4, spaceStorage.countUserSpaces(OWNER, OWNER, UserSpacesScope.ALL));
  }

  public void testVisitedProfileHidesTheHiddenSpacesTheViewerIsNotIn() {
    createSpaces();

    List<Space> spaces = getUserSpaces(VIEWER_A, UserSpacesScope.ALL);

    // Alpha (common), Beta (common with the other viewer, but public) and
    // Gamma (private, not hidden) — Delta is hidden and the viewer is not in it
    assertEquals(3, spaces.size());
    assertFalse(spaces.stream().anyMatch(space -> space.getVisibility().equals(Space.HIDDEN)));
    assertEquals(3, spaceStorage.countUserSpaces(VIEWER_A, OWNER, UserSpacesScope.ALL));
  }

  public void testVisitedProfileListsPrivateSpacesTheViewerIsNotIn() {
    createSpaces();

    List<Space> spaces = getUserSpaces(VIEWER_A, UserSpacesScope.ALL);

    // PRIVATE is not HIDDEN: it stays listed, the UI differentiates on
    // membership
    assertTrue(spaces.stream().anyMatch(space -> space.getDisplayName().equals("Gamma space")));
  }

  public void testVisitedProfileShowsAHiddenSpaceTheViewerIsAMemberOf() {
    createSpaces();
    updateSpace(deltaSpace, 4, "Delta space", Space.HIDDEN, new String[] { OWNER, VIEWER_A });

    List<Space> spaces = getUserSpaces(VIEWER_A, UserSpacesScope.ALL);

    assertEquals(4, spaces.size());
  }

  public void testCommonScopeKeepsOnlySharedSpaces() {
    createSpaces();

    List<Space> spaces = getUserSpaces(VIEWER_A, UserSpacesScope.COMMON);

    assertEquals(1, spaces.size());
    assertEquals("Alpha space", spaces.get(0).getDisplayName());
    assertEquals(1, spaceStorage.countUserSpaces(VIEWER_A, OWNER, UserSpacesScope.COMMON));
  }

  public void testTwoViewersNeverShareACacheEntry() {
    createSpaces();

    // Back to back on the same profile owner and the same scope: the only
    // difference is the viewer, which must be part of the key
    List<Space> spacesOfViewerA = getUserSpaces(VIEWER_A, UserSpacesScope.COMMON);
    List<Space> spacesOfViewerB = getUserSpaces(VIEWER_B, UserSpacesScope.COMMON);

    assertEquals(1, spacesOfViewerA.size());
    assertEquals(1, spacesOfViewerB.size());
    assertEquals("Alpha space", spacesOfViewerA.get(0).getDisplayName());
    assertEquals("Beta space", spacesOfViewerB.get(0).getDisplayName());
    assertEquals(1, spaceStorage.countUserSpaces(VIEWER_A, OWNER, UserSpacesScope.COMMON));
    assertEquals(1, spaceStorage.countUserSpaces(VIEWER_B, OWNER, UserSpacesScope.COMMON));
  }

  public void testScopeIsPartOfTheCacheKey() {
    createSpaces();

    // Same viewer, same owner, same paging: only the scope differs
    List<Space> commonSpaces = getUserSpaces(VIEWER_A, UserSpacesScope.COMMON);
    List<Space> allSpaces = getUserSpaces(VIEWER_A, UserSpacesScope.ALL);

    assertEquals(1, commonSpaces.size());
    assertEquals(3, allSpaces.size());
    assertEquals(1, spaceStorage.countUserSpaces(VIEWER_A, OWNER, UserSpacesScope.COMMON));
    assertEquals(3, spaceStorage.countUserSpaces(VIEWER_A, OWNER, UserSpacesScope.ALL));
  }

  public void testScopeIsPartOfTheCacheKeyInTheDisclosingDirection() {
    createSpaces();

    // The reverse order of the test above, which is the dangerous one: the wide
    // listing is cached first, then the narrow scope is requested. A scope
    // missing from the key would serve the owner's whole list to a viewer
    // entitled to the common spaces only.
    assertEquals(3, getUserSpaces(VIEWER_A, UserSpacesScope.ALL).size());
    assertEquals(1, getUserSpaces(VIEWER_A, UserSpacesScope.COMMON).size());
    assertEquals(1, spaceStorage.countUserSpaces(VIEWER_A, OWNER, UserSpacesScope.COMMON));
  }

  public void testMissingScopeNeverWidensThroughTheCache() {
    createSpaces();

    // The wide listing is cached first, then the same (viewer, owner) pair is
    // requested with no scope at all. The predicate builder treats a missing
    // scope as the restrictive one, and the cache key must classify it the same
    // way: otherwise the entry cached for ALL answers a request whose predicate
    // asked for COMMON.
    assertEquals(3, getUserSpaces(VIEWER_A, UserSpacesScope.ALL).size());

    List<Space> withoutScope = spaceStorage.getUserSpaces(VIEWER_A, OWNER, null, alphabetical(), 0, 20);
    assertEquals(1, withoutScope.size());
    assertEquals("Alpha space", withoutScope.get(0).getDisplayName());
    assertEquals(1, spaceStorage.countUserSpaces(VIEWER_A, OWNER, null));
  }

  public void testPagesBeyondTheFirstAreNotCached() {
    createSpaces();
    ExoCache<ListSpacesKey, ListSpacesData> spacesCache = cacheService.getSpacesCache();

    // The bound this listing accepts on a shared cache: only the page that
    // renders on every profile view is cached
    spacesCache.clearCache();
    spaceStorage.getUserSpaces(VIEWER_A, OWNER, UserSpacesScope.ALL, alphabetical(), 1, 2);
    assertEquals(0, spacesCache.getCacheSize());

    spaceStorage.getUserSpaces(VIEWER_A, OWNER, UserSpacesScope.ALL, alphabetical(), 0, 2);
    assertEquals(1, spacesCache.getCacheSize());
  }

  public void testViewerIsMandatory() {
    createSpaces();

    // A missing viewer must never degenerate into the unfiltered listing of the
    // owner's spaces, hidden ones included
    assertThrows(IllegalArgumentException.class,
                 () -> spaceStorage.getUserSpaces(null, OWNER, UserSpacesScope.COMMON, alphabetical(), 0, 20));
    assertThrows(IllegalArgumentException.class,
                 () -> spaceStorage.getUserSpaces("", OWNER, UserSpacesScope.COMMON, alphabetical(), 0, 20));
    assertThrows(IllegalArgumentException.class, () -> spaceStorage.countUserSpaces(null, OWNER, UserSpacesScope.COMMON));
  }

  public void testOwnProfileIsScopeIndependent() {
    createSpaces();

    // With the viewer as the profile owner the viewer axis matches every space
    // of the listing, so both scopes return the same rows, hidden ones included.
    // This is what lets the Service normalise the own profile to a single scope
    // and the cache key carry it without a third key type.
    assertEquals(4, getUserSpaces(OWNER, UserSpacesScope.ALL).size());
    assertEquals(4, getUserSpaces(OWNER, UserSpacesScope.COMMON).size());
    assertEquals(4, spaceStorage.countUserSpaces(OWNER, OWNER, UserSpacesScope.COMMON));
  }

  public void testPagesBeyondTheFirstAreNotCachedButStayCorrect() {
    createSpaces();

    // Only the first page is cached (shared-cache cardinality); the pages below
    // must still be filtered and ordered identically
    List<Space> secondPage = spaceStorage.getUserSpaces(VIEWER_A, OWNER, UserSpacesScope.ALL, alphabetical(), 1, 2);
    assertEquals(2, secondPage.size());
    assertEquals("Beta space", secondPage.get(0).getDisplayName());
    assertEquals("Gamma space", secondPage.get(1).getDisplayName());
    assertFalse(secondPage.stream().anyMatch(space -> space.getVisibility().equals(Space.HIDDEN)));
  }

  public void testEntryIsEvictedOnAMembershipChangeOfTheViewer() {
    createSpaces();
    assertEquals(1, getUserSpaces(VIEWER_B, UserSpacesScope.COMMON).size());

    updateSpace(alphaSpace, 1, "Alpha space", Space.PUBLIC, new String[] { OWNER, VIEWER_A, VIEWER_B });

    assertEquals(2, getUserSpaces(VIEWER_B, UserSpacesScope.COMMON).size());
    assertEquals(2, spaceStorage.countUserSpaces(VIEWER_B, OWNER, UserSpacesScope.COMMON));
  }

  public void testEntryIsEvictedOnAMembershipChangeOfTheProfileOwner() {
    createSpaces();
    assertEquals(4, getUserSpaces(OWNER, UserSpacesScope.ALL).size());

    updateSpace(alphaSpace, 1, "Alpha space", Space.PUBLIC, new String[] { VIEWER_A });

    assertEquals(3, getUserSpaces(OWNER, UserSpacesScope.ALL).size());
  }

  public void testEntryIsEvictedWhenASpaceTurnsHidden() {
    createSpaces();
    assertEquals(3, getUserSpaces(VIEWER_A, UserSpacesScope.ALL).size());

    updateSpace(gammaSpace, 3, "Gamma space", Space.HIDDEN, new String[] { OWNER });

    // A stale entry here would keep exposing a space that has just been hidden
    assertEquals(2, getUserSpaces(VIEWER_A, UserSpacesScope.ALL).size());
  }

  public void testOnlyMemberRoleIsListed() {
    Space invited = getSpaceInstance(5, "Epsilon space", Space.PUBLIC, new String[] { "someoneelse" });
    invited.setInvitedUsers(new String[] { OWNER });
    invited.setPendingUsers(new String[] { VIEWER_A });
    spaceStorage.saveSpace(invited, true);

    // The profile owner is only invited to that space
    assertEquals(0, getUserSpaces(OWNER, UserSpacesScope.ALL).size());
    // ... and a pending viewer shares nothing with anybody
    assertEquals(0, spaceStorage.countUserSpaces(VIEWER_A, "someoneelse", UserSpacesScope.COMMON));
  }

  public void testListingIsOrderedAlphabetically() {
    createSpaces();

    List<Space> spaces = getUserSpaces(OWNER, UserSpacesScope.ALL);

    assertEquals("Alpha space", spaces.get(0).getDisplayName());
    assertEquals("Beta space", spaces.get(1).getDisplayName());
    assertEquals("Delta space", spaces.get(2).getDisplayName());
    assertEquals("Gamma space", spaces.get(3).getDisplayName());
  }

  public void testListingIsPagedByTheDatastore() {
    createSpaces();

    List<Space> firstPage = spaceStorage.getUserSpaces(OWNER, OWNER, UserSpacesScope.ALL, alphabetical(), 0, 2);
    List<Space> secondPage = spaceStorage.getUserSpaces(OWNER, OWNER, UserSpacesScope.ALL, alphabetical(), 2, 2);

    assertEquals(2, firstPage.size());
    assertEquals(2, secondPage.size());
    assertEquals("Alpha space", firstPage.get(0).getDisplayName());
    assertEquals("Delta space", secondPage.get(0).getDisplayName());
  }

  private List<Space> getUserSpaces(String viewerUsername, UserSpacesScope scope) {
    return spaceStorage.getUserSpaces(viewerUsername, OWNER, scope, alphabetical(), 0, 20);
  }

  private Sorting alphabetical() {
    return new Sorting(SortBy.TITLE, OrderBy.ASC);
  }

  private void createSpaces() {
    alphaSpace = spaceStorage.saveSpace(getSpaceInstance(1, "Alpha space", Space.PUBLIC, new String[] { OWNER, VIEWER_A }),
                                        true);
    betaSpace = spaceStorage.saveSpace(getSpaceInstance(2, "Beta space", Space.PUBLIC, new String[] { OWNER, VIEWER_B }), true);
    gammaSpace = spaceStorage.saveSpace(getSpaceInstance(3, "Gamma space", Space.PRIVATE, new String[] { OWNER }), true);
    deltaSpace = spaceStorage.saveSpace(getSpaceInstance(4, "Delta space", Space.HIDDEN, new String[] { OWNER }), true);
    restartTransaction();
  }

  /**
   * Saves a full space definition over an existing one. A space reloaded from
   * the cache can carry a simplified form with no membership array, so the
   * update is built from the same helper as the creation and only reuses the
   * stored identifier.
   */
  private void updateSpace(Space stored, int number, String displayName, String visibility, String[] members) {
    Space update = getSpaceInstance(number, displayName, visibility, members);
    update.setId(stored.getId());
    spaceStorage.saveSpace(update, false);
    restartTransaction();
  }

  private Space getSpaceInstance(int number, String displayName, String visibility, String[] members) {
    Space space = new Space();
    space.setDisplayName(displayName);
    space.setPrettyName(displayName);
    space.setRegistration(Space.OPEN);
    space.setDescription("space " + number);
    space.setVisibility(visibility);
    space.setGroupId("/spaces/userspaces" + number);
    space.setUrl(space.getPrettyName());
    space.setManagers(new String[] { members[0] });
    space.setMembers(members);
    space.setInvitedUsers(new String[] {});
    space.setPendingUsers(new String[] {});
    return space;
  }
}
