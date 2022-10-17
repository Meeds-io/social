/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.rest.api;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;

import org.exoplatform.social.rest.entity.IdentityEntity;

public interface IdentityRestResources extends SocialRest {

  /**
   * Process to return a list of identities in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  public abstract Response getIdentities(@Context UriInfo uriInfo,
                                         @QueryParam("type") String type,
                                         @QueryParam("offset") int offset,
                                         @QueryParam("limit") int limit,
                                         @QueryParam("returnSize") boolean returnSize,
                                         @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to return an identity in json format
   * 
   * @param uriInfo
   * @param request
   * @param id
   * @param expand
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public abstract Response getIdentityById(@Context UriInfo uriInfo,
                                           @Context Request request,
                                           @PathParam("id") String id,
                                           @QueryParam("expand") String expand) throws Exception;
  

  /**
   * Return an identity identified by its providerId and remoteId
   * 
   * @param uriInfo
   * @param request
   * @param providerId
   * @param remoteId
   * @param expand
   * @return {@link Response} containing {@link IdentityEntity}
   */
  @GET
  @Path("{providerId}/{remoteId}")
  public Response getIdentityByProviderIdAndRemoteId(@Context UriInfo uriInfo,
                                                   @Context Request request,
                                                   @PathParam("providerId") String providerId,
                                                   @PathParam("remoteId") String remoteId,
                                                   @QueryParam("expand") String expand);

  /**
   * Process to return all relationships of an identity in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/relationships")
  public abstract Response getRelationshipsOfIdentity(@Context UriInfo uriInfo,
                                                      @PathParam("id") String id,
                                                      @QueryParam("with") String with,
                                                      @QueryParam("returnSize") boolean returnSize,
                                                      @QueryParam("offset") int offset,
                                                      @QueryParam("limit") int limit,
                                                      @QueryParam("expand") String expand) throws Exception;

}
