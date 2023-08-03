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

import org.apache.commons.codec.binary.Hex;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradePluginExecutionContext;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;
import org.exoplatform.web.security.security.SecureRandomService;
import org.picketlink.idm.api.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserPasswordHashMigration extends UpgradeProductPlugin {

  private static final Log           LOG                          = ExoLogger.getExoLogger(UserPasswordHashMigration.class);

  private final EntityManagerService entityManagerService;

  private final PicketLinkIDMService picketLinkIDMService;

  private final SecureRandomService  secureRandomService;

  private static final String        PASSWORD_SALT_USER_ATTRIBUTE = "passwordSalt128";

  private static final String        DEFAULT_ENCODER              = "org.exoplatform.web.security.hash.Argon2IdPasswordEncoder";

  private boolean                    hasErrors                    = false;

  public UserPasswordHashMigration(EntityManagerService entityManagerService,
                                   PicketLinkIDMService picketLinkIDMService,
                                   SecureRandomService secureRandomService,
                                   InitParams initParams) {
    super(initParams);
    this.entityManagerService = entityManagerService;
    this.picketLinkIDMService = picketLinkIDMService;
    this.secureRandomService = secureRandomService;
  }

  @Override
  public void processUpgrade(String s, String s1) {
    LOG.info("Start upgrade of users passwords hashing algorithm");
    long startupTime = System.currentTimeMillis();

    PortalContainer container = PortalContainer.getInstance();
    RequestLifeCycle.begin(container);
    AtomicInteger updatedPasswords = new AtomicInteger();
    EntityManager entityManager = this.entityManagerService.getEntityManager();
    try {
      String sqlString = "SELECT jbid_io.NAME, jbid_io_creden.TEXT  FROM"
          + " (jbid_io_creden INNER JOIN jbid_io ON jbid_io_creden.IDENTITY_OBJECT_ID = jbid_io.ID)"
          + " INNER JOIN (SELECT jbid_io_attr.IDENTITY_OBJECT_ID FROM jbid_io_attr"
          + " WHERE jbid_io_attr.IDENTITY_OBJECT_ID NOT IN (SELECT jbid_io_attr.IDENTITY_OBJECT_ID FROM jbid_io_attr WHERE NAME = 'passwordSalt128')"
          + " GROUP BY jbid_io_attr.IDENTITY_OBJECT_ID) jia ON jbid_io_creden.IDENTITY_OBJECT_ID  = jia.IDENTITY_OBJECT_ID;";

      Query nativeQuery = entityManager.createNativeQuery(sqlString);
      List<Object[]> result = nativeQuery.getResultList();
      result.forEach(item -> {
        String userName = (String) item[0];
        String passwordHash = (String) item[1];
        try {
          String saltString = Hex.encodeHexString(generateRandomSalt());
          User user = picketLinkIDMService.getIdentitySession().getPersistenceManager().findUser(userName);
          picketLinkIDMService.getExtendedAttributeManager().addAttribute(userName, PASSWORD_SALT_USER_ATTRIBUTE, saltString);
          picketLinkIDMService.getExtendedAttributeManager().updatePassword(user, passwordHash);
          int count = updatedPasswords.incrementAndGet();
          if (count % 50 == 0 || count == result.size()) {
            LOG.info("{}/{} passwords have been updated", count, result.size());
          }
        } catch (Exception e) {
          hasErrors = true;
          LOG.error("Error while creating attribute salt and updating password hash for user : {}", userName, e);
        }
      });
      if (hasErrors) {
        throw new IllegalStateException("UserPasswordHashMigration upgrade failed due to previous errors");
      }
    } catch (Exception e) {
      LOG.error("Error while getting old users passwords hash", e);
      throw new IllegalStateException("UserPasswordHashMigration upgrade failed due to previous errors");
    } finally {
      RequestLifeCycle.end();
    }
    LOG.info("End upgrade of users passwords hashing algorithm. {} passwords has been updated. It took {} ms",
             updatedPasswords.get(),
             (System.currentTimeMillis() - startupTime));
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion,
                                        String previousGroupVersion,
                                        UpgradePluginExecutionContext previousUpgradePluginExecution) {
    try {
      return picketLinkIDMService.getExtendedAttributeManager()
                                 .getDefaultCredentialEncoder()
                                 .getClass()
                                 .getName()
                                 .equals(DEFAULT_ENCODER)
          && super.shouldProceedToUpgrade(newVersion, previousGroupVersion, previousUpgradePluginExecution);
    } catch (Exception e) {
      LOG.error("Error while checking current default credential encoder", e);
      return false;
    }
  }

  private byte[] generateRandomSalt() {
    SecureRandom secureRandom = secureRandomService.getSecureRandom();
    byte[] salt = new byte[16];
    secureRandom.nextBytes(salt);
    return salt;
  }
}
