/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.portlet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.thumbnail.ImageThumbnailService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.image.plugin.FileThumbnailPlugin;
import io.meeds.social.util.JsonUtils;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

/**
 * Portlet listing the sub-spaces of the space whose page hosts it. The class
 * is glue only: preferences in, {@link SpaceService} calls, nothing decided
 * here. Its three preferences are declared in {@code portlet.xml} and written
 * only through {@link #processAction}, under the parent space management
 * guard, so that {@code showHiddenSubspaces} — the flag that lets a page
 * expose hidden sub-spaces to non-members — is never influenced by a client
 * value.
 */
public class SubspacesListPortlet extends GenericDispatchedViewPortlet {

  public static final String        HEADER_TRANSLATIONS_PREFERENCE   = "headerTranslations";

  public static final String        SHOW_HIDDEN_SUBSPACES_PREFERENCE = "showHiddenSubspaces";

  public static final String        SUBSPACES_LIMIT_PREFERENCE       = "subspacesLimit";

  public static final int           DEFAULT_SUBSPACES_LIMIT          = 4;

  public static final int           MIN_SUBSPACES_LIMIT              = 1;

  public static final int           MAX_SUBSPACES_LIMIT              = 25;

  public static final String        LIMIT_OUT_OF_RANGE_MESSAGE       = "subspacesList.limit.outOfRange";

  public static final String        HEADER_TRANSLATIONS_INVALID_MESSAGE = "subspacesList.headerTranslations.invalid";

  public static final String        SHOW_HIDDEN_SUBSPACES_INVALID_MESSAGE = "subspacesList.showHiddenSubspaces.invalid";

  public static final String        LIMIT_PARAMETER                  = "limit";

  /**
   * Resource id of the second resource this portlet serves: the avatar of one
   * listed sub-space ({@code <portlet:resourceURL id="avatar">} in the JSP).
   */
  public static final String        AVATAR_RESOURCE_ID               = "avatar";

  public static final String        SPACE_ID_PARAMETER               = "spaceId";

  public static final String        SPACE_ID_INVALID_MESSAGE         = "subspacesList.spaceId.invalid";

  /**
   * Bodies of the 404 and 403 answers. A fixed message code per status, as
   * {@code backend-spring.md} §5 asks: the Service's own sentence names the
   * acting user and the space id, which is troubleshooting detail for
   * {@code LOG.debug}, not something to hand a client.
   */
  public static final String        PARENT_SPACE_NOT_FOUND_MESSAGE   = "subspacesList.parentSpace.notFound";

  public static final String        ACCESS_DENIED_MESSAGE            = "subspacesList.accessDenied";

  /**
   * Browser cache of a widget-served avatar. The URL carries no
   * last-modified marker, so the cache is short and private.
   */
  public static final String        AVATAR_CACHE_CONTROL             = "private, max-age=3600";

  /**
   * Same thumbnail size as the space avatar REST endpoint's default, so both
   * paths serve the same bytes for the same space.
   */
  public static final int           AVATAR_THUMBNAIL_SIZE            = 100;

  public static final String        DEFAULT_AVATAR_MIME_TYPE         = "image/png";

  /**
   * Hard bound of one resource call. The 'see more' drawer lists every
   * sub-space at once (no pagination yet), so the cap is what keeps one parent
   * with an unexpected number of sub-spaces from making the call unbounded. A
   * constant rather than a property: the real bound is the parent template's
   * subspacesMaxLimit, and beyond this the answer is pagination, not a bigger
   * number.
   */
  public static final int           MAX_RESOURCE_LIMIT               = 500;

  /**
   * Statuses of the resource phase, following the platform's exception to
   * status contract: not found, then access, then parameter validation.
   * Declared here because this module has no servlet API on its classpath.
   */
  private static final int          STATUS_BAD_REQUEST               = 400;

  private static final int          STATUS_FORBIDDEN                 = 403;

  private static final int          STATUS_NOT_FOUND                 = 404;

  private static final Log          LOG                              = ExoLogger.getLogger(SubspacesListPortlet.class);

