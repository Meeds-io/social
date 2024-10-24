/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

package org.exoplatform.social.rest.impl.binding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingOperationReport;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportAction;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportUser;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.GroupNodeEntity;
import org.exoplatform.social.rest.entity.GroupSpaceBindingEntity;
import org.exoplatform.social.rest.entity.GroupSpaceBindingOperationReportEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/spaceGroupBindings")
@Tag(name = VersionResources.VERSION_ONE
    + "/social/groupSpaceBindings", description = "API  to manage the binding between a space and an organization group")
public class GroupSpaceBindingRest implements ResourceContainer {

  private GroupSpaceBindingService groupSpaceBindingService;

  private SpaceService             spaceService;

  private SimpleDateFormat         formater = new SimpleDateFormat("yy-MM-dd_HH-mm-ss");

  private static final Log         LOG      = ExoLogger.getLogger(GroupSpaceBindingRest.class);

  public GroupSpaceBindingRest(SpaceService spaceService,
                               GroupSpaceBindingService groupSpaceBindingService) {
    this.groupSpaceBindingService = groupSpaceBindingService;
    this.spaceService = spaceService;
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{spaceId}")
  @Operation(summary = "Gets list of binding for a space.", method = "GET", description = "Returns a list of bindings in the following cases if the authenticated user is an administrator.")
  @ApiResponses(value = { 
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), 
          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getBindingsBySpaceId(@Context UriInfo uriInfo,
                                       @Parameter(description = "Space id", required = true) @PathParam("spaceId") String spaceId,
                                       @Parameter(description = "Offset") @Schema(defaultValue = "0") @QueryParam("offset") int offset,
                                       @Parameter(description = "Limit") @Schema(defaultValue = "10") @QueryParam("limit") int limit,
                                       @Parameter(description = "Returning the number of spaces found or not") 
                                       @Schema(defaultValue = "false")  @QueryParam("returnSize") boolean returnSize) throws Exception {
    // Retrieve all removed bindings ids.
    List<Long> removedSpaceBindingsIds =
                                       groupSpaceBindingService.getGroupSpaceBindingsFromQueueByAction(GroupSpaceBindingQueue.ACTION_REMOVE)
                                                               .stream()
                                                               .map(groupSpaceBinding -> groupSpaceBinding.getId())
                                                               .collect(Collectors.toList());

    List<GroupSpaceBinding> spaceBindings = groupSpaceBindingService.findGroupSpaceBindingsBySpace(spaceId);

    // Get rid of removed bindings.
    if (removedSpaceBindingsIds.size() > 0 && spaceBindings.size() > 0) {
      spaceBindings.removeIf(spaceBinding -> removedSpaceBindingsIds.contains(Long.valueOf(spaceBinding.getId())));
    }

    if (spaceBindings.size() == 0) {
      return EntityBuilder.getResponse(new CollectionEntity(new ArrayList<>(),
                                                            EntityBuilder.GROUP_SPACE_BINDING_TYPE,
                                                            offset,
                                                            limit),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    }

    List<DataEntity> bindingEntities = new ArrayList<>();

    for (GroupSpaceBinding binding : spaceBindings) {
      GroupSpaceBindingEntity bindingEntity = EntityBuilder.buildEntityFromGroupSpaceBinding(binding);
      bindingEntities.add(bindingEntity.getDataEntity());
    }

    CollectionEntity collectionBinding = new CollectionEntity(bindingEntities,
                                                              EntityBuilder.GROUP_SPACE_BINDING_TYPE,
                                                              offset,
                                                              limit);
    if (returnSize) {
      collectionBinding.setSize(bindingEntities.size());
    }

    return EntityBuilder.getResponse(collectionBinding, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @RolesAllowed("administrators")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("saveGroupsSpaceBindings/{spaceId}")
  @Operation(summary = "Save space group bindings", method = "POST", description = "This method set bindings for a specific space with a list of groups if the authenticated user is an administrator.")
  @ApiResponses(value = { 
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding") })
  public Response saveGroupSpaceBindings(@Context UriInfo uriInfo,
                                         @Parameter(description = "SpaceId of the space", required = true) @PathParam("spaceId") String spaceId,
                                         @Parameter(description = "List of group names to be bound to the space", required = true) List<String> groupNames) {
    if (groupNames == null || groupNames.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    // Get already bound groups to the space.
    List<String> spaceBoundGroups = groupSpaceBindingService.findGroupSpaceBindingsBySpace(spaceId)
                                                            .stream()
                                                            .map(groupSpaceBinding -> groupSpaceBinding.getGroup())
                                                            .collect(Collectors.toList());
    // Get bound groups to the space that are already removed bindings.
    List<String> spaceRemovedBoundGroups =
                                         groupSpaceBindingService.getGroupSpaceBindingsFromQueueByAction(GroupSpaceBindingQueue.ACTION_REMOVE)
                                                                 .stream()
                                                                 .filter(groupSpaceBinding -> groupSpaceBinding.getSpaceId()
                                                                                                               .equals(spaceId))
                                                                 .map(groupSpaceBinding -> groupSpaceBinding.getGroup())
                                                                 .collect(Collectors.toList());
    // Get rid of only bound groups to the space that are not of removed bindings.
    spaceBoundGroups.removeAll(spaceRemovedBoundGroups);
    groupNames.removeAll(spaceBoundGroups);
    if (groupNames.size() == 0) {
      return Response.ok("Already bound!").build();
    }
    List<GroupSpaceBinding> groupSpaceBindings = new ArrayList<>();
    groupNames.stream().forEach(groupName -> groupSpaceBindings.add(new GroupSpaceBinding(spaceId, groupName)));

    groupSpaceBindingService.saveGroupSpaceBindings(groupSpaceBindings);

    return Response.ok().build();
  }

  /**
   * {@inheritDoc}
   */
  @DELETE
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @Path("removeGroupSpaceBinding/{bindingId}")
  @Operation(summary = "Deletes a binding.", method = "DELETE", description = "This method deletes a binding in the following cases the authenticated user is an administrator.")
  @ApiResponses(value = { 
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), 
          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response deleteSpaceBinding(@Context UriInfo uriInfo,
                                     @Parameter(description = "spaceId", required = true) @PathParam("bindingId") String bindingId) throws Exception {
    GroupSpaceBinding binding;
    binding = groupSpaceBindingService.findGroupSpaceBindingById(bindingId);
    if (binding != null) {
      groupSpaceBindingService.prepareDeleteGroupSpaceBinding(binding);
    }
    return Response.ok().build();
  }

  @GET
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @Path("getGroupsTree")
  @Operation(summary = "Gets list of groups entities from the parent group root.", method = "GET", description = "Returns a list of group entities in the following cases if the authenticated user is an administrator.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getGroupsTree(@Context UriInfo uriInfo) throws Exception {
    List<DataEntity> groupNodesDataEntities = buildGroupTree();

    CollectionEntity collectionGroupEntity = new CollectionEntity(groupNodesDataEntities,
                                                                  EntityBuilder.ORGANIZATION_GROUP_TYPE,
                                                                  0,
                                                                  10);

    return EntityBuilder.getResponse(collectionGroupEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @RolesAllowed("administrators")
  @Produces("application/vnd.ms-excel")
  @Path("getExport")
  @Operation(summary = "Gets CSV report", method = "GET", description = "Given a (space,group,action,groupBindingId), it return all lines of the report in a csv file")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getReport(@Context UriInfo uriInfo,
                            @Parameter(description = "spaceId", required = true) @QueryParam("spaceId") String spaceId,
                            @Parameter(description = "action", required = true) @QueryParam("action") String action,
                            @Parameter(description = "group", required = true) @QueryParam("group") String group,
                            @Parameter(description = "groupBindingId") @QueryParam("groupBindingId") String groupBindingId) throws Exception {
    List<GroupSpaceBindingReportUser> reports = groupSpaceBindingService.findReportsForCsv(Long.parseLong(spaceId),
                                                                                           Long.parseLong(groupBindingId),
                                                                                           group,
                                                                                           action);

    String csvString = computeCSV(spaceId, group, action, reports);
    try {
      String filename = action.substring(0, 1).toUpperCase() + action.substring(1, action.length()).toLowerCase() + "Binding_";
      filename += spaceService.getSpaceById(spaceId).getPrettyName() + "_";
      if (group.charAt(0) == '/') {
        group = group.substring(1, group.length());
      }
      filename += group.replace("/", "_") + "_";
      filename += formater.format(new Date());

      File temp = null;
      temp = File.createTempFile(filename, ".csv");
      temp.deleteOnExit();
      BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
      bw.write(csvString);
      bw.close();

      Response.ResponseBuilder response = Response.ok((Object) temp);
      response.header("Content-Disposition", "attachment; filename=" + filename + ".csv");
      return response.build();
    } catch (Exception e) {
      LOG.error("Error when creating temp file");
      return Response.serverError().build();

    }

  }

  @GET
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @Path("getBindingReportOperations")
  @Operation(summary = "Gets list of groups entities from the parent group root.", method = "GET", description = "Returns a list of group entities in the following cases if the authenticated user is an administrator.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getBindingReportOperations(@Context UriInfo uriInfo) throws Exception {
    // Get binding operations from the binding queue
    List<GroupSpaceBindingOperationReport> bindingOperationReports =
                                                                   groupSpaceBindingService.getGroupSpaceBindingReportOperations();

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();

    List<DataEntity> bindingOperationReportsDataEntities = new ArrayList<>();
    for (GroupSpaceBindingOperationReport bindingOperationReport : bindingOperationReports) {
      GroupSpaceBindingOperationReportEntity operationReportEntity =
                                                                   EntityBuilder.buildEntityFromGroupSpaceBindingOperationReport(bindingOperationReport);
      // Set group entity to the operation report entity
      OrganizationService organizationService = CommonsUtils.getOrganizationService();
      Group group = organizationService.getGroupHandler().findGroupById(bindingOperationReport.getGroup());
      GroupNodeEntity groupNodeEntity = group != null ? EntityBuilder.buildEntityFromGroup(group)
                                                      : EntityBuilder.buildEntityFromGroupId(bindingOperationReport.getGroup());
      operationReportEntity.setGroup(groupNodeEntity.getDataEntity());

      // Set the space entity to the operation report entity.
      Space space = spaceService.getSpaceById(Long.toString(bindingOperationReport.getSpaceId()));
      SpaceEntity spaceEntity = EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), null);
      operationReportEntity.setSpace(spaceEntity.getDataEntity());
      bindingOperationReportsDataEntities.add(operationReportEntity.getDataEntity());
    }

    CollectionEntity collectionBindingReports = new CollectionEntity(bindingOperationReportsDataEntities,
                                                                     EntityBuilder.GROUP_SPACE_BINDING_REPORT_OPERATIONS_TYPE,
                                                                     0,
                                                                     10);

    return EntityBuilder.getResponse(collectionBindingReports, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  private List<DataEntity> buildGroupTree() throws Exception {
    OrganizationService organizationService = CommonsUtils.getOrganizationService();
    Collection<Group> allGroups = organizationService.getGroupHandler().getAllGroups();
    // get rid of space groups
    List<Group> groups = allGroups.stream().filter(group -> !group.getId().startsWith("/spaces")).collect(Collectors.toList());

    // Find nodes with highest level
    int highestLevel = 0;
    for (Group group : groups) {
      int level = StringUtils.countMatches(group.getId(), "/");
      highestLevel = highestLevel > level ? highestLevel : level;
    }

    // convert to group node entities
    List<GroupNodeEntity> allGroupNodesEntities = new ArrayList<>();
    for (Group group : groups) {
      GroupNodeEntity groupNodeEntity = EntityBuilder.buildEntityFromGroup(group);
      allGroupNodesEntities.add(groupNodeEntity);
    }

    // Get root child groups. entities
    List<GroupNodeEntity> rootChildrenEntities = allGroupNodesEntities.stream()
                                                                      .filter(child -> child.getParentId().equals("root"))
                                                                      .collect(Collectors.toList());
    allGroupNodesEntities.removeAll(rootChildrenEntities);

    for (int i = highestLevel; i > 1; i--) {

      // Get bottom group node entities
      List<GroupNodeEntity> bottomGroupNodesEntities = getBottomGroupEntities(allGroupNodesEntities, i);
      List<GroupNodeEntity> bottomEntities = new ArrayList<>();
      bottomEntities.addAll(bottomGroupNodesEntities);

      // Build from children entities.
      for (GroupNodeEntity groupNodeEntity : bottomGroupNodesEntities) {
        if (bottomEntities.contains(groupNodeEntity)) {
          GroupNodeEntity parentEntity = getParentEntityOf(groupNodeEntity, allGroupNodesEntities);
          // If parent is null then its a direct child of a rootChild.
          if (parentEntity != null) {
            allGroupNodesEntities.remove(parentEntity);
            List<GroupNodeEntity> childrenEntities = getChildrenEntitiesOf(parentEntity, allGroupNodesEntities);
            parentEntity.setChildGroupNodesEntities(convertToChildrenDataEntities(childrenEntities));
            // replaceParent
            allGroupNodesEntities.add(parentEntity);
            allGroupNodesEntities.removeAll(childrenEntities);
            bottomEntities.removeAll(childrenEntities);
          }
        }
      }
    }

    // Finally set rootChildren's children.
    List<GroupNodeEntity> rootChildGroupNodesEntities = new ArrayList<>();
    for (GroupNodeEntity rootChildEntity : rootChildrenEntities) {
      List<GroupNodeEntity> childrenEntities = getChildrenEntitiesOf(rootChildEntity, allGroupNodesEntities);
      rootChildEntity.setChildGroupNodesEntities(convertToChildrenDataEntities(childrenEntities));
      rootChildGroupNodesEntities.add(rootChildEntity);
    }

    // Return list of data entities
    List<DataEntity> groupNodesDataEntities = new ArrayList<>();
    for (GroupNodeEntity entity : rootChildGroupNodesEntities) {
      groupNodesDataEntities.add(entity.getDataEntity());
    }

    return groupNodesDataEntities;

  }

  private List<GroupNodeEntity> getBottomGroupEntities(List<GroupNodeEntity> allGroupNodesEntities, int highestLevel) {
    List<GroupNodeEntity> bottomGroupNodesEntities = new ArrayList<>();
    for (GroupNodeEntity groupNodeEntity : allGroupNodesEntities) {
      if (StringUtils.countMatches(groupNodeEntity.getId(), "/") == highestLevel) {
        bottomGroupNodesEntities.add(groupNodeEntity);
      }
    }
    return bottomGroupNodesEntities;
  }

  private GroupNodeEntity getParentEntityOf(GroupNodeEntity groupNodeEntity, List<GroupNodeEntity> groupNodesEntities) {
    GroupNodeEntity parentNodeEntity = null;
    for (GroupNodeEntity parentEntity : groupNodesEntities) {
      if (parentEntity.getId().equals(groupNodeEntity.getParentId())) {
        parentNodeEntity = parentEntity;
      }
    }
    return parentNodeEntity;
  }

  private List<GroupNodeEntity> getChildrenEntitiesOf(GroupNodeEntity groupNodeEntity, List<GroupNodeEntity> groupNodeEntities) {
    List<GroupNodeEntity> childrenEntities = new ArrayList<>();
    for (GroupNodeEntity childEntity : groupNodeEntities) {
      if (childEntity.getParentId().equals(groupNodeEntity.getId())) {
        childrenEntities.add(childEntity);
      }
    }
    return childrenEntities;
  }

  private List<DataEntity> convertToChildrenDataEntities(List<GroupNodeEntity> childrenEntities) {
    List<DataEntity> childrenDataEntities = new ArrayList<>();
    for (GroupNodeEntity childGroupNodeEntity : childrenEntities) {
      childrenDataEntities.add(childGroupNodeEntity.getDataEntity());
    }
    return childrenDataEntities;
  }

  private List<GroupSpaceBindingOperationReport> convertBindingQueueListToReportOperations(List<GroupSpaceBindingQueue> bindingQueueList) {
    List<GroupSpaceBindingOperationReport> bindingOperationReports =
                                                                   bindingQueueList.stream()
                                                                                   .map(bindingQueue -> convertBindingQueueToOperationReport(bindingQueue))
                                                                                   .collect(Collectors.toList());
    return bindingOperationReports;
  }

  private GroupSpaceBindingOperationReport convertBindingQueueToOperationReport(GroupSpaceBindingQueue bindingQueue) {
    GroupSpaceBindingOperationReport bindingOperationReport = new GroupSpaceBindingOperationReport();
    bindingOperationReport.setSpaceId(Long.parseLong(bindingQueue.getGroupSpaceBinding().getSpaceId()));
    bindingOperationReport.setGroup(bindingQueue.getGroupSpaceBinding().getGroup());
    bindingOperationReport.setAction(bindingQueue.getAction());
    bindingOperationReport.setGroupSpaceBindingId(bindingQueue.getGroupSpaceBinding().getId());
    return bindingOperationReport;
  }

  private String computeCSV(String spaceId, String group, String action, List<GroupSpaceBindingReportUser> reports) {
    StringBuilder sbResult = new StringBuilder();

    Space space = spaceService.getSpaceById(spaceId);
    sbResult.append(space.getDisplayName() + "\n");
    sbResult.append(group + "\n");
    sbResult.append(action + "\n");
    if (reports.size() != 0) {
      sbResult.append(reports.get(0).getDate() + "\n");
      sbResult.append(reports.get(reports.size() - 1).getDate() + "\n");
    }
    // headers :
    sbResult.append("Username,Action,Date,Present Before,Still in space\n");

    reports.stream().forEach(groupSpaceBindingReport -> {
      sbResult.append(groupSpaceBindingReport.getUsername() + ",");
      
      if (!groupSpaceBindingReport.getGroupSpaceBindingReportAction().getAction().equals(GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION)  &&
          (groupSpaceBindingReport.getAction().equals(GroupSpaceBindingReportUser.ACTION_REMOVE_USER)
          && (groupSpaceBindingReport.isWasPresentBefore() | groupSpaceBindingReport.isStillInSpace()))) {
        //if the current global action is not synchronize and
        // if the action is "remove", and the user present in the
        // space before, then, he was not removed
        // if the action is "remove", and the user is still present
        // in the space
        // so, display nothing for the action in the report
        
        
        sbResult.append(",");
      } else {
        // else, display the action
  
        //if the current global action is synchronize, we display the action in all cases, even if user was present before
        //because in this case, he is removed
        
        sbResult.append(groupSpaceBindingReport.getAction() + ",");
      }
      sbResult.append(groupSpaceBindingReport.getDate() + ",");
      sbResult.append(groupSpaceBindingReport.isWasPresentBefore() + ",");

      if (groupSpaceBindingReport.getAction().equals(GroupSpaceBindingReportUser.ACTION_REMOVE_USER)) {
        // in case of add action, stillPresentInSpace is not relevant, so do not display
        sbResult.append(groupSpaceBindingReport.isStillInSpace());
      }
      sbResult.append("\n");

    });

    return sbResult.toString();
  }
}
