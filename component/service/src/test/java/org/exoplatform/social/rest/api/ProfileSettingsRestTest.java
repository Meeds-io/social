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
package org.exoplatform.social.rest.api;

import java.util.List;
import java.util.Map;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.service.test.AbstractResourceTest;

import io.meeds.social.core.profileproperty.util.ProfilePropertyValueUtil;

public class ProfileSettingsRestTest extends AbstractResourceTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    registry(new ProfileSettingsRest(getContainer().getComponentInstanceOfType(ProfilePropertyService.class)));
  }

  @Override
  public void tearDown() throws Exception {
    removeResource(ProfileSettingsRest.class);
    super.tearDown();
  }

  /**
   * The attribute form disables both listing switches on these names, read from
   * the server's answer rather than from a copy of its own.
   */
  @SuppressWarnings("unchecked")
  public void testTheSettingsAnswerCarriesTheNamesNoFormLists() throws Exception {
    startSessionAs("root");

    ContainerResponse response = service("GET", getURLResource("profile/settings"), "", null, null);

    assertEquals(200, response.getStatus());
    Map<String, List<?>> answer = (Map<String, List<?>>) response.getEntity();
    assertEquals(ProfilePropertyValueUtil.UNLISTABLE_NAMES, answer.get("unlistableProperties"));
  }

}
