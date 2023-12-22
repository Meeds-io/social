/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.upgrade;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradePluginExecutionContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;
import org.exoplatform.web.security.hash.Argon2IdPasswordEncoder;
import org.exoplatform.web.security.security.SecureRandomService;
import org.gatein.portal.idm.impl.store.attribute.ExtendedAttributeManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.PersistenceManager;
import org.picketlink.idm.api.User;
import org.picketlink.idm.impl.api.model.SimpleUser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserPasswordHashMigrationTest {

  @Mock
  private EntityManagerService      entityManagerService;

  @Mock
  private PicketLinkIDMService      picketLinkIDMService;

  private UserPasswordHashMigration userPasswordHashMigration;

  @Before
  public void setUp() {
    InitParams initParams = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("product.group.id");
    valueParam.setValue("org.exoplatform.platform");
    ValueParam valueParamVersion = new ValueParam();
    valueParamVersion.setName("plugin.upgrade.target.version");
    valueParamVersion.setValue("6.5.0");
    ValueParam oldAppNamevalueParam = new ValueParam();
    oldAppNamevalueParam.setName("plugin.execution.order");
    oldAppNamevalueParam.setValue("100");
    ValueParam oldAppIdvalueParam = new ValueParam();
    oldAppIdvalueParam.setName("plugin.upgrade.execute.once");
    oldAppIdvalueParam.setValue("true");
    ValueParam newAppIdvalueParam = new ValueParam();
    newAppIdvalueParam.setName("plugin.upgrade.async.execution");
    newAppIdvalueParam.setValue("true");
    initParams.addParameter(valueParam);
    initParams.addParameter(valueParamVersion);
    initParams.addParameter(oldAppNamevalueParam);
    initParams.addParameter(oldAppIdvalueParam);
    initParams.addParameter(newAppIdvalueParam);
    userPasswordHashMigration = new UserPasswordHashMigration(entityManagerService,
                                                              picketLinkIDMService,
                                                              initParams);
  }

  @Test
  public void tesProcessUpgrade() throws Exception {


    List<Object[]> result = new ArrayList<>();
    result.add(new String[] { "user", "passwordHash" });
    EntityManager entityManager = mock(EntityManager.class);
    Query query = mock(Query.class);
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.getResultList()).thenReturn(result);
    when(entityManagerService.getEntityManager()).thenReturn(entityManager);

    IdentitySession identitySession = mock(IdentitySession.class);
    PersistenceManager persistenceManager = mock(PersistenceManager.class);
    User user = new SimpleUser("userId");
    when(persistenceManager.findUser(anyString())).thenReturn(user);
    Argon2IdPasswordEncoder encoder = mock(Argon2IdPasswordEncoder.class);
    when(encoder.generateRandomSalt()).thenReturn("testSalt".getBytes());
    ExtendedAttributeManager extendedAttributeManager = mock(ExtendedAttributeManager.class);
    when(extendedAttributeManager.getDefaultCredentialEncoder()).thenReturn(encoder);
    when(identitySession.getPersistenceManager()).thenReturn(persistenceManager);
    when(picketLinkIDMService.getIdentitySession()).thenReturn(identitySession);
    when(picketLinkIDMService.getExtendedAttributeManager()).thenReturn(extendedAttributeManager);
    userPasswordHashMigration.processUpgrade(null, null);
    verify(extendedAttributeManager, times(1)).updatePassword(any(), anyString());
    verify(extendedAttributeManager, times(1)).addAttribute(anyString(), anyString(), anyString());

    when(extendedAttributeManager.getDefaultCredentialEncoder()).thenReturn(new Argon2IdPasswordEncoder());
    boolean proceedToUpgrade = userPasswordHashMigration.shouldProceedToUpgrade(null, null, null);
    assertFalse(proceedToUpgrade);

    UpgradePluginExecutionContext upgradePluginExecutionContext = new UpgradePluginExecutionContext("6.4.0", 0);
    proceedToUpgrade = userPasswordHashMigration.shouldProceedToUpgrade("6.5.0", "6.4.0", upgradePluginExecutionContext);
    assertTrue(proceedToUpgrade);
    // Case of exceptions existing during upgrade
    doThrow(new RuntimeException()).when(picketLinkIDMService).getIdentitySession();
    Throwable exception = assertThrows(IllegalStateException.class, () -> userPasswordHashMigration.processUpgrade(null, null));
    assertEquals("UserPasswordHashMigration upgrade failed due to previous errors", exception.getMessage());
  }
}
