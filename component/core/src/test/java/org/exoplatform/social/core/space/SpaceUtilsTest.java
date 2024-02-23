/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.space;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.EntityConverterUtils;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.gatein.pc.api.*;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.mockito.Mockito;

import org.exoplatform.application.registry.Application;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.util.*;

import static org.junit.Assert.assertNotEquals;

/**
 * Unit Test for {@link SpaceUtilsTest}
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jan 27, 2011
 * @since 1.2.0-GA
 */
public class SpaceUtilsTest extends AbstractCoreTest {

  private static final Log LOG = ExoLogger.getLogger(SpaceUtilsTest.class);

  private List<Space> tearDown = new ArrayList<Space>();

  private org.exoplatform.services.security.Identity identity;
  private Identity rootIdentity;
  private IdentityManager identityManager;
  private IdentityStorage identityStorage;
  private PageService pageService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Collection<String> roles = Collections.emptySet();
    Set<MembershipEntry> memberships = new HashSet<MembershipEntry>();
    identity = new org.exoplatform.services.security.Identity("root", memberships, roles);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    String spaceDisplayName = "Space1";
    Space space1 = new Space();
    space1.setApp("Members:true,Contact:true");
    space1.setDisplayName(spaceDisplayName);
    space1.setPrettyName(space1.getDisplayName());
    String shortName = Utils.cleanString(spaceDisplayName);
    space1.setGroupId("/spaces/" + shortName);
    space1.setUrl(shortName);
    space1.setRegistration("validation");
    space1.setDescription("This is my first space for testing");
    space1.setType("classic");
    space1.setVisibility("public");
    space1.setPriority("2");
    String[] manager = new String []{"root"};
    String[] members = new String []{"root", "demo", "john", "mary"};
    space1.setManagers(manager);
    space1.setMembers(members);