  /**
   * The only preference names {@link #processAction} stores. Any other posted
   * parameter is ignored: a blanket copy would let a space admin store
   * arbitrary preference names on the page window.
   */
  private static final List<String> STORED_PREFERENCES               = List.of(HEADER_TRANSLATIONS_PREFERENCE,
                                                                               SHOW_HIDDEN_SUBSPACES_PREFERENCE,
                                                                               SUBSPACES_LIMIT_PREFERENCE);

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifySettings(request.getRemoteUser())) {
      throw new PortletException("User is not allowed to save settings");
    }
    PortletPreferences preferences = request.getPreferences();
    for (String name : STORED_PREFERENCES) {
      String value = request.getParameter(name);
      if (value != null) {
        preferences.setValue(name, normalizePreferenceValue(name, value));
      }
    }
    preferences.store();
  }

  private String normalizePreferenceValue(String name, String value) throws PortletException {
    if (StringUtils.equals(name, SUBSPACES_LIMIT_PREFERENCE)) {
      int limit = NumberUtils.toInt(value, -1);
      if (limit < MIN_SUBSPACES_LIMIT || limit > MAX_SUBSPACES_LIMIT) {
        throw new PortletException(LIMIT_OUT_OF_RANGE_MESSAGE);
      }
      return String.valueOf(limit);
    } else if (StringUtils.equals(name, SHOW_HIDDEN_SUBSPACES_PREFERENCE)) {
      // the flag exposes hidden sub-spaces to non-members: only an explicit true/false is stored
      if (!StringUtils.equalsAny(value, "true", "false")) {
        throw new PortletException(SHOW_HIDDEN_SUBSPACES_INVALID_MESSAGE);
      }
      return value;
    } else {
      // headerTranslations: a JSON object (lang -> label), labels being
      // strings, re-serialized so that the JSP always hands the widget
      // something JSON.parse accepts and the title prop can render
      try {
        JSONObject translations = new JSONObject(value);
        for (String lang : translations.keySet()) {
          if (!(translations.get(lang) instanceof String)) {
            throw new PortletException(HEADER_TRANSLATIONS_INVALID_MESSAGE);
          }
        }
        return translations.toString();
      } catch (JSONException e) {
        throw new PortletException(HEADER_TRANSLATIONS_INVALID_MESSAGE, e);
      }
    }
  }

  /**
   * Answers the widget with everything it needs to render, in one envelope:
   * whether the hosting space is a parent space at all, what the viewer may do
   * on it, and the sub-spaces they may see. Every decision is the
   * {@link SpaceService}'s; this method only reads the trusted preference and
   * the page context.
   * <p>
   * The space is resolved from the page ({@link SpaceUtils#getSpaceByContext()}),
   * never from a request parameter, and {@code showHiddenSubspaces} is read
   * from the portlet preferences, never from the request: a client value must
   * not be able to widen what is listed.
   */
  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    if (AVATAR_RESOURCE_ID.equals(request.getResourceID())) {
      serveSubspaceAvatar(request, response);
      return;
    }
    response.setContentType("application/json");
    String username = request.getRemoteUser();
    Space space = SpaceUtils.getSpaceByContext();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);

    if (space == null || !spaceService.isParentSpace(space)) {
      // no space context, a sub-space, or a template allowing no sub-space:
      // nothing to show and no listing query
      writeJson(response, SubspacesEnvelope.notParentSpace());
      return;
    }

    try {
      int limit = parseLimit(request.getParameter(LIMIT_PARAMETER));
      boolean showHiddenSubspaces = Boolean.parseBoolean(request.getPreferences()
                                                                .getValue(SHOW_HIDDEN_SUBSPACES_PREFERENCE, "false"));
      List<Space> subspaces = spaceService.getSubspaces(space.getSpaceId(), username, showHiddenSubspaces, 0, limit);
      writeJson(response,
                new SubspacesEnvelope(true,
                                      spaceService.canManageSpace(space, username),
                                      spaceService.canCreateSubspace(space, username, request.getLocale()),
                                      subspaces.stream().map(subspace -> toItem(spaceService, subspace, username)).toList(),
                                      storedSettings(request.getPreferences())));
    } catch (ObjectNotFoundException e) {
      LOG.debug("Parent space {} not found while listing its subspaces", space.getSpaceId(), e);
      sendError(response, STATUS_NOT_FOUND, PARENT_SPACE_NOT_FOUND_MESSAGE);
    } catch (IllegalAccessException e) {
      LOG.debug("User {} isn't allowed to list subspaces of space {}", username, space.getSpaceId(), e);
      sendError(response, STATUS_FORBIDDEN, ACCESS_DENIED_MESSAGE);
    } catch (IllegalArgumentException e) {
      sendError(response, STATUS_BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Streams the avatar of one sub-space the widget lists. The space avatar
   * REST endpoint answers 404 for a HIDDEN and CLOSED space to a viewer who is
   * neither member nor invited, so a hidden sub-space exposed through
   * {@code showHiddenSubspaces} could not show its image otherwise.
   * <p>
   * No access rule is written here: the sub-space is served if and only if
   * {@link SpaceService#getSubspaces} lists it to this viewer under the same
   * preference the listing uses — the Service's decision, applied a second
   * time. Statuses follow the platform contract: 404 when the page hosts no
   * parent space, when the id is not one of the viewer's sub-spaces or when
   * the space has no avatar (the client then shows the default image), 403
   * when the viewer cannot view the parent, 400 on an unusable id.
   * <p>
   * Cost: one listing query per image, over as many rows as the <em>caller</em>
   * was given — the widget's own {@code subspacesLimit + 1} by default, and
   * the number of rows the "see more" drawer received when it asks for them
   * (the optional {@value #LIMIT_PARAMETER} parameter, capped like the listing
   * resource's own). Two callers, two bounds, because they render different
   * lists: the widget shows its first rows and the drawer shows all of them,
   * and a hidden sub-space past the widget's limit would otherwise be answered
   * 404 and show the default image in the drawer while showing its real avatar
   * in the widget. Keeping the default at the widget's limit is what stops the
   * drawer's price being paid on every page render.
   * <p>
   * The parameter widens nothing: it moves the size of the window scanned
   * inside a listing the Service has already filtered for this viewer, and the
   * same client may ask {@link #serveResource} for the same 500 rows anyway.
   * <p>
   * The proper fix is a Service method answering "is this one sub-space
   * listed to this viewer" instead of a list — same filter, restricted to the
   * one id, one row instead of a window. It is not done here because
   * {@code SpaceFilter} carries no id set and
   * {@code XSpaceFilter.setSpaceFilter} would drop one added to it, the same
   * copy that already drops {@code extraStatus}: reaching it means changing
   * shared listing code, which does not belong in this diff. It stays a
   * follow-up, and the per-caller bound is what keeps the answer correct
   * until then.
   */
  private void serveSubspaceAvatar(ResourceRequest request, ResourceResponse response) throws IOException {
    String username = request.getRemoteUser();
    Space parentSpace = SpaceUtils.getSpaceByContext();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    if (parentSpace == null || !spaceService.isParentSpace(parentSpace)) {
      sendError(response, STATUS_NOT_FOUND, null);
      return;
    }
    long subspaceId = NumberUtils.toLong(request.getParameter(SPACE_ID_PARAMETER), -1);
    if (subspaceId <= 0) {
      sendError(response, STATUS_BAD_REQUEST, SPACE_ID_INVALID_MESSAGE);
      return;
    }
    long bound;
    try {
      bound = avatarListingBound(request);
    } catch (IllegalArgumentException e) {
      sendError(response, STATUS_BAD_REQUEST, e.getMessage());
      return;
    }
    try {
      PortletPreferences preferences = request.getPreferences();
      boolean showHiddenSubspaces = Boolean.parseBoolean(preferences.getValue(SHOW_HIDDEN_SUBSPACES_PREFERENCE, "false"));
      Space subspace = spaceService.getSubspaces(parentSpace.getSpaceId(), username, showHiddenSubspaces, 0, bound)
                                   .stream()
                                   .filter(listed -> listed.getSpaceId() == subspaceId)
                                   .findFirst()
                                   .orElse(null);
      if (subspace == null) {
        sendError(response, STATUS_NOT_FOUND, null);
        return;
      }
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, subspace.getPrettyName());
      FileItem avatarFile = spaceIdentity == null ? null : identityManager.getAvatarFile(spaceIdentity);
      if (avatarFile == null) {
        sendError(response, STATUS_NOT_FOUND, null);
        return;
      }
      FileInfo fileInfo = avatarFile.getFileInfo();
      response.setContentType(StringUtils.defaultIfBlank(fileInfo == null ? null : fileInfo.getMimetype(),
                                                         DEFAULT_AVATAR_MIME_TYPE));
      response.setProperty("Cache-Control", AVATAR_CACHE_CONTROL);
      response.getPortletOutputStream().write(getThumbnailOrOriginal(avatarFile));
    } catch (ObjectNotFoundException e) {
      LOG.debug("Parent space {} not found while serving a subspace avatar", parentSpace.getSpaceId(), e);
      sendError(response, STATUS_NOT_FOUND, PARENT_SPACE_NOT_FOUND_MESSAGE);
    } catch (IllegalAccessException e) {
      LOG.debug("User {} isn't allowed to list subspaces of space {}", username, parentSpace.getSpaceId(), e);
      sendError(response, STATUS_FORBIDDEN, ACCESS_DENIED_MESSAGE);
    }
  }

  /**
   * The {@value #AVATAR_THUMBNAIL_SIZE}px thumbnail of an avatar, as the space
   * avatar REST endpoint serves by default; the original bytes when the file
   * carries no id or the thumbnail cannot be produced (same fall-back as the
   * endpoint), so an uploaded logo is not streamed whole for a 37px avatar.
   */
  private byte[] getThumbnailOrOriginal(FileItem avatarFile) {
    FileInfo fileInfo = avatarFile.getFileInfo();
    if (fileInfo == null) {
      return avatarFile.getAsByte();
    }
    try {
      FileItem thumbnail = CommonsUtils.getService(ImageThumbnailService.class)
                                       .getOrCreateThumbnail(FileThumbnailPlugin.FILE_TYPE,
                                                             Long.toString(fileInfo.getId()),
                                                             fileInfo.getUpdater(),
                                                             AVATAR_THUMBNAIL_SIZE,
                                                             AVATAR_THUMBNAIL_SIZE);
      return thumbnail == null ? avatarFile.getAsByte() : thumbnail.getAsByte();
    } catch (Exception e) {
      LOG.warn("Error while resizing avatar file {}, original image will be returned", fileInfo.getId(), e);
      return avatarFile.getAsByte();
    }
  }

  /**
   * The rows the image path looks through for one caller: what it asked for
   * when it says so, and otherwise the rows the widget itself renders.
   *
   * @param request the avatar resource request
   * @return the listing bound, never above {@link #MAX_RESOURCE_LIMIT}
   * @throws IllegalArgumentException when the parameter is present and is not
   *           a strictly positive number
   */
  private long avatarListingBound(ResourceRequest request) {
    String requested = request.getParameter(LIMIT_PARAMETER);
    if (StringUtils.isBlank(requested)) {
      // the rows the widget renders, and no more: the extra one is the same
      // 'see more' row the listing asks for
      return storedLimit(request.getPreferences()) + 1L;
    }
    return parseLimit(requested);
  }

  /**
   * @param value the requested limit, absent when the drawer asks for the whole
   *          list
   * @return the number of sub-spaces to load, never above
   *         {@link #MAX_RESOURCE_LIMIT}
   * @throws IllegalArgumentException when the parameter is present and is not a
   *           strictly positive number
   */
  private int parseLimit(String value) {
    if (StringUtils.isBlank(value)) {
      return MAX_RESOURCE_LIMIT;
    }
    int limit = NumberUtils.toInt(value, -1);
    if (limit <= 0) {
      throw new IllegalArgumentException(LIMIT_OUT_OF_RANGE_MESSAGE);
    }
    return Math.min(limit, MAX_RESOURCE_LIMIT);
  }

  private SubspaceItem toItem(SpaceService spaceService, Space subspace, String username) {
    return new SubspaceItem(subspace.getId(),
                            subspace.getDisplayName(),
                            subspace.getPrettyName(),
                            subspace.getAvatarUrl(),
                            subspace.getVisibility(),
                            spaceService.isMember(subspace, username),
                            spaceService.isInvitedUser(subspace, username));
  }

  private void writeJson(ResourceResponse response, SubspacesEnvelope envelope) throws IOException {
    response.getWriter().write(JsonUtils.toJsonString(envelope));
  }

  private void sendError(ResourceResponse response, int status, String message) throws IOException {
    response.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(status));
    response.setContentType("text/plain");
    response.getWriter().write(StringUtils.defaultString(message));
  }

  /**
   * What the widget renders from. {@code parentSpace} false means the widget
   * hides its whole application: the other fields are then empty and no listing
   * query ran.
   * <p>
   * {@code settings} is what the preferences <em>hold</em>, not what a client
   * asked to store: it is how the drawer learns whether its save was applied.
   * The action phase answers 200 whatever happens — a {@link PortletException}
   * thrown by {@link #processAction} is rethrown by the portal's
   * {@code UIPortletActionListener} and swallowed by
   * {@code PortalRequestHandler}, which logs it and commits the response
   * untouched — so the transport cannot tell a stored value from a refused
   * one, and only reading the preferences back can.
   */
  public record SubspacesEnvelope(boolean parentSpace,
                                  boolean canManageSpace,
                                  boolean canCreateSubspace,
                                  List<SubspaceItem> subspaces,
                                  StoredSettings settings) {
    public static SubspacesEnvelope notParentSpace() {
      return new SubspacesEnvelope(false, false, false, Collections.emptyList(), null);
    }
  }

  /**
   * The three preferences as they are stored, echoed to the widget so that a
   * save can be confirmed against them.
   */
  public record StoredSettings(Map<String, Object> headerTranslations, boolean showHiddenSubspaces, int subspacesLimit) {
  }

  private StoredSettings storedSettings(PortletPreferences preferences) {
    return new StoredSettings(storedHeaderTranslations(preferences),
                              Boolean.parseBoolean(preferences.getValue(SHOW_HIDDEN_SUBSPACES_PREFERENCE, "false")),
                              storedLimit(preferences));
  }

  /**
   * The stored translations as a map, so the widget receives an object and not
   * a string to parse. Unreadable content answers an empty map rather than
   * failing the whole envelope: {@link #processAction} is what keeps the value
   * well formed, and a widget with no header title falls back to its default
   * label.
   */
  private Map<String, Object> storedHeaderTranslations(PortletPreferences preferences) {
    try {
      // a preference explicitly stored with a null value reads back as null,
      // which JSONObject rejects with an NPE rather than a JSONException
      return new JSONObject(StringUtils.defaultIfBlank(preferences.getValue(HEADER_TRANSLATIONS_PREFERENCE, "{}"), "{}")).toMap();
    } catch (JSONException e) {
      LOG.debug("Unreadable stored header translations, answering none", e);
      return Collections.emptyMap();
    }
  }

  /**
   * The stored number of rows, brought back inside
   * {@link #MIN_SUBSPACES_LIMIT}..{@link #MAX_SUBSPACES_LIMIT}.
   * <p>
   * {@link #processAction} refuses a value outside those bounds, but it is
   * not the only writer: a preference imported through the layout editor
   * bypasses it entirely, so every <em>reader</em> clamps. This one is shared
   * with the JSP rather than duplicated there, so the three places that read
   * the preference cannot drift apart: an unclamped value feeds both the
   * avatar resource's listing bound — where a large one is a big query on the
   * render path and a negative one makes the Service throw
   * {@code IllegalArgumentException}, which that method does not catch — and
   * the envelope the widget re-seats its settings from, where a negative one
   * makes the widget's own {@code slice} drop rows from the end.
   *
   * @param preferences the portlet preferences to read
   * @return the stored limit, never outside the bounds the widget can render
   */
  public static int storedLimit(PortletPreferences preferences) {
    return Math.min(MAX_SUBSPACES_LIMIT,
                    Math.max(MIN_SUBSPACES_LIMIT,
                             NumberUtils.toInt(preferences.getValue(SUBSPACES_LIMIT_PREFERENCE, null),
                                               DEFAULT_SUBSPACES_LIMIT)));
  }

  /**
   * One listed sub-space. {@code isMember} and {@code isInvited} carry the
   * viewer's relationship to it for the hidden-space rendering only; they are
   * not access decisions, which the Service already made by returning the
   * space at all. Both are needed because the space REST endpoints serve a
   * member <em>and</em> an invited user alike, so a row is only rendered
   * anonymously when the viewer is neither.
   */
  public record SubspaceItem(String id,
                             String displayName,
                             String prettyName,
                             String avatarUrl,
                             String visibility,
                             boolean isMember,
                             boolean isInvited) {
  }

  /**
   * Whether the page this portlet renders on hosts a space that may carry
   * sub-spaces — the same question {@link #serveResource} answers, asked of
   * the same {@link SpaceService} and resolved from the page, never from a
   * request parameter.
   * <p>
   * Read by the JSP so that the widget is <em>not booted at all</em> outside a
   * parent space: it used to boot everywhere, render its loading state and
   * only then remove itself, which flashed a loading placeholder on every
   * space that is not a parent one (PO feedback on EXO-89270).
   * <p>
   * Shared with the JSP the way {@link #storedLimit} is, rather than carried
   * there as a render attribute: {@code GenericDispatchedViewPortlet.doView}
   * copies <em>every</em> stored preference into a request attribute of the
   * same name, after this class has had its turn, and a page editor may store
   * a preference under any name at all
   * ({@code PageLayoutService.updatePageApplicationPreferences}). No attribute
   * name is therefore safe from being answered by a preference.
   *
   * @return {@code true} when the hosting space may carry sub-spaces
   */
  public static boolean isParentSpaceContext() {
    Space space = SpaceUtils.getSpaceByContext();
    return space != null && CommonsUtils.getService(SpaceService.class).isParentSpace(space);
  }

  private boolean canModifySettings(String username) {
    Space space = SpaceUtils.getSpaceByContext();
    // resolved per call, as the sibling ParentSpaceListingPortlet does: the
    // portlet instance is not Spring-managed and must not pin a container
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    return spaceService.canManageSpace(space, username);
  }
}
