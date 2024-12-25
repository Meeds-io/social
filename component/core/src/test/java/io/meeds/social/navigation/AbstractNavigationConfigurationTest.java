/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.navigation;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;

import io.meeds.kernel.test.AbstractSpringTest;
import io.meeds.social.navigation.plugin.LinkSidebarPlugin;
import io.meeds.social.navigation.plugin.PageSidebarPlugin;
import io.meeds.social.navigation.plugin.SiteSidebarPlugin;
import io.meeds.social.navigation.plugin.SpaceListSidebarPlugin;
import io.meeds.social.navigation.plugin.SpaceTemplateSidebarPlugin;
import io.meeds.social.navigation.service.NavigationConfigurationImportService;
import io.meeds.social.navigation.service.NavigationConfigurationService;
import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;
import io.meeds.social.space.template.utils.EntityMapper;
import io.meeds.spring.AvailableIntegration;

import lombok.SneakyThrows;

@SpringBootApplication(scanBasePackages = {
  AbstractNavigationConfigurationTest.MODULE_NAME,
  "io.meeds.social.common", // TODO to delete once MIP-170 merged
  AvailableIntegration.KERNEL_TEST_MODULE,
  AvailableIntegration.JPA_MODULE,
}, exclude = {
  LiquibaseAutoConfiguration.class,
})
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/exo.social.component.core-local-root-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/social.component.core-local-navigation-portal-configuration.xml"),
})
@RunWith(SpringRunner.class)
public abstract class AbstractNavigationConfigurationTest extends AbstractCoreTest {

  public static final String                     MODULE_NAME      = "io.meeds.social.navigation";

  protected static final String                  SITE_NAME        = "contribute";

  private SpaceTemplateDAO                       spaceTemplateDao = new SpaceTemplateDAO();

  @Autowired
  protected LayoutService                        layoutService;

  @Autowired
  protected NavigationService                    navigationService;

  @Autowired
  protected SpaceTemplateService                 spaceTemplateService;

  @Autowired
  protected SpaceTemplateStorage                 spaceTemplateStorage;

  @Autowired
  protected UserACL                              userAcl;

  @Autowired
  protected LinkSidebarPlugin                    linkSidebarPlugin;

  @Autowired
  protected PageSidebarPlugin                    pageSidebarPlugin;

  @Autowired
  protected SiteSidebarPlugin                    siteSidebarPlugin;

  @Autowired
  protected SpaceListSidebarPlugin               spaceListSidebarPlugin;

  @Autowired
  protected SpaceTemplateSidebarPlugin           spaceTemplateSidebarPlugin;

  @Autowired
  protected NavigationConfigurationService       navigationConfigurationService;

  @Autowired
  protected NavigationConfigurationImportService navigationConfigurationImportService;

  protected SpaceTemplate                        spaceTemplate;

  public AbstractNavigationConfigurationTest() {
    AbstractSpringTest.setTestClass(this.getClass());
  }

  @Before
  public void beforeEach() throws Exception {
    setUp();
    begin();
    navigationConfigurationImportService.start();
  }

  @After
  public void afterEach() throws Exception {
    end();
    tearDown();
  }

  @SneakyThrows
  public SpaceTemplate mockSpaceTemplate() {
    spaceTemplate = spaceTemplateService.getSpaceTemplates().getFirst();
    if (spaceTemplateDao.find(spaceTemplate.getId()) == null) {
      SpaceTemplateEntity spaceTemplateEntity = EntityMapper.toEntity(spaceTemplate);
      spaceTemplateEntity.setId(null);
      spaceTemplateEntity = spaceTemplateDao.create(spaceTemplateEntity);
      if (spaceTemplate.getId() != spaceTemplateEntity.getId()) {
        spaceTemplate.setId(spaceTemplateEntity.getId());
        spaceTemplateStorage.updateSpaceTemplate(spaceTemplate);
      }
    }
    return spaceTemplate;
  }

  public static class SpaceTemplateDAO extends GenericDAOJPAImpl<SpaceTemplateEntity, Long> {
  }
}
