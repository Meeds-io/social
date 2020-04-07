/*
 * Copyright (C) 2003-2020 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.service.rest;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.editors.ErrorMessage;
import org.exoplatform.social.service.rest.editors.HypermediaLink;
import org.exoplatform.social.service.rest.editors.HypermediaSupport;
import org.exoplatform.social.service.rest.editors.IdentityData;

/**
 * The Class IdentitySearchRESTService.
 */
@Path("/identity/search")
public class IdentitySearchRESTService implements ResourceContainer {

  /** The Constant LOG. */
  protected static final Log    LOG                    = ExoLogger.getLogger(IdentitySearchRESTService.class);

  /** The Constant CANNOT_LOAD_IDENTITIES. */
  protected static final String CANNOT_LOAD_IDENTITIES = "CannotLoadIdentities";

  /** The max result size. */
  protected static final int    MAX_RESULT_SIZE        = 30;

  /** The Constant USER_TYPE. */
  protected static final String USER_TYPE              = "user";

  /** The Constant SPACE_TYPE. */
  protected static final String SPACE_TYPE             = "space";

  /** The Constant GROUP_TYPE. */
  protected static final String GROUP_TYPE             = "group";

  /** The Constant SELF. */
  protected static final String SELF                   = "self";

  /** The identity manager. */
  protected IdentityManager     identityManager;

  /** The organization service. */
  protected OrganizationService organization;

  /** The space service. */
  protected SpaceService        spaceService;

  /**
   * Instantiates a new IdentitySearchRESTService.
   *
   * @param identityManager the identity manager
   * @param organization the organization
   * @param spaceService the space service
   */
  public IdentitySearchRESTService(IdentityManager identityManager, OrganizationService organization, SpaceService spaceService) {
    this.identityManager = identityManager;
    this.organization = organization;
    this.spaceService = spaceService;
  }

  /**
   * Search identities.
   *
   * @param uriInfo the uri info
   * @param name the name
   * @return the response
   */
  @GET
  @RolesAllowed("administrators")
  @Path("/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchIdentities(@Context UriInfo uriInfo, @PathParam("name") String name) {
    try {
      IdentitiesDataResponse responseEntity = new IdentitiesDataResponse(findGroupsAndUsers(name));
      responseEntity.addLink(SELF, new HypermediaLink(uriInfo.getAbsolutePath().toString()));
      return Response.status(Status.OK).entity(responseEntity).build();
    } catch (AccessDeniedException e) {
      LOG.error("Access denied to get identities with name: {}, error: {}", name, e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorMessage("Access denied error.", CANNOT_LOAD_IDENTITIES))
                     .build();
    } catch (Exception e) {
      LOG.error("Cannot get identities with name: {}, error: {}", name, e.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorMessage(e.getMessage(), CANNOT_LOAD_IDENTITIES))
                     .build();
    }
  }

  /**
   * Find groups and users.
   *
   * @param name the name
   * @return the list
   * @throws Exception the exception
   */
  protected List<IdentityData> findGroupsAndUsers(String name) throws Exception {
    List<IdentityData> identitiesData = findUsers(name, MAX_RESULT_SIZE / 2);
    int remain = MAX_RESULT_SIZE - identitiesData.size();
    identitiesData.addAll(findGroups(name, remain));
    Collections.sort(identitiesData, new Comparator<IdentityData>() {
      public int compare(IdentityData s1, IdentityData s2) {
        return s1.getDisplayName().compareTo(s2.getDisplayName());
      }
    });
    return identitiesData;
  }

  /**
   * Find users.
   *
   * @param name the name
   * @param count the count
   * @return the list
   * @throws Exception the exception
   */
  protected List<IdentityData> findUsers(String name, int count) throws Exception {
    List<IdentityData> results = new ArrayList<>();
    ProfileFilter identityFilter = new ProfileFilter();
    identityFilter.setName(name);
    ListAccess<Identity> identitiesList = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                                                       identityFilter,
                                                                                       false);
    int size = identitiesList.getSize() >= count ? count : identitiesList.getSize();
    if (size > 0) {
      Identity[] identities = identitiesList.load(0, size);
      for (Identity id : identities) {
        Profile profile = id.getProfile();
        String fullName = profile.getFullName();
        String userName = (String) profile.getProperty(Profile.USERNAME);
        String avatarUrl = profile.getAvatarUrl() != null ? profile.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
        results.add(new IdentityData(userName, fullName, USER_TYPE, avatarUrl));
      }
    }
    return results;
  }

  /**
   * Find groups.
   *
   * @param name the name
   * @param count the count
   * @return the list
   * @throws Exception the exception
   */
  protected List<IdentityData> findGroups(String name, int count) throws Exception {
    List<IdentityData> results = new ArrayList<>();
    ListAccess<Group> groupsAccess = organization.getGroupHandler().findGroupsByKeyword(name);
    int size = groupsAccess.getSize() >= count ? count : groupsAccess.getSize();
    if (size > 0) {
      Group[] groups = groupsAccess.load(0, size);
      for (Group group : groups) {
        Space space = spaceService.getSpaceByGroupId(group.getId());
        if (space != null) {
          String avatarUrl = space.getAvatarUrl() != null ? space.getAvatarUrl() : LinkProvider.SPACE_DEFAULT_AVATAR_URL;
          results.add(new IdentityData(space.getGroupId(), space.getDisplayName(), SPACE_TYPE, avatarUrl));
        } else {
          results.add(new IdentityData(group.getId(), group.getLabel(), GROUP_TYPE, LinkProvider.SPACE_DEFAULT_AVATAR_URL));
        }
      }
    }
    return results;
  }

  /**
   * The Class IdentitiesDataResponse.
   */
  public static class IdentitiesDataResponse extends HypermediaSupport {

    /** The identities. */
    private final List<IdentityData> identities;

    /**
     * Instantiates a new identities data response.
     *
     * @param identities the identities
     */
    public IdentitiesDataResponse(List<IdentityData> identities) {
      this.identities = identities;
    }

    /**
     * Gets the identities.
     *
     * @return the identities
     */
    public List<IdentityData> getIdentities() {
      return identities;
    }

  }

}
