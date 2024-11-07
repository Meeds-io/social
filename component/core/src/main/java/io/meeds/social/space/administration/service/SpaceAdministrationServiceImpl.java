package io.meeds.social.space.administration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.administration.model.SpacePermissions;

import lombok.Setter;

@Service
public class SpaceAdministrationServiceImpl implements SpaceAdministrationService {

  private static final String SPACE_NOT_FOUN_MESSAGE = "Space with id %s doesn't exist";

  @Setter
  @Autowired
  private SpaceService        spaceService;

  @Override
  public SpacePermissions getSpacePermissions(long spaceId) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUN_MESSAGE, spaceId));
    }
    return new SpacePermissions(space.getLayoutPermissions(),
                                space.getPublicSitePermissions(),
                                space.getDeletePermissions());
  }

  @Override
  public void updateSpacePermissions(long spaceId, SpacePermissions permissions) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUN_MESSAGE, spaceId));
    }
    space.setLayoutPermissions(permissions.getLayoutPermissions());
    space.setPublicSitePermissions(permissions.getPublicSitePermissions());
    space.setDeletePermissions(permissions.getDeletePermissions());
    spaceService.updateSpace(space);
  }

}
