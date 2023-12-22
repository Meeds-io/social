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

import lombok.Getter;
import org.apache.commons.codec.binary.Hex;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradePluginExecutionContext;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;
import org.exoplatform.web.security.hash.Argon2IdPasswordEncoder;
import org.exoplatform.web.security.security.SecureRandomService;
import org.picketlink.idm.api.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class UserPasswordHashMigration extends UpgradeProductPlugin {

  private static final Log           LOG                          = ExoLogger.getExoLogger(UserPasswordHashMigration.class);

  private static final int           CHUNCK                       = 100;

  private final EntityManagerService entityManagerService;

  private final PicketLinkIDMService picketLinkIDMService;

  private static final String        PASSWORD_SALT_USER_ATTRIBUTE = "passwordSalt128";

  private static final String        DEFAULT_ENCODER              = "org.exoplatform.web.security.hash.Argon2IdPasswordEncoder";

  public UserPasswordHashMigration(EntityManagerService entityManagerService,
                                   PicketLinkIDMService picketLinkIDMService,
                                   InitParams initParams) {
    super(initParams);
    this.entityManagerService = entityManagerService;
    this.picketLinkIDMService = picketLinkIDMService;
  }

  @Override
  public void processUpgrade(String s, String s1) {
    LOG.info("Start upgrade of users passwords hashing algorithm");

    PortalContainer container = PortalContainer.getInstance();
    EntityManager entityManager = this.entityManagerService.getEntityManager();
    try {
      String sqlString = "SELECT jbid_io.NAME, jbid_io_creden.TEXT  FROM"
          + " (jbid_io_creden INNER JOIN jbid_io ON jbid_io_creden.IDENTITY_OBJECT_ID = jbid_io.ID)"
          + " INNER JOIN (SELECT jbid_io_attr.IDENTITY_OBJECT_ID FROM jbid_io_attr"
          + " WHERE jbid_io_attr.IDENTITY_OBJECT_ID NOT IN (SELECT jbid_io_attr.IDENTITY_OBJECT_ID FROM jbid_io_attr WHERE NAME = 'passwordSalt128')"
          + " GROUP BY jbid_io_attr.IDENTITY_OBJECT_ID) jia ON jbid_io_creden.IDENTITY_OBJECT_ID  = jia.IDENTITY_OBJECT_ID;";

      RequestLifeCycle.begin(container);
      Query nativeQuery = entityManager.createNativeQuery(sqlString);
      List<Object[]> result = nativeQuery.getResultList();
      UpgradeReport upgradeReport = new UpgradeReport(result.size());

      result.forEach(item -> fixUser(item, upgradeReport));

      LOG.info("End upgrade of users passwords hashing algorithm. {} passwords has been updated. {} users where not treated due to error. It took {} ms",
               upgradeReport.getSuccess(),
               upgradeReport.getFailure(),
               upgradeReport.getDurationInMillis());
      if (!upgradeReport.getFailedUser().isEmpty()) {
        LOG.error("Users with not modified password : {}", upgradeReport.getFailedUser());
        throw new IllegalStateException("UserPasswordHashMigration upgrade failed due to previous errors");
      }
    } catch (Exception e) {
      throw new IllegalStateException("UserPasswordHashMigration upgrade failed due to previous errors");
    } finally {
      RequestLifeCycle.end();
    }

  }

  @ExoTransactional
  private void fixUser(Object[] item, UpgradeReport upgradeReport) {
    String userName = (String) item[0];
    String passwordHash = (String) item[1];

    try {
      User user = picketLinkIDMService.getIdentitySession().getPersistenceManager().findUser(userName);
      String saltString = Hex.encodeHexString(generateRandomSalt());
      if (user != null && picketLinkIDMService.getExtendedAttributeManager().getAttribute(userName, PASSWORD_SALT_USER_ATTRIBUTE) == null) {
        picketLinkIDMService.getExtendedAttributeManager().addAttribute(userName, PASSWORD_SALT_USER_ATTRIBUTE, saltString);
        picketLinkIDMService.getExtendedAttributeManager().updatePassword(user, passwordHash);
      }
      upgradeReport.incrementSuccess();
      int count = upgradeReport.getFailure()+upgradeReport.getSuccess();
      if (count % CHUNCK == 0) {
        if (picketLinkIDMService.getIdentitySession().getTransaction() != null) {
          picketLinkIDMService.getIdentitySession().getTransaction().commit();
        }
        LOG.info("{}/{} passwords have been updated ({} ms for the chunck)",
                 count,
                 upgradeReport.total,
                 upgradeReport.getChunkDurationInMillis());
        upgradeReport.resetChunckTime();
      }
    } catch (Exception e) {
      upgradeReport.incrementFailure();
      upgradeReport.addFailUser(userName);
      LOG.error("Error while creating attribute salt and updating password hash for user : {}", userName, e);
    }
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
    try {
      if (picketLinkIDMService.getExtendedAttributeManager()
                              .getDefaultCredentialEncoder()
                              .getClass()
                              .getName()
                              .equals(DEFAULT_ENCODER)) {
        Argon2IdPasswordEncoder encoder = (Argon2IdPasswordEncoder) picketLinkIDMService.getExtendedAttributeManager()
                                                                                        .getDefaultCredentialEncoder();
        return encoder.generateRandomSalt();
      } else {
        return new byte[0];
      }
    } catch (Exception e) {
      LOG.error("Error for generating salt",e);
      return new byte[0];
    }

  }

  public static class UpgradeReport {
    private long startTime = System.currentTimeMillis();
    @Getter
    private int total;
    @Getter
    private int success;
    @Getter
    private int failure;
    @Getter
    private long startChunckTime = System.currentTimeMillis();
    @Getter
    private List<String> failedUser = new ArrayList<>();

    public UpgradeReport(int total) {
      this.total = total;
    }
    public void incrementSuccess() {
      success++;
    }
    public void incrementFailure() {
      failure++;
    }
    public long getDurationInMillis() {
      return System.currentTimeMillis() - startTime;
    }

    public long getChunkDurationInMillis() {
      return System.currentTimeMillis() - startChunckTime;
    }

    public void resetChunckTime() {
      startChunckTime = System.currentTimeMillis();
    }

    public void addFailUser(String username) {
      failedUser.add(username);
    }
  }
}
