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
package io.meeds.social.space.template.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Pageable;

import io.meeds.social.space.constant.Registration;
import io.meeds.social.space.constant.Visibility;
import io.meeds.social.space.template.model.SpaceTemplate;

public class SpaceTemplateStorageMock extends SpaceTemplateStorage {

  private SpaceTemplate spaceTemplate;

  public SpaceTemplateStorageMock() {
    super(null);
    spaceTemplate = new SpaceTemplate(2l,
                                      "name",
                                      "description",
                                      0,
                                      "icon",
                                      true,
                                      false,
                                      true,
                                      1l,
                                      null,
                                      Arrays.asList("*:/platform/users"),
                                      Arrays.asList("*:/platform/administrators"),
                                      Arrays.asList("*:/platform/users"),
                                      Arrays.asList("name", "invitation", "properties", "access"),
                                      Visibility.PRIVATE,
                                      Registration.OPEN,
                                      false);
  }

  @Override
  public List<SpaceTemplate> getSpaceTemplates(Pageable pageable) {
    return Collections.singletonList(spaceTemplate);
  }

  @Override
  public List<SpaceTemplate> getEnabledSpaceTemplates(Pageable pageable) {
    return Collections.singletonList(spaceTemplate);
  }

  @Override
  public SpaceTemplate getSpaceTemplate(long id) {
    return id == 0 || id == spaceTemplate.getId() ? spaceTemplate : null;
  }

  @Override
  public SpaceTemplate createSpaceTemplate(SpaceTemplate spaceTemplate) {
    return updateSpaceTemplate(spaceTemplate);
  }

  @Override
  public SpaceTemplate updateSpaceTemplate(SpaceTemplate spaceTemplate) {
    spaceTemplate.setId(this.spaceTemplate.getId());
    this.spaceTemplate = spaceTemplate;
    return spaceTemplate;
  }

  @Override
  public void deleteSpaceTemplate(long id) {
    // NoOp
  }

}
