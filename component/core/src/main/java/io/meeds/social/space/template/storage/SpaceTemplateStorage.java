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

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.space.template.dao.SpaceTemplateDAO;
import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.utils.EntityMapper;

@Component
public class SpaceTemplateStorage {

  private SpaceTemplateDAO spaceTemplateDAO;

  public SpaceTemplateStorage(SpaceTemplateDAO spaceTemplateDAO) {
    this.spaceTemplateDAO = spaceTemplateDAO;
  }

  @Cacheable(cacheNames = "social.spaceTemplates")
  public List<SpaceTemplate> getSpaceTemplates(Pageable pageable) {
    return spaceTemplateDAO.findByDeletedFalse(pageable)
                           .stream()
                           .map(EntityMapper::fromEntity)
                           .toList();
  }

  @Cacheable(cacheNames = "social.enabledSpaceTemplates")
  public List<SpaceTemplate> getEnabledSpaceTemplates(Pageable pageable) {
    return spaceTemplateDAO.findByDeletedFalseAndEnabledTrue(pageable)
                           .stream()
                           .map(EntityMapper::fromEntity)
                           .toList();
  }

  @Cacheable(cacheNames = "social.spaceTemplates")
  public SpaceTemplate getSpaceTemplate(long id) {
    return spaceTemplateDAO.findById(id)
                           .map(EntityMapper::fromEntity)
                           .orElse(null);
  }

  @CacheEvict(cacheNames = { "social.spaceTemplates", "social.enabledSpaceTemplates" }, allEntries = true)
  public SpaceTemplate createSpaceTemplate(SpaceTemplate spaceTemplate) {
    SpaceTemplateEntity spaceTemplateEntity = EntityMapper.toEntity(spaceTemplate);
    spaceTemplateEntity = spaceTemplateDAO.save(spaceTemplateEntity);
    return EntityMapper.fromEntity(spaceTemplateEntity);
  }

  @CacheEvict(cacheNames = { "social.spaceTemplates", "social.enabledSpaceTemplates" }, allEntries = true)
  public SpaceTemplate updateSpaceTemplate(SpaceTemplate spaceTemplate) throws ObjectNotFoundException {
    if (!spaceTemplateDAO.existsById(spaceTemplate.getId())) {
      throw new ObjectNotFoundException("Space template doesn't exist");
    }
    SpaceTemplateEntity spaceTemplateEntity = EntityMapper.toEntity(spaceTemplate);
    spaceTemplateEntity = spaceTemplateDAO.save(spaceTemplateEntity);
    return EntityMapper.fromEntity(spaceTemplateEntity);
  }

  @CacheEvict(cacheNames = { "social.spaceTemplates", "social.enabledSpaceTemplates" }, allEntries = true)
  public void deleteSpaceTemplate(long id) {
    spaceTemplateDAO.deleteById(id);
  }

}
