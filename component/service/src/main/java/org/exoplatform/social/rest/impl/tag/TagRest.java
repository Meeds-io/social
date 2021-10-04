package org.exoplatform.social.rest.impl.tag;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagFilter;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.annotations.*;

@Path(VersionResources.VERSION_ONE + "/social/tags")
@Api(
    tags = VersionResources.VERSION_ONE + "/social/tags",
    value = VersionResources.VERSION_ONE + "/social/tags",
    description = "Managing tags for any type of data" // NOSONAR
)
public class TagRest implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(TagRest.class);

  private TagService       tagService;

  public TagRest(TagService tagService) {
    this.tagService = tagService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Find list of tags using a search term",
      httpMethod = "POST",
      response = Response.class,
      produces = MediaType.APPLICATION_JSON,
      notes = "Returns list of tags"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
      }
  )
  public Response findTags(
                           @ApiParam(
                               value = "Search term",
                               required = false,
                               defaultValue = "false"
                           )
                           @QueryParam("q")
                           String term,
                           @ApiParam(
                               value = "Search results limit",
                               required = false,
                               defaultValue = "false"
                           )
                           @QueryParam("limit")
                           long limit) {
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      List<TagName> tagNames = tagService.findTags(new TagFilter(term, limit), userIdentityId);
      return Response.ok(tagNames).build();
    } catch (Exception e) {
      LOG.warn("Error getting list of tags", e);
      return Response.serverError().build();
    }
  }

}
