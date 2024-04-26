package org.exoplatform.social.rest.suggest;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.RuntimeDelegate;

import org.json.JSONArray;
import org.json.JSONObject;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.deprecation.DeprecatedAPI;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.service.rest.Util;

@Path("/homepage/intranet/people/")
@Produces("application/json")
@Deprecated
public class PeopleRestServices implements ResourceContainer {

  private static final int NUMBER_OF_SUGGESTIONS = 10;
  private static Log log = ExoLogger.getLogger(PeopleRestServices.class);

  private static final CacheControl cacheControl;

  private UserACL userACL;

  private IdentityManager identityManager;

  private  RelationshipManager relationshipManager;

  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }
  public PeopleRestServices(UserACL userACL, IdentityManager identityManager,  RelationshipManager relationshipManager) {
    this.userACL = userACL;
    this.identityManager = identityManager;
    this.relationshipManager =  relationshipManager;
  }

  @GET
  @Path("contacts/suggestions")
  @DeprecatedAPI("Use UserRestResourcesV1.getUsers instead")
  public Response getSuggestions(@Context SecurityContext sc, @Context UriInfo uriInfo) {

    try {

      String userId = getUserId(sc, uriInfo);
      if (userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
      }

      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);

      ListAccess<Identity> connectionList = relationshipManager.getConnections(identity);
      int size = connectionList.getSize();
      Map<Identity, Integer> connectionsSuggestions;
      if (size > 0) {
        connectionsSuggestions = relationshipManager.getSuggestions(identity, 20, 50, 10);
        if (connectionsSuggestions.size() == 1 && connectionsSuggestions.keySet().iterator().next().getRemoteId().equals(userACL.getSuperUser())) {
          // The only suggestion is the super user so we clear the suggestion list
          connectionsSuggestions = Collections.emptyMap();
        }
      } else {
        connectionsSuggestions = Collections.emptyMap();
      }

      JSONObject jsonGlobal = new JSONObject();
      JSONArray jsonArray = new JSONArray();
      Map<Identity, Integer> suggestions = new LinkedHashMap<>(connectionsSuggestions);
      suggestions = suggestions.entrySet()
              .stream()
              .sorted((Map.Entry.<Identity, Integer>comparingByValue().reversed()))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

      if (suggestions.size() < NUMBER_OF_SUGGESTIONS) {
        // Returns the last users
        List<Identity> identities = identityManager.getLastIdentities(NUMBER_OF_SUGGESTIONS - suggestions.size());
        for (Identity id : identities) {
          if (identity.equals(id) || relationshipManager.get(identity, id) != null)
            continue;
          suggestions.putIfAbsent(id, 0);
        }
      }
      for (Entry<Identity, Integer> suggestion : suggestions.entrySet()) {
        Identity id = suggestion.getKey();
        if (id == null) {
          continue;
        }
        boolean isExternal = Util.isExternal(id.getId());
        if ((id.getRemoteId().equals(userACL.getSuperUser())) || isExternal) continue;
        JSONObject json = new JSONObject();
        Profile socialProfile = id.getProfile();
        String avatar = socialProfile.getAvatarUrl();
        if (avatar == null) {
          avatar = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
        }
        String position = socialProfile.getPosition();
        if (position == null) {
          position = "";
        }
        json.put("username", id.getRemoteId());
        json.put("suggestionName", socialProfile.getFullName());
        json.put("suggestionId", id.getId());
        json.put("avatar", avatar);
        json.put("profile", socialProfile.getUrl());
        json.put("title", position);

        //set mutual friend number
        json.put("number", suggestion.getValue());
        json.put("createdDate",socialProfile.getCreatedTime());
        jsonArray.put(json);
      }
      jsonGlobal.put("items",jsonArray);
      jsonGlobal.put("noConnections", size);
      jsonGlobal.put("username", userId);
      return Response.ok(jsonGlobal.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      log.error("Error in getting GS progress: " + e.getMessage(), e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }

  }

  private String getUserId(SecurityContext sc, UriInfo uriInfo) {
    try {
      return sc.getUserPrincipal().getName();
    } catch (Exception e) {
      return null;
    }
  }

}
