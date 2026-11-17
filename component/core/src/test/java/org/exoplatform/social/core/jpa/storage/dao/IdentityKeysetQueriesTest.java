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
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;

/**
 * The keyset queries the profile property holders reindex and the profile
 * index upgrade walk, run by the test database's engine. Built on the base
 * test case that invokes the test method: {@code BaseCoreTest#runTest} does
 * not.
 */
public class IdentityKeysetQueriesTest extends AbstractCoreTest {

  private IdentityDAO          identityDAO;

  private List<IdentityEntity> deleteIdentities;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityDAO = getService(IdentityDAO.class);
    deleteIdentities = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    for (IdentityEntity identity : deleteIdentities) {
      // the managed entity, whose properties collection is removed with it
      IdentityEntity stored = identityDAO.find(identity.getId());
      if (stored != null) {
        stored.getProperties().clear();
        identityDAO.update(stored);
        identityDAO.delete(identityDAO.find(identity.getId()));
      }
    }
    super.tearDown();
  }

  public void testGetDisabledIdsByProviderAfterId() {
    IdentityEntity enabledUser = identityDAO.create(createIdentityEntity(OrganizationIdentityProvider.NAME, "disabledKeyset1"));
    IdentityEntity disabledUser1 = createIdentityEntity(OrganizationIdentityProvider.NAME, "disabledKeyset2");
    disabledUser1.setEnabled(false);
    disabledUser1 = identityDAO.create(disabledUser1);
    IdentityEntity disabledUser2 = createIdentityEntity(OrganizationIdentityProvider.NAME, "disabledKeyset3");
    disabledUser2.setEnabled(false);
    disabledUser2 = identityDAO.create(disabledUser2);
    IdentityEntity disabledSpace = createIdentityEntity(SpaceIdentityProvider.NAME, "disabledKeysetSpace");
    disabledSpace.setEnabled(false);
    disabledSpace = identityDAO.create(disabledSpace);

    // from just before the enabled user, who must not be listed
    List<Long> ids = identityDAO.getDisabledIdsByProviderAfterId(OrganizationIdentityProvider.NAME, enabledUser.getId() - 1, 0);
    assertEquals(List.of(disabledUser1.getId(), disabledUser2.getId()), ids);
    assertEquals(List.of(disabledUser1.getId()),
                 identityDAO.getDisabledIdsByProviderAfterId(OrganizationIdentityProvider.NAME, enabledUser.getId() - 1, 1));
    assertEquals(List.of(disabledUser2.getId()),
                 identityDAO.getDisabledIdsByProviderAfterId(OrganizationIdentityProvider.NAME, disabledUser1.getId(), 0));

    deleteIdentities.add(enabledUser);
    deleteIdentities.add(disabledUser1);
    deleteIdentities.add(disabledUser2);
    deleteIdentities.add(disabledSpace);
  }

  public void testGetIdsHoldingPropertyAfterId() {
    IdentityEntity holder1 = createIdentityEntity(OrganizationIdentityProvider.NAME, "propertyHolder1");
    holder1.getProperties().put("keyset.department", "HR");
    holder1 = identityDAO.create(holder1);
    IdentityEntity holder2 = createIdentityEntity(OrganizationIdentityProvider.NAME, "propertyHolder2");
    holder2.getProperties().put("keyset.department", "[{\"value\":\"HR\"},{\"value\":\"IT\"}]");
    holder2.setEnabled(false);
    holder2 = identityDAO.create(holder2);
    IdentityEntity otherProperty = createIdentityEntity(OrganizationIdentityProvider.NAME, "propertyHolder3");
    otherProperty.getProperties().put("keyset.city", "Tunis");
    otherProperty = identityDAO.create(otherProperty);
    IdentityEntity spaceHolder = createIdentityEntity(SpaceIdentityProvider.NAME, "propertyHolderSpace");
    spaceHolder.getProperties().put("keyset.department", "HR");
    spaceHolder = identityDAO.create(spaceHolder);

    assertEquals(List.of(holder1.getId(), holder2.getId()),
                 identityDAO.getIdsHoldingPropertyAfterId(OrganizationIdentityProvider.NAME, "keyset.department", 0, 0));
    assertEquals(List.of(holder1.getId()),
                 identityDAO.getIdsHoldingPropertyAfterId(OrganizationIdentityProvider.NAME, "keyset.department", 0, 1));
    assertEquals(List.of(holder2.getId()),
                 identityDAO.getIdsHoldingPropertyAfterId(OrganizationIdentityProvider.NAME,
                                                          "keyset.department",
                                                          holder1.getId(),
                                                          0));
    assertEquals(List.of(), identityDAO.getIdsHoldingPropertyAfterId(OrganizationIdentityProvider.NAME, "keyset.unknown", 0, 0));

    deleteIdentities.add(holder1);
    deleteIdentities.add(holder2);
    deleteIdentities.add(otherProperty);
    deleteIdentities.add(spaceHolder);
  }

  private IdentityEntity createIdentityEntity(String providerId, String remoteId) {
    IdentityEntity entity = new IdentityEntity();
    entity.setProviderId(providerId);
    entity.setRemoteId(remoteId);
    entity.setEnabled(true);
    entity.setDeleted(false);
    return entity;
  }
}
