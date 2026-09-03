/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.space.service;

import java.util.List;

import static org.junit.Assert.assertThrows;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.constant.UserSpacesScope;

/**
 * The four access-control cases of the profile spaces listing (eXIP 7.3.0.18,
 * board stories US01, US02, US03 and US07), asserted on what a viewer actually
 * receives rather than on the scope handed to the storage.
 * <p>
 * The external-viewer case is load-bearing: an external viewer is
 * indistinguishable at the REST annotation level, since the externals role
 * always carries the users role too, so {@code Identity.isExternal()} inside the
 * Service is the only discriminator on that axis. Remove it, or branch on the
 * profile owner instead of the viewer, and
 * {@link #testExternalViewerCannotOptOutOfCommonScope()} fails.
 * <p>
 * This is a container test on purpose: {@code SpaceServiceImpl} cannot be built
 * outside a container, because {@code AbstractLifeCycle}'s constructor calls
 * {@code PortalContainer.getInstance()} — instantiating it in a plain Mockito
 * test boots a half-configured container that then breaks every container test
 * in the same JVM.
 */
public class UserSpacesServiceTest extends AbstractCoreTest {

  private static final String OWNER           = "john";

  private static final String VIEWER          = "mary";

  private static final String EXTERNAL_VIEWER = "demo";

  private static final String EXTERNAL_OWNER  = "ghost";