    spaceService.createSpace(space1, "root");
    tearDown.add(space1);
  }

  @Override
  protected  void tearDown() throws Exception {
    for(Space space : tearDown) {
      spaceService.deleteSpace(space);
    }
    identityManager.deleteIdentity(rootIdentity);
    super.tearDown();
  }

  public void testGetDisplayAppName() throws Exception {

    final String inputPortlet = "ABCPortlet";
    final String outputPortlet = "ABC";

    final String inputPortlet2 = "hello world portlet";
    final String outputPortlet2 = "hello world";

    assertEquals(outputPortlet, SpaceUtils.getDisplayAppName(inputPortlet));
    assertEquals(outputPortlet2, SpaceUtils.getDisplayAppName(inputPortlet2));

  }
  /**
   * Test {@link SpaceUtils#removeSpecialCharacterInSpaceFilter(String)}
   * @throws Exception
   * @since 1.2.2
   */
  public void testRemoveSpecialCharacter() {
    assertEquals("The filter should only filter special characters only","script alert 'Hello' script 100", Utils.removeSpecialCharacterInSpaceFilter("<script>alert('Hello');</script> 100"));
    assertEquals("The filter should keep wildcard *,? and %","% * '?", Utils.removeSpecialCharacterInSpaceFilter("( ) %{ } * [ ] \'? \""));
  }
  
  public void testGetAppFromPortalContainer() throws Exception {
    String appId = "appId";
    String appName = "app";
    String contentId = appName + "/" + appId;

    PortletInvoker portletInvoker = Mockito.mock(PortletInvoker.class);
    Portlet portlet = Mockito.mock(Portlet.class);
    PortletContext portletContext = Mockito.mock(PortletContext.class);
    PortletInfo portletInfo = Mockito.mock(PortletInfo.class);
    MetaInfo metaInfo = Mockito.mock(MetaInfo.class);

    Mockito.when(portletContext.getId()).thenReturn(contentId);
    Mockito.when(portletInfo.getApplicationName()).thenReturn(appName);
    Mockito.when(portletInfo.getName()).thenReturn(contentId);
    Mockito.when(portletInfo.getMeta()).thenReturn(metaInfo);
    Mockito.when(portlet.getContext()).thenReturn(portletContext);
    Mockito.when(portlet.getInfo()).thenReturn(portletInfo);

    Mockito.when(portletInvoker.getPortlets()).thenReturn(Collections.singleton(portlet));

    ExoContainerContext.getCurrentContainer().registerComponentInstance(PortletInvoker.class, portletInvoker);
    Application application = SpaceUtils.getAppFromPortalContainer("appId");
    assertNull(application);

    application = SpaceUtils.getAppFromPortalContainer(contentId.replace('/', '_'));
    assertNotNull(application);
  }
  
  public void testIsInstalledApp() {
    Space space = new Space();
    String apps = "ForumPortlet:Forums:true:active,WikiPortlet:Wiki:true:active,FileExplorerPortlet:Documents:true:active,"
                + "CalendarPortlet:Agenda:true:active,SpaceSettingPortlet:Space Settings:false:active,"
                + "AnswersPortlet:Answer:true:active,FAQPortlet:FAQ:true:active,MembersPortlet:Members:true:active";
    space.setApp(apps);
    boolean isInstalledApp = SpaceUtils.isInstalledApp(space, "Agenda");
    assertTrue(isInstalledApp);
    isInstalledApp = SpaceUtils.isInstalledApp(space, "CalendarPortlet");
    assertTrue(isInstalledApp);
  }
 
  public void testProcessUnifiedSearchCondition() {
    String input = "spa~ce~0.5";
    assertEquals("spa~ce", Utils.processUnifiedSearchCondition(input));
    input = "space~0.5";
    assertEquals("space", Utils.processUnifiedSearchCondition(input));
    input = "space~0.5 test~0.5";
    assertEquals("space test", Utils.processUnifiedSearchCondition(input));
  }

  public void testchangeAppPageTitle() throws Exception {
    Space space1 = tearDown.get(0);

    ConversationState.setCurrent(new ConversationState(identity));
    List<UserNode> childNodes = SpaceUtils.getSpaceUserNodeChildren(space1);

    PageService pageService = (PageService) getContainer().getComponentInstanceOfType(PageService.class);
    PageContext pc = null;
    String AppName ="";

    for (UserNode childNode : childNodes)
    {
      pc = pageService.loadPage(childNode.getPageRef());
      AppName = pc.getState().getDisplayName().split("-")[0].trim();

      assertEquals("Space1", AppName);
      SpaceUtils.changeAppPageTitle(childNode,"newspacetitle");
      pc = pageService.loadPage(childNode.getPageRef());
      AppName = pc.getState().getDisplayName().split("-")[0].trim();

      assertEquals("newspacetitle", AppName);
    }

  }

  public void testUpdateDefaultSpaceAvatar() throws Exception {
    Space space = tearDown.get(0);
    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(identity);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FULL_NAME, space.getDisplayName());
    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    String identityId = identity.getId();
    assertNotNull(identityId);
    FileItem avatarFile = identityStorage.getAvatarFile(identity);
    profile = identityStorage.loadProfile(profile);
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    assertTrue(profile.isDefaultAvatar());

    Long avatarFileId = avatarFile.getFileInfo().getId();
    assertNotNull(avatarFileId);

    profile.setProperty("test", "test");
    identityStorage.updateProfile(profile);

    profile = identityStorage.loadProfile(profile);
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    assertTrue(profile.isDefaultAvatar());

    assertEquals("Avatar File shouldn't change if Space not renamed", avatarFileId, avatarFile.getFileInfo().getId());

    // Rename space
    String JOHN = "john";
    String newSpaceName = "newname";
    spaceService.renameSpace(JOHN, space, newSpaceName);
    space = spaceService.getSpaceById(space.getId());
    assertEquals(newSpaceName, space.getDisplayName());

    SpaceUtils.updateDefaultSpaceAvatar(space);
    identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getDisplayName());
    avatarFile = identityManager.getAvatarFile(identity);
    Long updatedAvatarId = avatarFile.getFileInfo().getId();
    assertNotNull(updatedAvatarId);
    assertNotEquals(avatarFileId, updatedAvatarId);
  }

  public void testUpdateDefaultUserAvatar() throws Exception {
    FileItem avatarFile = identityStorage.getAvatarFile(rootIdentity);
    Profile profile = identityStorage.loadProfile(rootIdentity.getProfile());
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    assertTrue(profile.isDefaultAvatar());

    Long avatarFileId = avatarFile.getFileInfo().getId();
    assertNotNull(avatarFileId);

    profile.setProperty("test", "test");
    identityStorage.updateProfile(profile);

    profile = identityStorage.loadProfile(profile);
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    assertTrue(profile.isDefaultAvatar());

    assertEquals("Avatar File shouldn't change if User not renamed", avatarFileId, avatarFile.getFileInfo().getId());
  }

  public void testIsRedactor() throws Exception {
    String JOHN = "john";
    String DEMO = "demo";
    Space space = tearDown.get(0);
    // No redactor in /spaces/space1 -> john is redactor
    try {
      assertTrue(SpaceUtils.isRedactor(JOHN, space.getGroupId()));
    } catch (Exception e) {
      LOG.error("Problem executing Test", e);
      fail();
    }
    // Add another redactor in group -> john is no more redactor
    spaceService.addRedactor(space, DEMO);
    assertFalse(SpaceUtils.isRedactor(JOHN, space.getGroupId()));

    // add the user with membership redactor -> john is redactor
    spaceService.addRedactor(space, JOHN);
    assertTrue(SpaceUtils.isRedactor(JOHN, space.getGroupId()));
  }

  public void testIsSpaceManagerOrSuperManager() throws Exception {
    String JOHN = "john";
    Space space = tearDown.get(0);
    spaceService.setManager(space, JOHN, true);
    try {
      assertTrue(SpaceUtils.isSpaceManagerOrSuperManager(JOHN, space.getGroupId()));
    } catch (Exception e) {
      LOG.error("Problem executing Test", e);
      fail();
    }
    // john is no more manager
    spaceService.setManager(space, JOHN, false);
    assertFalse(SpaceUtils.isSpaceManagerOrSuperManager(JOHN, space.getGroupId()));
    // root is super manager
    assertTrue(SpaceUtils.isSpaceManagerOrSuperManager("root", space.getGroupId()));
  }
}
