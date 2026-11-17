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
package io.meeds.social.space.security.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.security.model.SpaceSecuritySettings;
import io.meeds.social.translation.service.TranslationService;
import io.meeds.social.space.template.dao.SpaceTemplateDAO;
import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;

import jakarta.persistence.EntityManagerFactory;

/**
 * Case (e): the template's delegated administrators and its settings are read
 * from its committed row, never through the social.spaceTemplates cache. Each
 * case caches the template, then writes its row with the storage bypassed.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
  SpaceSecurityServiceImpl.class,
  SpaceTemplateService.class,
  SpaceTemplateStorage.class,
  SpaceSecurityServiceTemplateAdministrationTest.JpaConfiguration.class,
})
public class SpaceSecurityServiceTemplateAdministrationTest {

  private static final String  USER             = "mary";

  private static final String  DELEGATES_GROUP  = "/platform/delegates";

  private static final long    SPACE_ID         = 7L;

  @MockitoBean
  private SpaceStorage         spaceStorage;

  @MockitoBean
  private SpaceService         spaceService;

  @MockitoBean
  private UserACL              userAcl;

  @MockitoBean
  private TranslationService      translationService;

  @MockitoBean
  private AttachmentService       attachmentService;

  @MockitoBean
  private UserPortalConfigService userPortalConfigService;

  @MockitoBean
  private LayoutService           layoutService;

  @MockitoBean
  private NavigationService       navigationService;

  @MockitoBean
  private ListenerService         listenerService;

  @MockitoBean
  private OrganizationService     organizationService;

  @MockitoBean
  private LocaleConfigService     localeConfigService;

  @Autowired
  private SpaceSecurityService spaceSecurityService;

  @Autowired
  private SpaceTemplateStorage spaceTemplateStorage;

  @Autowired
  private SpaceTemplateDAO     spaceTemplateDAO;

  @Before
  public void setUp() throws Exception {
    Identity identity = new Identity(USER, List.of(new MembershipEntry(DELEGATES_GROUP)));
    when(userAcl.getUserIdentity(USER)).thenReturn(identity);
    when(userAcl.isAdministrator(identity)).thenReturn(false);
    when(spaceStorage.getSpaceExtendedProperties(SPACE_ID)).thenReturn(Map.of(SpaceSecuritySettings.LIMIT_ACCESS_MANAGEMENT_KEY,
                                                                              "true"));
    when(spaceStorage.getMemberStatuses(SPACE_ID, USER)).thenReturn(Set.of());
  }

  @Test
  public void testDelegatedAdministratorsReadFromTheCommittedRow() throws Exception {
    SpaceTemplateEntity template = createTemplate(List.of(DELEGATES_GROUP), new HashMap<>());
    Space space = space(template.getId());
    SpaceSecuritySettings settings = spaceSecurityService.getSettings(SPACE_ID);
    assertTrue(spaceSecurityService.canManageDocumentsAccess(space, settings, USER));
    assertEquals(List.of(DELEGATES_GROUP), spaceTemplateStorage.getSpaceTemplate(template.getId()).getAdminPermissions());

    template.setAdminPermissions(List.of("/platform/others"));
    spaceTemplateDAO.save(template);

    assertEquals(List.of(DELEGATES_GROUP), spaceTemplateStorage.getSpaceTemplate(template.getId()).getAdminPermissions());
    assertFalse(spaceSecurityService.canManageDocumentsAccess(space, settings, USER));
    assertFalse(spaceSecurityService.canManageSpace(space, USER));
  }

  @Test
  public void testDeletedTemplateHasNoDelegatedAdministrator() throws Exception {
    SpaceTemplateEntity template = createTemplate(List.of(DELEGATES_GROUP), new HashMap<>());
    Space space = space(template.getId());
    spaceTemplateStorage.getSpaceTemplate(template.getId());

    template.setDeleted(true);
    spaceTemplateDAO.save(template);

    assertFalse(spaceSecurityService.canManageSpace(space, USER));
  }

  @Test
  public void testTemplateSettingsReadFromTheCommittedRow() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put(SpaceSecuritySettings.EXTERNAL_MEMBERS_ALLOWED_KEY, "true");
    SpaceTemplateEntity template = createTemplate(List.of(), properties);
    spaceTemplateStorage.getSpaceTemplate(template.getId());
    assertTrue(spaceSecurityService.getTemplateSettings(template.getId()).getExternalMembersAllowed());

    properties = new HashMap<>();
    properties.put(SpaceSecuritySettings.EXTERNAL_MEMBERS_ALLOWED_KEY, "false");
    template.setExtendedProperties(properties);
    spaceTemplateDAO.save(template);

    assertEquals("true",
                 spaceTemplateStorage.getSpaceTemplate(template.getId())
                                     .getExtendedProperties()
                                     .get(SpaceSecuritySettings.EXTERNAL_MEMBERS_ALLOWED_KEY));
    assertFalse(spaceSecurityService.getTemplateSettings(template.getId()).getExternalMembersAllowed());
  }

  private SpaceTemplateEntity createTemplate(List<String> adminPermissions, Map<String, String> extendedProperties) {
    SpaceTemplateEntity template = new SpaceTemplateEntity();
    template.setEnabled(true);
    template.setAdminPermissions(adminPermissions);
    template.setExtendedProperties(extendedProperties);
    return spaceTemplateDAO.save(template);
  }

  private Space space(long templateId) {
    Space space = new Space();
    space.setId(SPACE_ID);
    space.setTemplateId(templateId);
    return space;
  }

  @Configuration
  @EnableCaching
  @EnableTransactionManagement
  @EnableJpaRepositories(basePackageClasses = SpaceTemplateDAO.class)
  static class JpaConfiguration {

    @Bean
    DataSource dataSource() {
      return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
                                          .setName("space-security-template")
                                          .build();
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
      LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
      entityManagerFactory.setDataSource(dataSource);
      entityManagerFactory.setPackagesToScan(SpaceTemplateEntity.class.getPackageName());
      entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
      entityManagerFactory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
      return entityManagerFactory;
    }

    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
      return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager("social.spaceTemplates", "social.enabledSpaceTemplates");
    }

  }

}
