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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Parameter;
import org.exoplatform.social.rest.entity.SpaceEntity;

public interface SpaceRestResources extends SocialRest {

/**
 * Process to return a list of space in json format
 * 
 * @param uriInfo
 * @param request
 * @param q
 * @param typeFilter
 * @param offset
 * @param limit
 * @param sort
 * @param order
 * @param returnSize
 * @param expand
 * @return
 * @throws Exception
 */
  @GET
  public abstract Response getSpaces(@Context UriInfo uriInfo,
                                     @Context Request request,
                                     @QueryParam("q") String q,
                                     @QueryParam("all") String typeFilter,
                                     @QueryParam("offset") int offset,
                                     @QueryParam("limit") int limit,
                                     @QueryParam("sort") String sort,
                                     @QueryParam("order") String order,
                                     @QueryParam("returnSize") boolean returnSize,
                                     @QueryParam("favorites") boolean favorites,
                                     @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to create a new space
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @POST
  public abstract Response createSpace(@Context UriInfo uriInfo, 
                                       @QueryParam("expand") String expand,
                                       SpaceEntity model) throws Exception;

  /**
   * Checks if a specific space contains external users
   *
   * @param uriInfo
   * @param request
   * @param spaceId
   * @return space info of a space contains an external members
   */
  @GET
  @Path("{spaceId}")
  public abstract Response isSpaceContainsExternals(@Context UriInfo uriInfo,
                                                    @Context Request request,
                                                    @Parameter(description = "Space Id", required = true) @PathParam("spaceId") String spaceId);

  /**
   * Process to return a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public abstract Response getSpaceById(@Context UriInfo uriInfo,
                                        @Context Request request,
                                        @PathParam("id") String id,
                                        @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to return a space by pretty name
   * 
   * @param uriInfo
   * @param prettyName
   * @param expand
   * @return
   * @throws Exception
   */
  @GET
  @Path("{prettyName}")
  public abstract Response getSpaceByPrettyName(@Context UriInfo uriInfo,
                                                @Context Request request,
                                                @PathParam(
                                                  "prettyName"
                                                ) String prettyName,
                                                @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to return a space by display name
   * 
   * @param uriInfo
   * @param displayName
   * @param expand
   * @return
   * @throws Exception
   */
  @GET
  @Path("{displayName}")
  public abstract Response getSpaceByDisplayName(@Context UriInfo uriInfo,
                                                 @Context Request request,
                                                 @PathParam(
                                                   "displayName"
                                                 ) String displayName,
                                                 @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to update a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public abstract Response updateSpaceById(@Context UriInfo uriInfo,
                                           @PathParam("id") String id,
                                           @QueryParam("expand") String expand, 
                                           SpaceEntity model) throws Exception;

  /**
   * Process to delete a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public abstract Response deleteSpaceById(@Context UriInfo uriInfo,
                                           @PathParam("id") String id,
                                           @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to return a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/users")
  public abstract Response getSpaceMembers(@Context UriInfo uriInfo,
                                           @Context Request request,
                                           @PathParam("id") String id,
                                           @QueryParam("q") String q,
                                           @QueryParam("role") String role,
                                           @QueryParam("offset") int offset,
                                           @QueryParam("limit") int limit,
                                           @QueryParam("returnSize") boolean returnSize,
                                           @QueryParam("expand") String expand) throws Exception;


}