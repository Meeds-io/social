/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import org.exoplatform.social.core.space.SpaceUtils;

import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;
import io.meeds.social.space.template.model.SpaceTemplate;

/**
 * One default template (id 2), which every legacy call reads and writes as
 * before: {@link #getSpaceTemplate(long)} answers it for id 0 or 2, and a
 * create or update carrying no other id overwrites it. A template created with
 * its own id is held beside it, listed after it, and removed by
 * {@link #deleteSpaceTemplate(long)} — for the tests that need a parent
 * template allowing a distinct child template.
 */
public class SpaceTemplateStorageMock extends SpaceTemplateStorage {

  private SpaceTemplate                  spaceTemplate;

  private final Map<Long, SpaceTemplate> otherTemplates = new LinkedHashMap<>();

  private static final long SPACE_CATEGORY_ID = 2l;

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
                                      null,
                                      Arrays.asList(SpaceUtils.PLATFORM_USERS_GROUP),
                                      Arrays.asList(SpaceUtils.PLATFORM_PUBLISHER_GROUP),
                                      Arrays.asList(SpaceUtils.SPACE_ADMIN_REFERENCE_NAME),
                                      Arrays.asList(SpaceUtils.SPACE_ADMIN_REFERENCE_NAME),
                                      Arrays.asList(SpaceUtils.SPACE_ADMIN_REFERENCE_NAME),
                                      Arrays.asList("name", "invitation", "properties", "access"),
                                      Arrays.asList(SPACE_CATEGORY_ID),
                                      SpaceVisibility.PRIVATE,
                                      SpaceRegistration.OPEN,
                                      false,
                                      null,
                                      0,
                                      "/space_templates/name",
                                      null,
                                      new HashMap<>());
  }

  @Override
  public List<SpaceTemplate> getSpaceTemplates(Pageable pageable) {
    List<SpaceTemplate> templates = new ArrayList<>();
    templates.add(spaceTemplate);
    templates.addAll(otherTemplates.values());
    return templates;
  }

  @Override
  public List<SpaceTemplate> getEnabledSpaceTemplates(Pageable pageable) {
    return getSpaceTemplates(pageable).stream().filter(SpaceTemplate::isEnabled).toList();
  }

  @Override
  public SpaceTemplate getSpaceTemplate(long id) {
    if (id == 0 || id == spaceTemplate.getId()) {
      return spaceTemplate;
    }
    return otherTemplates.get(id);
  }

  @Override
  public SpaceTemplate createSpaceTemplate(SpaceTemplate spaceTemplate) {
    return updateSpaceTemplate(spaceTemplate);
  }

  @Override
  public SpaceTemplate updateSpaceTemplate(SpaceTemplate spaceTemplate) {
    if (isOtherTemplate(spaceTemplate)) {
      otherTemplates.put(spaceTemplate.getId(), spaceTemplate);
    } else {
      spaceTemplate.setId(this.spaceTemplate.getId());
      this.spaceTemplate = spaceTemplate;
    }
    return spaceTemplate;
  }

  @Override
  public void deleteSpaceTemplate(long id) {
    otherTemplates.remove(id);
  }

  private boolean isOtherTemplate(SpaceTemplate template) {
    return template.getId() > 0 && template.getId() != spaceTemplate.getId();
  }

}
