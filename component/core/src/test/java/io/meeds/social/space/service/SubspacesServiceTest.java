/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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

import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.springframework.data.domain.Pageable;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;
import io.meeds.social.space.template.utils.EntityMapper;

import lombok.SneakyThrows;

/**
 * The business rules of the Subspaces List portlet (eXIP 7.3.0.14, board story
 * US01.03), asserted on what a viewer actually receives.
 * <p>
 * This is a container test on purpose, and not a Mockito one: the visibility
 * rule lives in SQL — the VISIBLE list access builds
 * {@code (visibility <> HIDDEN OR (member.userId = :userId AND status IN
 * (MEMBER, INVITED)))} — so only a real query through the real
 * {@code SpaceListAccess} can see it. A mocked storage would agree with
 * whatever the test asserted. {@code SpaceServiceImpl} cannot be instantiated
 * outside a container either.
 * <p>
 * The load-bearing case is {@link #testHiddenSubspaceIsListedToAnInvitedUser()}:
 * it is the one where {@code SpaceStorage.getVisibleSpaces} (MEMBER plus extra
 * status INVITED) and {@code SpaceStorage.getVisibleSpacesCount} (MEMBER only)
 * disagree, which is why the widget derives its 'see more' from an extra row
 * and never from a count. {@link #testVisibleCountDisagreesWithTheVisibleList()}
 * pins that disagreement itself, so that a later "simplification" back to a
 * count query fails here rather than in production.
 */
public class SubspacesServiceTest extends AbstractCoreTest {

  private static final String  PARENT_MANAGER   = "john";

  private static final String  PARENT_MEMBER    = "mary";

  private static final String  OUTSIDER         = "demo";

  private SpaceStorage         spaceStorage;

  private SpaceTemplateStorage spaceTemplateStorage;

  private SpaceTemplate        parentTemplate;

  private Space                parentSpace;

  private Space                otherParentSpace;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    spaceTemplateStorage = getService(SpaceTemplateStorage.class);

    createIdentity(PARENT_MANAGER);
    createIdentity(PARENT_MEMBER);
    createIdentity(OUTSIDER);

    parentTemplate = useMockTemplateAsParentTemplate();

    parentSpace = saveSpace("Parent space", Space.PUBLIC, null, PARENT_MANAGER, PARENT_MEMBER);
    otherParentSpace = saveSpace("Other parent space", Space.PUBLIC, null, PARENT_MANAGER);

