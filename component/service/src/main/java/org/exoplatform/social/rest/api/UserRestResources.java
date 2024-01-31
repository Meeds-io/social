/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.api;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.exoplatform.social.rest.entity.UserEntity;

public interface UserRestResources extends SocialRest {

  /**
   * Get all users, filter by name if exists.
   *
   * @param q value that an user's name match
   * @param status filter only online users
   * @param userType filter by user type
   * @param isConnected filter by connected users
   * @param spaceId filter only space members
   * @param isDisabled only disabled users
   * @param enrollmentStatus filter by enrollment status
   * @return List of users in json format.
   * @throws Exception
   */
  @GET
  public abstract Response getUsers(@Context UriInfo uriInfo,
                                    @QueryParam("q") String q,
                                    @QueryParam("searchEmail") boolean searchEmail,
                                    @QueryParam("searchUserName") boolean searchUserName,
                                    @QueryParam("status") String status,
                                    @QueryParam("userType") String userType,
                                    @QueryParam("isConnected") String isConnected,
                                    @QueryParam("spaceId") String spaceId,
                                    @QueryParam("isDisabled") boolean isDisabled,
                                    @QueryParam("enrollmentStatus") String enrollmentStatus,
                                    @QueryParam("offset") int offset,
                                    @QueryParam("limit") int limit,
                                    @QueryParam("returnSize") boolean returnSize,
                                    @QueryParam("expand") String expand,
                                    @QueryParam("excludeCurrentUser") boolean excludeCurrentUser) throws Exception;

  /**
   * Creates an user
   * 
   * @param uriInfo
   * @return user created in json format
   * @throws Exception
   */
  @POST
  public abstract Response addUser(@Context UriInfo uriInfo, 
                                   @QueryParam("expand") String expand,
                                   UserEntity model) throws Exception;

  @GET
  @Path("{id}")
  public abstract Response getUserById(@Context UriInfo uriInfo,
                                       @Context Request request,
                                       @PathParam("id") String id,
                                       @QueryParam("expand") String expand) throws Exception;

  @DELETE
  @Path("{id}")
  public abstract Response deleteUserById(@Context UriInfo uriInfo,
                                          @PathParam("id") String id,
                                          @QueryParam("expand") String expand) throws Exception;

  @PUT
  @Path("{id}")
  public abstract Response updateUserById(@Context UriInfo uriInfo,
                                                   @PathParam("id") String id,
                                                   @QueryParam("expand") String expand, 
                                                   UserEntity model) throws Exception;

  @GET
  @Path("{id}/connections")
  public abstract Response getConnectionsOfUser(@Context UriInfo uriInfo,
                                               @PathParam("id") String id,
                                               @PathParam("q") String q,
                                               @QueryParam("returnSize") boolean returnSize,
                                               @QueryParam("expand") String expand) throws Exception;

  @GET
  @Path("{id}/connections/invitations")
  public abstract Response getInvitationsOfUser(@Context UriInfo uriInfo,
                                                @QueryParam("returnSize") boolean returnSize,
                                                @QueryParam("expand") String expand) throws Exception;

  @GET
  @Path("{id}/connections/pending")
  public abstract Response getPendingOfUser(@Context UriInfo uriInfo,
                                            @QueryParam("returnSize") boolean returnSize,
                                            @QueryParam("expand") String expand) throws Exception;

  @GET
  @Path("{id}/spaces")
  public abstract Response getSpacesOfUser(@Context UriInfo uriInfo,
                                           @PathParam("id") String id,
                                           @QueryParam("offset") int offset,
                                           @QueryParam("limit") int limit,
                                           @QueryParam("returnSize") boolean returnSize,
                                           @QueryParam("expand") String expand) throws Exception;

}
