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
package io.meeds.social;

import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;

import io.meeds.spring.AvailableIntegration;

@SpringBootApplication(scanBasePackages = {
  "io.meeds.social.navigation",
  "io.meeds.social.category",
  "io.meeds.social.space.category",
  AvailableIntegration.KERNEL_TEST_MODULE,
  AvailableIntegration.JPA_MODULE,
}, exclude = {
  LiquibaseAutoConfiguration.class
})
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/exo.social.component.core-local-root-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/social.component.core-local-navigation-portal-configuration.xml"),
})
@RunWith(SpringRunner.class)
public abstract class AbstractSpringConfigurationTest extends AbstractCoreTest {

}
