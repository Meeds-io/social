/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.deprecation.DeprecatedAPI;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.impl.user.UserRestResourcesV1;

/**
 * Provides services to work with identities.
 *
 *  @deprecated use {@link UserRestResourcesV1}
 * @anchor IdentityRestService
 */
@Path("{portalName}/social/identity/{username}/id")
@Deprecated
public class IdentityRestService implements ResourceContainer {
  private IdentityManager _identityManager;
  private static final Log LOG = ExoLogger.getLogger(IdentityRestService.class);
  /**
   * constructor
   */
  public IdentityRestService() {}

  /**
   * Gets an identity by a user's name and returns in the JSON format.
   * 
   * @param uriInfo The requested URI information.
   * @param username The name of the target user.
   * @param portalName The name of the current portal.
   * @anchor IdentityRestService.getId
   *
   * @return UserId The information of provided user.
   * 
   * @throws Exception
   * 
   * @LevelAPI Platform
   */
  @GET
  @Path("show.json")
  @Produces({MediaType.APPLICATION_JSON})
  @DeprecatedAPI(value = "The endpoint is deprecated, use IdentityRestResourcesV1 instead", insist = true)
  public Response getId(@Context UriInfo uriInfo,
                      @PathParam("username") String username,
                      @PathParam("portalName") String portalName) throws Exception {
      _identityManager = getIdentityManager(portalName);
      MediaType mediaType = Util.getMediaType("json");
      String id = null;
      String viewerId = Util.getViewerId();
      if (viewerId != null) {
        if(getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true) == null ) {
           LOG.error("No id is found for this username. You should enter a valid and a correct value for the username parameter.");
           return Util.getResponse("No id is found for this username. You should enter a valid and a correct value for the username parameter.", uriInfo, mediaType, Response.Status.OK);
        } else {
          try {
           id = _identityManager.getOrCreateIdentity("organization", username).getId();
          } catch(Exception ex) {
           throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
          }
        }
        Identity identity = getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME, viewerId, true);
        if (identity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      }
      try {
        id = _identityManager.getOrCreateIdentity("organization", username).getId();
      } catch(Exception ex) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
      UserId userId = new UserId(id);
      return Util.getResponse(userId, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager(String portalName) {
    if (_identityManager == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getContainerByName(portalName);
      _identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return _identityManager;
  }

  /**
   * UserId class to be exposed
   * @author hoatle
   *
   */
  public class UserId {
    private String _id;

    /**
     * constructor
     */
    public UserId() {

    }
    /**
     * constructor
     * @param id
     */
    public UserId(String id) {
      _id = id;
    }
    /**
     * sets id
     * @param id userId
     */
    public void setId(String id) {
      _id = id;
    }
    /**
     * gets id
     * @return userId
     */
    public String getId() {
      return _id;
    }

    public String toString() {
      return _id;
    }
  }
}
