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

import io.swagger.annotations.ApiParam;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.social.rest.entity.IdentityEntity;
import org.exoplatform.social.rest.entity.ProfileEntity;

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
   * Process to create an identity
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @POST
  public abstract Response createIdentities(@Context UriInfo uriInfo,
                                            @QueryParam("remoteId") String remoteId,
                                            @QueryParam("providerId") String providerId,
                                            @QueryParam("expand") String expand ) throws Exception;

  /**
   * Process to return an identity in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public abstract Response getIdentityById(@Context UriInfo uriInfo,
                                           @PathParam("id") String id,
                                           @QueryParam("expand") String expand) throws Exception;
  

  /**
   * Return an identity identified by its providerId and remoteId
   * 
   * @param uriInfo
   * @param providerId
   * @param remoteId
   * @param expand
   * @return {@link Response} containing {@link IdentityEntity}
   */
  @GET
  @Path("{providerId}/{remoteId}")
  public Response getIdentityProviderIdAndRemoteId(@Context UriInfo uriInfo,
                                                   @PathParam("providerId") String providerId,
                                                   @PathParam("remoteId") String remoteId,
                                                   @QueryParam("expand") String expand);

  /**
   * Process to update an identity by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public abstract Response updateIdentityById(@Context UriInfo uriInfo,
                                              @PathParam("id") String id,
                                              @QueryParam("expand") String expand,
                                              @ApiParam(value = "Updated profile object.", required = false) ProfileEntity model) throws Exception;

  /**
   * Process to delete an identity
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public abstract Response deleteIdentityById(@Context UriInfo uriInfo,
                                              @PathParam("id") String id,
                                              @QueryParam("expand") String expand) throws Exception;

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