  private SpaceStorage        spaceStorage;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);

    createIdentity(OWNER);
    createIdentity(VIEWER);
    createIdentity(EXTERNAL_VIEWER);
    createIdentity(EXTERNAL_OWNER);
    markExternal(EXTERNAL_VIEWER);
    markExternal(EXTERNAL_OWNER);

    saveSpace(1, "Alpha space", Space.PUBLIC, OWNER, VIEWER);
    saveSpace(2, "Beta space", Space.PUBLIC, OWNER, EXTERNAL_VIEWER);
    saveSpace(3, "Gamma space", Space.PRIVATE, OWNER);
    saveSpace(4, "Delta space", Space.HIDDEN, OWNER);
    saveSpace(5, "Epsilon space", Space.PUBLIC, EXTERNAL_OWNER, VIEWER);
    saveSpace(6, "Zeta space", Space.PUBLIC, EXTERNAL_OWNER);
    restartTransaction();
  }

  @Override
  protected void tearDown() throws Exception {
    // The identity property rows outlive this test class in the shared test
    // database, and other suites assert on the absence of external members
    markExternal(EXTERNAL_VIEWER, false);
    markExternal(EXTERNAL_OWNER, false);
    restartTransaction();
    super.tearDown();
  }

  public void testOwnProfileListsEverySpaceIncludingHidden() throws ObjectNotFoundException {
    List<Space> spaces = spaceService.getUserSpaces(OWNER, OWNER, UserSpacesScope.COMMON, 0, 20);

    // Own profile is not a scope the client chooses
    assertEquals(4, spaces.size());
    assertEquals(4, spaceService.countUserSpaces(OWNER, OWNER, UserSpacesScope.COMMON));
    assertTrue(spaces.stream().anyMatch(space -> space.getVisibility().equals(Space.HIDDEN)));
  }

  public void testOwnProfileOfAnExternalUserIsNotNarrowed() throws ObjectNotFoundException {
    List<Space> spaces = spaceService.getUserSpaces(EXTERNAL_VIEWER, EXTERNAL_VIEWER, UserSpacesScope.ALL, 0, 20);

    assertEquals(1, spaces.size());
    assertEquals("Beta space", spaces.get(0).getDisplayName());
  }

  public void testVisitedProfileHidesTheHiddenSpacesTheViewerIsNotIn() throws ObjectNotFoundException {
    List<Space> spaces = spaceService.getUserSpaces(VIEWER, OWNER, UserSpacesScope.ALL, 0, 20);

    // Alpha (common), Beta (public) and Gamma (private, not hidden). Delta is
    // hidden and the viewer is not a member of it.
    assertEquals(3, spaces.size());
    assertEquals(3, spaceService.countUserSpaces(VIEWER, OWNER, UserSpacesScope.ALL));
    assertFalse(spaces.stream().anyMatch(space -> space.getVisibility().equals(Space.HIDDEN)));
    // PRIVATE is not HIDDEN: it stays listed
    assertTrue(spaces.stream().anyMatch(space -> space.getDisplayName().equals("Gamma space")));
  }

  public void testVisitedProfileHonoursTheCommonScope() throws ObjectNotFoundException {
    List<Space> spaces = spaceService.getUserSpaces(VIEWER, OWNER, UserSpacesScope.COMMON, 0, 20);

    assertEquals(1, spaces.size());
    assertEquals("Alpha space", spaces.get(0).getDisplayName());
    assertEquals(1, spaceService.countUserSpaces(VIEWER, OWNER, UserSpacesScope.COMMON));
  }

  public void testInternalViewerOnExternalOwnerGetsCommonSpacesOnly() throws ObjectNotFoundException {
    List<Space> spaces = spaceService.getUserSpaces(VIEWER, EXTERNAL_OWNER, UserSpacesScope.ALL, 0, 20);

    // Zeta is public and the owner is a member of it, but the owner is external
    assertEquals(1, spaces.size());
    assertEquals("Epsilon space", spaces.get(0).getDisplayName());
    assertEquals(1, spaceService.countUserSpaces(VIEWER, EXTERNAL_OWNER, UserSpacesScope.ALL));
  }

  public void testExternalViewerCannotOptOutOfCommonScope() throws ObjectNotFoundException {
    // The client asks for the permissive scope, on an INTERNAL owner's profile
    List<Space> spaces = spaceService.getUserSpaces(EXTERNAL_VIEWER, OWNER, UserSpacesScope.ALL, 0, 20);

    // ... and the Service overrides it: only the space they share
    assertEquals(1, spaces.size());
    assertEquals("Beta space", spaces.get(0).getDisplayName());
    assertEquals(1, spaceService.countUserSpaces(EXTERNAL_VIEWER, OWNER, UserSpacesScope.ALL));
  }

  public void testMissingScopeDefaultsToAllForAnInternalViewer() throws ObjectNotFoundException {
    assertEquals(3, spaceService.getUserSpaces(VIEWER, OWNER, null, 0, 20).size());
  }

  public void testListingIsOrderedAlphabetically() throws ObjectNotFoundException {
    List<Space> spaces = spaceService.getUserSpaces(OWNER, OWNER, UserSpacesScope.ALL, 0, 20);

    // Board stories US01, US02 and US05: alphabetical, never the owner's last
    // visit — that would disclose their browsing recency to a visitor
    assertEquals("Alpha space", spaces.get(0).getDisplayName());
    assertEquals("Beta space", spaces.get(1).getDisplayName());
    assertEquals("Delta space", spaces.get(2).getDisplayName());
    assertEquals("Gamma space", spaces.get(3).getDisplayName());
  }

  public void testListingIsPaged() throws ObjectNotFoundException {
    assertEquals(2, spaceService.getUserSpaces(OWNER, OWNER, UserSpacesScope.ALL, 0, 2).size());
    List<Space> secondPage = spaceService.getUserSpaces(OWNER, OWNER, UserSpacesScope.ALL, 2, 2);
    assertEquals(2, secondPage.size());
    assertEquals("Delta space", secondPage.get(0).getDisplayName());
  }

  public void testAnonymousViewerGetsNothing() throws ObjectNotFoundException {
    // The Service is the layer that stops an unauthenticated caller: the Storage
    // below refuses a blank viewer outright rather than widening
    assertTrue(spaceService.getUserSpaces(null, OWNER, UserSpacesScope.ALL, 0, 20).isEmpty());
    assertTrue(spaceService.getUserSpaces("", OWNER, UserSpacesScope.ALL, 0, 20).isEmpty());
    assertTrue(spaceService.getUserSpaces(IdentityConstants.ANONIM, OWNER, UserSpacesScope.ALL, 0, 20).isEmpty());
    assertTrue(spaceService.getUserSpaces(IdentityConstants.SYSTEM, OWNER, UserSpacesScope.ALL, 0, 20).isEmpty());
    assertEquals(0, spaceService.countUserSpaces(null, OWNER, UserSpacesScope.ALL));
    assertEquals(0, spaceService.countUserSpaces(IdentityConstants.ANONIM, OWNER, UserSpacesScope.ALL));
  }

  public void testUnresolvableViewerGetsTheRestrictiveScope() throws ObjectNotFoundException {
    // Not anonymous, so the Service's first guard lets it through, but no
    // identity resolves: isExternal() cannot answer, and an unresolvable viewer
    // must not be treated as an internal one
    assertTrue(spaceService.getUserSpaces("notauser", OWNER, UserSpacesScope.ALL, 0, 20).isEmpty());
    assertEquals(0, spaceService.countUserSpaces("notauser", OWNER, UserSpacesScope.ALL));
  }

  public void testUnknownProfileOwnerIsNotFound() {
    assertThrows(ObjectNotFoundException.class,
                 () -> spaceService.getUserSpaces(VIEWER, "notauser", UserSpacesScope.ALL, 0, 20));
    assertThrows(ObjectNotFoundException.class,
                 () -> spaceService.countUserSpaces(VIEWER, "notauser", UserSpacesScope.ALL));
  }

  public void testExternalFlagIsEffectivelySetOnTheFixture() {
    assertTrue(identityManager.getOrCreateUserIdentity(EXTERNAL_VIEWER).isExternal());
    assertTrue(identityManager.getOrCreateUserIdentity(EXTERNAL_OWNER).isExternal());
    assertFalse(identityManager.getOrCreateUserIdentity(OWNER).isExternal());
    assertFalse(identityManager.getOrCreateUserIdentity(VIEWER).isExternal());
  }

  private void markExternal(String username) {
    markExternal(username, true);
  }

  private void markExternal(String username, boolean external) {
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    Profile profile = identity.getProfile();
    profile.setProperty(Profile.EXTERNAL, String.valueOf(external));
    identityManager.updateProfile(profile);
  }

  private void saveSpace(int number, String displayName, String visibility, String... members) {
    Space space = new Space();
    space.setDisplayName(displayName);
    space.setPrettyName(displayName);
    space.setRegistration(Space.OPEN);
    space.setDescription("space " + number);
    space.setVisibility(visibility);
    space.setGroupId("/spaces/userspacesservice" + number);
    space.setUrl(space.getPrettyName());
    space.setManagers(new String[] { members[0] });
    space.setMembers(members);
    space.setInvitedUsers(new String[] {});
    space.setPendingUsers(new String[] {});
    spaceStorage.saveSpace(space, true);
  }
}
