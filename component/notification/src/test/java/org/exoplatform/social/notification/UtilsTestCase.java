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
package org.exoplatform.social.notification;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import org.mockito.MockedStatic;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.core.identity.plugin.AccountKindPlugin;
import io.meeds.social.core.identity.service.AccountKindService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class UtilsTestCase extends AbstractCoreTest {

  Space space ;
  @Override
  public void setUp() throws Exception {
    super.setUp();
    space = new Space();
    space.setDisplayName("my space");
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("new space ");
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setEditor(rootIdentity.getRemoteId());
    space.setAvatarLastUpdated(System.currentTimeMillis());
    Space createdSpace = spaceService.createSpace(space, rootIdentity.getRemoteId());

    Arrays.asList(rootIdentity.getRemoteId(), demoIdentity.getRemoteId(), johnIdentity.getRemoteId())
          .forEach(u -> spaceService.addMember(createdSpace, u));

    tearDownSpaceList.add(createdSpace);
    System.setProperty(CommonsUtils.CONFIGURED_DOMAIN_URL_KEY, "http://test.com");
  }

  public void testProcessLinkInActivityTitle() {
    String title = "<a href=\"www.yahoo.com\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\">Hotmail Site</a>";
    title = Utils.processLinkTitle(title);
    assertEquals("<a href=\"www.yahoo.com\" style=\"color: #2f5e92; text-decoration: none;\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\" style=\"color: #2f5e92; text-decoration: none;\">Hotmail Site</a>", title);
    
    title = "Shared a document <a href=\"portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\">New design. eXo in Smart Watch.jpg</a>";
    assertEquals("Shared a document <a href=\"http://test.com/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\" style=\"color: #2f5e92; text-decoration: none;\">New design. eXo in Smart Watch.jpg</a>", Utils.processLinkTitle(title));
    title = "Shared a document <a href=\"/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\">New design. eXo in Smart Watch.jpg</a>";
    assertEquals("Shared a document <a href=\"http://test.com/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\" style=\"color: #2f5e92; text-decoration: none;\">New design. eXo in Smart Watch.jpg</a>", Utils.processLinkTitle(title));
  }

  /**
   * The name of an account of a registered kind carries the kind's label in a
   * notification, a guest's the guest label, and anyone else's, an account of
   * a kind no plugin declares included, no label. The labels are stubbed: the
   * test container serves no shared resource bundle.
   */
  public void testAddExternalFlagNamesAnAccountOfKindAndAGuest() {
    AccountKindService accountKindService = getContainer().getComponentInstanceOfType(AccountKindService.class);
    accountKindService.register(new NotificationTestAccountKindPlugin());
    try (MockedStatic<LinkProvider> linkProvider = mockStatic(LinkProvider.class, CALLS_REAL_METHODS)) {
      linkProvider.when(() -> LinkProvider.getResourceBundleLabel(any(Locale.class), eq("accountKind.label.bot"))).thenReturn("Bot");
      linkProvider.when(() -> LinkProvider.getResourceBundleLabel(any(Locale.class), eq("external.label.tag"))).thenReturn("Guest");
      linkProvider.when(() -> LinkProvider.getCurrentUserLanguage(anyString())).thenReturn("en");
      assertEquals("Bot Smith (Bot)", Utils.addExternalFlag(userIdentity("Bot Smith", Profile.ACCOUNT_KIND, "bot")));
      assertEquals("Guest Smith (Guest)", Utils.addExternalFlag(userIdentity("Guest Smith", Profile.EXTERNAL, "true")));
      assertEquals("Other Smith", Utils.addExternalFlag(userIdentity("Other Smith", Profile.ACCOUNT_KIND, "unregistered")));
      assertEquals("Plain Smith", Utils.addExternalFlag(userIdentity("Plain Smith", null, null)));
    }
  }

  /**
   * An identity of the demo user, held in memory only, with the given full
   * name and, when given, one profile property.
   *
   * @param fullName the full name
   * @param property the profile property to set, or null for none
   * @param value the property value
   * @return the identity
   */
  private Identity userIdentity(String fullName, String property, String value) {
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, "demo");
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FULL_NAME, fullName);
    if (property != null) {
      profile.setProperty(property, value);
    }
    identity.setProfile(profile);
    return identity;
  }

  /**
   * The {@code bot} account kind of the members of {@code /platform/bots}.
   */
  private static class NotificationTestAccountKindPlugin implements AccountKindPlugin {

    /**
     * @return the {@code bot} kind
     */
    @Override
    public String getKind() {
      return "bot";
    }

    /**
     * @return the bots group
     */
    @Override
    public String getGroupId() {
      return "/platform/bots";
    }
  }

  public void testGetMentioners() {
    String mentionTitle ="<a href=\"/portal/dw/profile/demo\"> " + demoIdentity.getProfile().getFullName() + " </a>";
    Set<String> receivers = Utils.getMentioners(mentionTitle, rootIdentity.getId(), space.getId());
    assertEquals(1, receivers.size());
    mentionTitle ="<a href=\"/portal/dw/profile/root\"> "+ rootIdentity.getProfile().getFullName() + " </a>";
    receivers = Utils.getMentioners(mentionTitle, rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());
    mentionTitle ="<a href=\"/portal/dw/profile/ghost\"> "+ ghostIdentity.getProfile().getFullName() + " </a>";
    receivers = Utils.getMentioners(mentionTitle, rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());
  }


  public void testSendToCommenters() {
    Set<String> receivers = new HashSet<>();
    String[] commenters = new String[] { demoIdentity.getId()};
    Utils.sendToCommeters(receivers, commenters, rootIdentity.getId(), space.getId());
    assertEquals(1, receivers.size());

    receivers = new HashSet<>();
    commenters = new String[] { rootIdentity.getId()};
    Utils.sendToCommeters(receivers, commenters, rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());

    receivers = new HashSet<>();
    commenters = new String[] {ghostIdentity.getId()};
    Utils.sendToCommeters(receivers, commenters, rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());
  }

  public void testSendToActivityPoster() {
    Set<String> receivers = new HashSet<>();
    Utils.sendToActivityPoster(receivers, demoIdentity.getId(), rootIdentity.getId(), space.getId());
    assertEquals(1, receivers.size());

    receivers = new HashSet<>();
    Utils.sendToActivityPoster(receivers, rootIdentity.getId(), rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());

    receivers = new HashSet<>();
    Utils.sendToActivityPoster(receivers, ghostIdentity.getId(), rootIdentity.getId(), space.getId());
    assertEquals(0, receivers.size());
  }
}
