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
package org.exoplatform.social.core.service;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class LinkProviderTest extends AbstractCoreTest { // NOSONAR

  public void testGetProfileLink() {
    final String portalOwner = "classic";

    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    assertNotNull(rootIdentity);
    assertNotNull(rootIdentity.getProfile());
    String rootFullName = rootIdentity.getProfile().getFullName();
    assertNotNull("rootFullName must not be null.", rootFullName);

    // but when we have the identity we generate a link
    String actualLink = LinkProvider.getProfileLink(rootIdentity.getRemoteId(), portalOwner);
    Object external = rootIdentity.getProfile().getProperty(Profile.EXTERNAL);
    String expected = "<a class=\"user-suggester\" href=\"http://localhost:8080/portal/" + portalOwner
        + "/profile/" +
        rootIdentity.getRemoteId() + "\" "
        + "target=\"_parent\" v-identity-popover=\"{id: '" + rootIdentity.getId() + "',username: '" + rootIdentity.getRemoteId()
        + "',fullName: '"
        + rootIdentity.getProfile().getFullName() + "',avatar: '" + rootIdentity.getProfile().getAvatarUrl().replace("&", "&amp;") + "',position: '"
        + StringUtils.trimToEmpty(rootIdentity.getProfile().getPosition()) + "',external: '"
        + (external == null ? "false" : external)
        + "',enabled: '" + (rootIdentity.isEnable() && !rootIdentity.isDeleted())
        + "',deleted: '" + rootIdentity.isDeleted()
        + "',displayedEmail: '" + Objects.toString(rootIdentity.getProfile().getProperty(Profile.DISPLAYED_EMAIL), "")
        + "',displayedPhone: '" + Objects.toString(rootIdentity.getProfile().getProperty(Profile.DISPLAYED_PHONE), "")
        + "',}\">" + rootFullName + "</a>";
    assertEquals(expected, actualLink);
  }

  public void testGetProfileLinkEscapesEveryPopoverValue() {
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    Profile profile = johnIdentity.getProfile();
    profile.setProperty(Profile.DISPLAYED_EMAIL, "o'hara\"@example.com");
    profile.setProperty(Profile.DISPLAYED_PHONE, "+33 $1 \\ 42");
    identityManager.updateProfile(profile);
    try {
      String link = LinkProvider.getProfileLink("john", "classic");

      assertTrue(link, link.contains("displayedEmail: 'o\\'hara&quot;@example.com'"));
      assertTrue(link, link.contains("displayedPhone: '+33 $1 \\\\ 42'"));
    } finally {
      profile.setProperty(Profile.DISPLAYED_EMAIL, null);
      profile.setProperty(Profile.DISPLAYED_PHONE, null);
      identityManager.updateProfile(profile);
    }
  }

  public void testToPopoverValueRoundTripsThroughTheAttributeAndTheJsString() {
    String value = "O'Brien \"the\" \\ <b>&amp;</b>\nnext\u2028line";

    String escaped = LinkProvider.toPopoverValue(value);

    assertFalse(escaped, escaped.matches(".*(?<!\\\\)'.*"));
    assertFalse(escaped, escaped.contains("\""));
    assertFalse(escaped, escaped.contains("<"));
    assertEquals(value, StringEscapeUtils.unescapeEcmaScript(StringEscapeUtils.unescapeHtml4(escaped)));
    assertEquals("", LinkProvider.toPopoverValue(null));
  }
}
