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
package io.meeds.social.identity.permission.entity;

import static org.junit.Assert.assertThrows;

import java.sql.SQLException;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;

import io.meeds.social.core.profileproperty.util.ProfilePropertyValueUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

/**
 * The group id a permission row holds is bounded by its column,
 * SOC_USER_PERMISSION.GROUP_ID, on the test database, HSQLDB: an id of
 * {@link ProfilePropertyValueUtil#MAX_GROUP_ID_LENGTH} characters is stored,
 * one character more is refused by the engine at the flush.
 */
public class UserPermissionEntityContainerTest extends AbstractCoreTest {

  /** SQLSTATE of a string longer than its column */
  private static final String STRING_DATA_RIGHT_TRUNCATION = "22001";

  public void testAGroupIdOfTheBoundIsStoredAndOneCharacterMoreIsRefused() throws Exception {
    Identity identity = createUserAndIdentity("permissionBoundUser");
    String groupId = "/" + "g".repeat(ProfilePropertyValueUtil.MAX_GROUP_ID_LENGTH - 1);
    EntityManager entityManager = getService(EntityManagerService.class).getEntityManager();

    UserPermissionEntity stored = permission(identity, groupId);
    entityManager.getTransaction().begin();
    entityManager.persist(stored);
    entityManager.flush();
    entityManager.getTransaction().commit();
    entityManager.clear();
    try {
      assertEquals(groupId, entityManager.find(UserPermissionEntity.class, stored.getId()).getGroupId());

      UserPermissionEntity refused = permission(identity, groupId + "g");
      entityManager.getTransaction().begin();
      try {
        entityManager.persist(refused);
        PersistenceException exception = assertThrows(PersistenceException.class, entityManager::flush);
        assertEquals(STRING_DATA_RIGHT_TRUNCATION, sqlState(exception));
      } finally {
        entityManager.getTransaction().rollback();
        entityManager.clear();
      }
    } finally {
      entityManager.getTransaction().begin();
      entityManager.remove(entityManager.find(UserPermissionEntity.class, stored.getId()));
      entityManager.getTransaction().commit();
    }
  }

  private UserPermissionEntity permission(Identity identity, String groupId) {
    UserPermissionEntity entity = new UserPermissionEntity();
    entity.setIdentityId(Long.parseLong(identity.getId()));
    entity.setUserName(identity.getRemoteId());
    entity.setGroupId(groupId);
    entity.setMembershipType("member");
    entity.setInherited(false);
    return entity;
  }

  private String sqlState(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause instanceof SQLException sqlException) {
        return sqlException.getSQLState();
      }
    }
    return null;
  }

}