    long parentId = parentSpace.getSpaceId();
    // Alphabetical order is deliberately not the insertion order
    saveSpace("Delta subspace", Space.PUBLIC, parentId, PARENT_MANAGER);
    saveSpace("Alpha subspace", Space.PRIVATE, parentId, PARENT_MANAGER);
    saveSpace("Charlie subspace", Space.HIDDEN, parentId, PARENT_MANAGER, PARENT_MEMBER);
    Space invitedTo = saveSpace("Bravo subspace", Space.HIDDEN, parentId, PARENT_MANAGER);
    invitedTo.setInvitedUsers(new String[] { PARENT_MEMBER });
    spaceStorage.saveSpace(invitedTo, false);
    // A sub-space of another parent: it must never leak into this parent's list
    saveSpace("Echo subspace", Space.PUBLIC, otherParentSpace.getSpaceId(), PARENT_MANAGER);
    restartTransaction();
  }

  public void testListsOnlyTheSubspacesOfThatParent() throws Exception {
    List<String> names = names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MANAGER, false, 0, 20));

    assertFalse(names.contains("Echo subspace"));
    assertFalse(names.contains("Other parent space"));
    assertFalse(names.contains("Parent space"));
  }

  public void testPublicAndPrivateSubspacesAreListedWhateverTheMembership() throws Exception {
    // The outsider is a member of none of them, and of the parent neither —
    // the parent-view guard is asserted separately, so ask as a parent member
    List<String> names = names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 0, 20));

    assertTrue(names.contains("Delta subspace"));
    assertTrue(names.contains("Alpha subspace"));
  }

  public void testHiddenSubspaceIsNotListedToANonMember() throws Exception {
    // PARENT_MANAGER is a member of Charlie and Bravo; use a viewer who is a
    // member of the parent but of neither hidden sub-space
    Space hiddenToEveryone = saveSpace("Foxtrot subspace",
                                       Space.HIDDEN,
                                       parentSpace.getSpaceId(),
                                       PARENT_MANAGER);
    restartTransaction();

    List<String> names = names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 0, 20));

    assertFalse(names.contains(hiddenToEveryone.getDisplayName()));
  }

  public void testHiddenSubspaceIsListedToAMember() throws Exception {
    List<String> names = names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 0, 20));

    assertTrue(names.contains("Charlie subspace"));
  }

  /**
   * The board rule of US01.03/US01.05: a hidden sub-space is listed to a viewer
   * who is only <em>invited</em> to it, not just to its members.
   * <p>
   * This pin was the opposite until 2026-09-17. {@code getSubspaces} asked the
   * VISIBLE access, whose {@code extraStatus = INVITED} is dropped by
   * {@code XSpaceFilter.setSpaceFilter} before it reaches the DAO, so the
   * listing was MEMBER-only and the board rule was unmet. It now asks
   * ALL_FILTER with a remote id, which routes to the DAO's
   * {@code :visibleStatuses} branch and its hardcoded {MEMBER, INVITED} — see
   * {@link #testAllFilterPathWithARemoteIdHonoursTheInvitedUser()} for the
   * measurement of the two paths side by side.
   * <p>
   * The platform defect in {@code setSpaceFilter} is untouched and still makes
   * {@code getVisibleSpaces} narrower than {@code SpaceService.canListSpace}
   * for every other caller. This feature no longer depends on it.
   */
  public void testHiddenSubspaceIsListedToAnInvitedUser() throws Exception {
    List<String> names = names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 0, 20));

    assertTrue(names.contains("Bravo subspace"));
  }

  public void testHiddenSubspacesAreListedToEveryViewerWhenTheFlagIsOn() throws Exception {
    List<String> withoutFlag = names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MANAGER, false, 0, 20));
    List<String> withFlag = names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MANAGER, true, 0, 20));

    // The manager is a member of both hidden sub-spaces, so the flag changes
    // nothing for them; it changes everything for a viewer who is in neither
    assertEquals(withoutFlag.size(), withFlag.size());

    Space hiddenToEveryone = saveSpace("Golf subspace",
                                       Space.HIDDEN,
                                       parentSpace.getSpaceId(),
                                       "someoneelse");
    restartTransaction();

    assertFalse(names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 0, 20))
                                                                                                       .contains(hiddenToEveryone.getDisplayName()));
    assertTrue(names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, true, 0, 20))
                                                                                                    .contains(hiddenToEveryone.getDisplayName()));
  }

  public void testListingIsOrderedAlphabetically() throws Exception {
    List<String> names = names(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 0, 20));

    // Bravo is present: hidden, and the viewer is invited to it — see
    // testHiddenSubspaceIsListedToAnInvitedUser
    assertEquals(List.of("Alpha subspace", "Bravo subspace", "Charlie subspace", "Delta subspace"), names);
  }

  public void testListingIsBoundedByTheRequestedLimit() throws Exception {
    // What the widget does: ask subspacesLimit + 1 and read the extra row as
    // the 'see more' signal.
    // Four sub-spaces are visible to this viewer since the invited-user rule
    // landed: Alpha, Bravo (hidden, invited), Charlie (hidden, member), Delta
    // — see testHiddenSubspaceIsListedToAnInvitedUser
    assertEquals(2, spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 0, 2).size());
    assertEquals(2, spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 2, 2).size());
    // the last page is short: the bound is the data, not the requested limit
    assertEquals(1, spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 3, 2).size());
    assertTrue(spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MEMBER, false, 0, 0).isEmpty());
  }

  /**
   * The Tech Spec's D6 says the VISIBLE list and the VISIBLE count disagree on
   * an invited user, and derives the 'see more' extra-row design from it.
   * Measured, they agree: both lose {@code extraStatus} in
   * {@code XSpaceFilter.setSpaceFilter} (see the divergence above), so both are
   * MEMBER-only. The extra-row design is kept — it cannot disagree with its own
   * query whatever the filter does — but its stated reason is pinned here as
   * what the code really does.
   */
  public void testVisibleCountAgreesWithTheVisibleListToday() {
    SpaceFilter filter = new SpaceFilter();
    filter.setParentSpaceId(parentSpace.getSpaceId());

    int listed = spaceStorage.getVisibleSpaces(PARENT_MEMBER, filter, 0, 20).size();
    int counted = spaceStorage.getVisibleSpacesCount(PARENT_MEMBER, filter);

    assertEquals(3, listed);
    assertEquals(listed, counted);
  }

  public void testListingIsRefusedToAViewerWhoCannotViewTheParent() {
    assertThrows(IllegalAccessException.class,
                 () -> spaceService.getSubspaces(parentSpace.getSpaceId(), OUTSIDER, false, 0, 20));
    assertThrows(IllegalAccessException.class,
                 () -> spaceService.getSubspaces(parentSpace.getSpaceId(), null, false, 0, 20));
    // The flag is a page setting, never a way around the parent guard
    assertThrows(IllegalAccessException.class,
                 () -> spaceService.getSubspaces(parentSpace.getSpaceId(), OUTSIDER, true, 0, 20));
  }

  public void testListingAnUnknownParentIsNotFound() {
    assertThrows(ObjectNotFoundException.class, () -> spaceService.getSubspaces(123456789L, PARENT_MANAGER, false, 0, 20));
  }

  public void testNegativeOffsetOrLimitIsARequestError() {
    assertThrows(IllegalArgumentException.class,
                 () -> spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MANAGER, false, -1, 20));
    assertThrows(IllegalArgumentException.class,
                 () -> spaceService.getSubspaces(parentSpace.getSpaceId(), PARENT_MANAGER, false, 0, -1));
  }

  public void testIsParentSpace() {
    assertTrue(spaceService.isParentSpace(parentSpace));
    assertFalse(spaceService.isParentSpace(null));

    // A sub-space is never a parent space, whatever its own template allows
    Space subspace = spaceService.getSpaceByPrettyName("delta_subspace");
    assertNotNull(subspace);
    assertFalse(spaceService.isParentSpace(subspace));

    // A top-level space whose template allows no sub-space template either
    withoutAllowedSubspaceTemplates(() -> assertFalse(spaceService.isParentSpace(parentSpace)));
  }

  public void testCannotCreateSubspaceWithoutAParentOrAUser() {
    assertFalse(spaceService.canCreateSubspace(null, PARENT_MANAGER, Locale.ENGLISH));
    assertFalse(spaceService.canCreateSubspace(parentSpace, null, Locale.ENGLISH));
    assertFalse(spaceService.canCreateSubspace(parentSpace, "", Locale.ENGLISH));
  }

  public void testCannotCreateSubspaceWhenNotAMemberOfTheParent() {
    assertFalse(spaceService.canCreateSubspace(parentSpace, OUTSIDER, Locale.ENGLISH));
  }

  public void testCannotCreateSubspaceUnderATemplateAllowingNone() {
    withoutAllowedSubspaceTemplates(() -> assertFalse(spaceService.canCreateSubspace(parentSpace,
                                                                                     PARENT_MANAGER,
                                                                                     Locale.ENGLISH)));
  }

  public void testCanCreateSubspaceAsAParentMember() {
    assertTrue(spaceService.canCreateSubspace(parentSpace, PARENT_MEMBER, Locale.ENGLISH));
  }

  public void testCannotCreateSubspaceWhenTheGlobalLimitIsReached() throws Exception {
    // Four sub-spaces exist under the parent
    parentTemplate.setSubspacesMaxLimit(4);
    spaceTemplateStorage.updateSpaceTemplate(parentTemplate);
    restartTransaction();
    try {
      assertFalse(spaceService.canCreateSubspace(parentSpace, PARENT_MEMBER, Locale.ENGLISH));

      // The count is over every sub-space, hidden ones included: the parent
      // member is in only two of the four
      parentTemplate.setSubspacesMaxLimit(5);
      spaceTemplateStorage.updateSpaceTemplate(parentTemplate);
      restartTransaction();
      assertTrue(spaceService.canCreateSubspace(parentSpace, PARENT_MEMBER, Locale.ENGLISH));
    } finally {
      parentTemplate.setSubspacesMaxLimit(0);
      spaceTemplateStorage.updateSpaceTemplate(parentTemplate);
      restartTransaction();
    }
  }

  /**
   * Verifies the one hypothesis the O7 discussion rests on, before any
   * production code is changed: that the board's invited-user rule is
   * reachable <strong>without</strong> touching shared platform code.
   * <p>
   * Today {@code getSubspaces} asks the VISIBLE access, which loses
   * {@code extraStatus} in {@code XSpaceFilter.setSpaceFilter}
   * ({@code SpaceStorage:743-744}) and so lists MEMBER-only &mdash; see
   * {@link #testHiddenSubspaceIsNotListedToAnInvitedUserBecauseExtraStatusIsDropped()}.
   * The DAO has a second branch: when the filter carries <em>no</em> status but
   * <em>does</em> carry a {@code remoteId}, the predicate binds the hardcoded
   * {@code SpaceDAO.VISIBLE_STATUSES} = {MEMBER, INVITED} to
   * {@code :visibleStatuses} ({@code SpaceDAO:495-513}). Neither that constant
   * nor {@code :userId} passes through {@code getStatusList()}, so neither can
   * be dropped by the filter copy.
   * <p>
   * This test asks both paths the same question and asserts they differ. It is
   * deliberately at the Storage layer: it measures the query, not a Service
   * that has not been changed yet. If it passes, {@code getSubspaces} can meet
   * US01.03 by setting {@code remoteId} on its filter and using ALL_FILTER, and
   * the platform defect in {@code setSpaceFilter} becomes a separate ticket. If
   * it fails, the only remaining route is the platform fix (O7 option 1) and
   * this test must be deleted rather than weakened.
   */
  public void testAllFilterPathWithARemoteIdHonoursTheInvitedUser() throws Exception {
    // A hidden sub-space the viewer has no membership row on at all: the
    // control that tells "honours INVITED" apart from "ignores visibility"
    Space hiddenToTheViewer = saveSpace("Hotel subspace", Space.HIDDEN, parentSpace.getSpaceId(), PARENT_MANAGER);
    restartTransaction();

    SpaceFilter visibleFilter = new SpaceFilter();
    visibleFilter.setParentSpaceId(parentSpace.getSpaceId());
    List<String> throughVisibleAccess = names(spaceStorage.getVisibleSpaces(PARENT_MEMBER, visibleFilter, 0, 20));

    SpaceFilter remoteIdFilter = new SpaceFilter();
    remoteIdFilter.setParentSpaceId(parentSpace.getSpaceId());
    remoteIdFilter.setRemoteId(PARENT_MEMBER);
    List<String> throughAllFilter = names(spaceStorage.getSpacesByFilter(remoteIdFilter, 0, 20));

    // Bravo is HIDDEN and PARENT_MEMBER is only invited to it
    assertFalse("the VISIBLE access is expected to still lose the invited user",
                throughVisibleAccess.contains("Bravo subspace"));
    assertTrue("the remoteId path must list the hidden sub-space the viewer is invited to",
               throughAllFilter.contains("Bravo subspace"));

    // ...and it must not have simply stopped filtering on visibility
    assertFalse("a hidden sub-space the viewer has no membership on must stay hidden",
                throughAllFilter.contains(hiddenToTheViewer.getDisplayName()));

    // ...nor widened the parent scope, nor lost what already worked
    assertFalse("the listing must stay scoped to this parent", throughAllFilter.contains("Echo subspace"));
    assertTrue(throughAllFilter.contains("Alpha subspace"));
    assertTrue(throughAllFilter.contains("Charlie subspace"));
    assertTrue(throughAllFilter.contains("Delta subspace"));

    // The board rule, stated as the whole expected answer for this viewer
    assertEquals(List.of("Alpha subspace", "Bravo subspace", "Charlie subspace", "Delta subspace"), throughAllFilter);
  }

  private List<String> names(List<Space> spaces) {
    return spaces.stream().map(Space::getDisplayName).toList();
  }

  /**
   * Templates are written through the Kernel DAO rather than the storage: in
   * this container the storage's Spring Data repository does not commit the row
   * before the space inserts that carry a foreign key to it
   * (FK_SOC_SPACE_TEMPLATES_ID_01). Same workaround as SpaceServiceTest.
   */
  /**
   * The container replaces {@code SpaceTemplateStorage} with a mock holding one
   * single template, so the fixture configures that template rather than
   * creating its own: a row written through the DAO is simply not visible to
   * the Service. A database row is still needed, because SOC_SPACES carries a
   * foreign key to SOC_SPACE_TEMPLATES.
   */
  @SneakyThrows
  private SpaceTemplate useMockTemplateAsParentTemplate() {
    SpaceTemplate template = spaceTemplateStorage.getSpaceTemplates(Pageable.unpaged()).getFirst();
    SpaceTemplateDAO dao = new SpaceTemplateDAO();
    if (dao.find(template.getId()) == null) {
      SpaceTemplateEntity entity = EntityMapper.toEntity(template);
      entity.setId(null);
      while (dao.create(entity).getId() < template.getId()) {
        entity = EntityMapper.toEntity(template);
        entity.setId(null);
      }
      restartTransaction();
    }
    // a template that allows sub-spaces of its own kind: one template is all
    // the mock can hold, and the rule under test is "allows at least one"
    template.setAllowedSubspaceTemplates(List.of(template.getId() + ":0"));
    template.setPermissions(List.of("Everyone"));
    template.setSubspacesMaxLimit(0);
    return spaceTemplateStorage.updateSpaceTemplate(template);
  }

  @SneakyThrows
  private void withoutAllowedSubspaceTemplates(Runnable assertion) {
    List<String> allowed = parentTemplate.getAllowedSubspaceTemplates();
    parentTemplate.setAllowedSubspaceTemplates(null);
    spaceTemplateStorage.updateSpaceTemplate(parentTemplate);
    try {
      assertion.run();
    } finally {
      parentTemplate.setAllowedSubspaceTemplates(allowed);
      spaceTemplateStorage.updateSpaceTemplate(parentTemplate);
    }
  }

  public static class SpaceTemplateDAO extends GenericDAOJPAImpl<SpaceTemplateEntity, Long> {
  }

  private Space saveSpace(String displayName, String visibility, Long parentSpaceId, String... members) {
    Space space = new Space();
    space.setDisplayName(displayName);
    space.setPrettyName(displayName.replace(' ', '_').toLowerCase());
    space.setRegistration(Space.OPEN);
    space.setDescription(displayName);
    space.setVisibility(visibility);
    space.setTemplateId(parentTemplate.getId());
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setUrl(space.getPrettyName());
    space.setManagers(new String[] { members[0] });
    space.setMembers(members);
    space.setInvitedUsers(new String[] {});
    space.setPendingUsers(new String[] {});
    return spaceStorage.saveSpace(space, parentSpaceId, true);
  }
}
