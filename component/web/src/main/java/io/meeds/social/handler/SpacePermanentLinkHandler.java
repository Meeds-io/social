/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.handler;

import static io.meeds.social.space.plugin.SpacePermanentLinkPlugin.APPLICATION_URI;
import static io.meeds.social.space.plugin.SpacePermanentLinkPlugin.OBJECT_TYPE;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.security.sso.SSOHelper;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.service.PermanentLinkService;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;

public class SpacePermanentLinkHandler extends WebRequestHandler {

  public static final QualifiedName REQUEST_SPACE_ID = QualifiedName.create("spaceId");

  public static final QualifiedName INVITATION_ID    = QualifiedName.create("invitation_id");

  public static final QualifiedName REQUEST_PATH     = QualifiedName.create("path");

  /**
   * The scheme a link starts with, captured, a port excluded (social
   * NavigationUtils.js, getNavigationNodeHomeUri)
   */
  private static final Pattern      SCHEME_PATTERN            = Pattern.compile("^([a-z][a-z0-9+.-]*):(?!\\d)",
                                                                                Pattern.CASE_INSENSITIVE);

  /**
   * A host name whose last label starts with a letter, an optional port, then a
   * path, a query, a fragment or nothing (social NavigationUtils.js)
   */
  private static final Pattern      HOST_PREFIX_PATTERN       =
                                                        Pattern.compile("^([a-z0-9]([a-z0-9-]*[a-z0-9])?\\.)*[a-z]([a-z0-9-]*[a-z0-9])?(:\\d+)?([/?#]|$)",
                                                                        Pattern.CASE_INSENSITIVE);

  /**
   * A control character, a line break included, which a Location header can't
   * carry
   */
  private static final Pattern      CONTROL_CHARACTER_PATTERN = Pattern.compile("\\p{Cntrl}");

  /** The schemes that run a script or read local content */
  private static final Set<String>  REFUSED_SCHEMES           = Set.of("javascript", "vbscript", "data", "file");

  /**
   * A URI with an authority, with or without a scheme: the authority and what
   * follows it, captured
   */
  private static final Pattern      AUTHORITY_PATTERN         = Pattern.compile("^(?:[a-z][a-z0-9+.-]*:)?//([^/?#]*)(.*)$",
                                                                                Pattern.CASE_INSENSITIVE);

  private SpaceService              spaceService;

  private UserPortalConfigService   portalConfigService;

  private PermanentLinkService      permanentLinkService;

  private NavigationService         navigationService;

  @Override
  public void onInit(WebAppController controller, ServletConfig sConfig) throws Exception {
    super.onInit(controller, sConfig);

    PortalContainer container = PortalContainer.getInstance();
    this.spaceService = container.getComponentInstanceOfType(SpaceService.class);
    this.portalConfigService = container.getComponentInstanceOfType(UserPortalConfigService.class);
    this.permanentLinkService = container.getComponentInstanceOfType(PermanentLinkService.class);
    this.navigationService = container.getComponentInstanceOfType(NavigationService.class);
  }

  @Override
  public String getHandlerName() {
    return "spaces";
  }

  @Override
  protected boolean getRequiresLifeCycle() {
    return true;
  }

  @Override
  public boolean execute(ControllerContext controllerContext) throws Exception {
    String username = controllerContext.getRequest().getRemoteUser();
    String spaceId = controllerContext.getParameter(REQUEST_SPACE_ID);
    String path = controllerContext.getParameter(REQUEST_PATH);
    String queryString = controllerContext.getRequest().getQueryString();
    if (StringUtils.isNotEmpty(queryString)) {
      path = path + "?" + queryString;
    }
    Space space = spaceService.getSpaceById(spaceId);
    if (StringUtils.isBlank(username) && space.getPublicSiteId() == 0) {
      String loginPath = getAuthenticationUrl(controllerContext.getRequest().getRequestURI(),
                                              controllerContext.getRequest().getQueryString());
      controllerContext.getResponse().sendRedirect(loginPath);
    } else if (space == null || (StringUtils.isNotBlank(username) && isHiddenSpace(space, username)
        && space.getRegistration().equalsIgnoreCase(Space.CLOSED) && !spaceService.isInvitedUser(space, username))) {
      String pageNotFoundUrl = "/portal/" + getPageNotFoundSite(username) + "/page-not-found";
      controllerContext.getResponse().sendRedirect(pageNotFoundUrl);
    } else {
      String uri = StringUtils.isBlank(path) ? getSpaceHomeLink(space, username, controllerContext.getRequest()) : null;
      if (uri == null) {
        uri = permanentLinkService.getLink(getPermanentLinkObject(spaceId, path));
      }
      controllerContext.getResponse().sendRedirect(uri);
    }
    return true;
  }

  /**
   * The portal renders a page of type {@link PageType#LINK} as an empty page:
   * only the menus listing its node replace the node's URL with the page's
   * link. The space home is the node the space URL names, so when its page is
   * a link the user can access, the space home redirects to that link, unless
   * the link leads back to this space home entry.
   *
   * @param space the requested space
   * @param username the current user, null when anonymous
   * @param request the current request
   * @return the URI of the space home page's link, or null when that page is
   *         not an accessible link or its link can't be redirected to
   */
  private String getSpaceHomeLink(Space space, String username, HttpServletRequest request) {
    if (StringUtils.isBlank(space.getUrl())) {
      return null;
    }
    NodeContext<NodeContext<Object>> homeNode = navigationService.loadNode(SiteKey.group(space.getGroupId()), space.getUrl());
    PageKey pageRef = homeNode == null || homeNode.getState() == null ? null : homeNode.getState().getPageRef();
    PageContext page = pageRef == null ? null : portalConfigService.getPage(pageRef, username);
    if (page != null && PageType.LINK.name().equals(page.getState().getType())) {
      String uri = toRedirectUri(page.getState().getLink());
      return leadsBackToSpaceHome(uri, username, request, space.getId()) ? null : uri;
    } else {
      return null;
    }
  }

  /**
   * A redirect to this space's own {@code /s/{spaceId}} entry reaches this
   * handler again for the same space home, and so does a redirect to the root
   * of this host or of this context, whatever its query, when the portal sends
   * that root to the user's default path, dropping the query, and that path is
   * this space's entry (a user who made this space their home page): either
   * would loop.
   *
   * @param uri the URI to redirect to, possibly null
   * @param username the current user, null when anonymous
   * @param request the current request
   * @param spaceId the requested space id
   * @return true when redirecting to the URI would lead back to this space
   *         home
   */
  private boolean leadsBackToSpaceHome(String uri, String username, HttpServletRequest request, String spaceId) {
    String path = getRequestedPath(uri, request);
    if (path == null) {
      return false;
    }
    String pathWithoutQuery = StringUtils.removeEnd(StringUtils.substringBefore(path, "?"), "/");
    if (pathWithoutQuery.isEmpty() || pathWithoutQuery.equals(request.getContextPath())) {
      return isSpaceHomeEntry(portalConfigService.getDefaultPath(username), request, spaceId);
    } else {
      return isSpaceHomeEntry(uri, request, spaceId);
    }
  }

  /**
   * This space's own {@code /s/{spaceId}} entry, with no query or an empty
   * one, reaches this handler again for the same space home.
   *
   * @param uri a URI, possibly null
   * @param request the current request
   * @param spaceId the requested space id
   * @return true when the URI is this space home entry
   */
  private static boolean isSpaceHomeEntry(String uri, HttpServletRequest request, String spaceId) {
    return (request.getContextPath() + "/s/" + spaceId).equals(getRequestedPath(uri, request));
  }

  /**
   * The path a URI requests from this host, as the product spells its own
   * links: a path, or a URL whose host is spelled as this request's server
   * name in any letter case, whatever its scheme, port and user, read without
   * its fragment, which stays in the browser, an empty query and a trailing
   * slash. Any other spelling the browser normalises into a path of this host
   * (a dot segment, a backslash, another spelling of an IPv6 literal) is not
   * recognised.
   *
   * @param uri a URI, possibly null
   * @param request the current request
   * @return the path, empty for the root of the host, or null when the URI is
   *         null, a URL of another host or neither a path nor a URL with a host
   */
  private static String getRequestedPath(String uri, HttpServletRequest request) {
    if (uri == null) {
      return null;
    }
    String uriPath = uri;
    Matcher authority = AUTHORITY_PATTERN.matcher(uri);
    if (authority.matches()) {
      String hostAndPort = StringUtils.contains(authority.group(1), '@') ? StringUtils.substringAfterLast(authority.group(1), "@") :
                                                                          authority.group(1);
      String host = hostAndPort.startsWith("[") ? StringUtils.substringBefore(hostAndPort, "]") + "]" :
                                                StringUtils.substringBefore(hostAndPort, ":");
      if (!StringUtils.equalsIgnoreCase(host, request.getServerName())) {
        return null;
      }
      uriPath = authority.group(2);
    } else if (!uri.startsWith("/")) {
      return null;
    }
    String requestedPath = StringUtils.removeEnd(StringUtils.substringBefore(uriPath, "#"), "?");
    return StringUtils.removeEnd(requestedPath, "/");
  }

  /**
   * A link page stores its link as typed, and the node editor accepts a link
   * without a scheme, which the browser would resolve as a path relative to
   * this request if it were sent as is. The link, trimmed and with its
   * characters above {@code ~} percent-encoded, is redirected to when it
   * starts with {@code /} (a path of this site or a protocol-relative link) or
   * with a scheme, and with {@code //} in front when it starts with a host
   * name, so that the browser opens it with the page's own scheme: the
   * {@code http://} Autolinker gives the ones it recognises in the menus
   * (social NavigationUtils.js, getLinkNodeUri) would open them over http on
   * an https platform. Once trimmed, a link holding a control character, a
   * link whose scheme runs a script or reads local content, and any other link
   * are not redirected to.
   *
   * @param link the link stored on the page
   * @return the URI to redirect to, or null when the link is not one
   */
  private static String toRedirectUri(String link) {
    String uri = StringUtils.trimToNull(link);
    if (uri == null || CONTROL_CHARACTER_PATTERN.matcher(uri).find()) {
      return null;
    } else if (uri.startsWith("/")) {
      return encodeNonAscii(uri);
    }
    Matcher scheme = SCHEME_PATTERN.matcher(uri);
    if (scheme.find()) {
      return REFUSED_SCHEMES.contains(scheme.group(1).toLowerCase(Locale.ROOT)) ? null : encodeNonAscii(uri);
    } else if (HOST_PREFIX_PATTERN.matcher(uri).find()) {
      return "//" + encodeNonAscii(uri);
    } else {
      return null;
    }
  }

  /**
   * Tomcat writes a header in ISO-8859-1 and drops a header holding a
   * character it can't encode, so a Location with such a character would
   * leave a redirect with no target. Every character above {@code ~} is
   * percent-encoded as UTF-8, as a browser encodes the same link in a menu's
   * href; ASCII, percent-escapes included, is kept as is.
   *
   * @param uri the URI to redirect to
   * @return the URI with only ASCII characters
   */
  private static String encodeNonAscii(String uri) {
    StringBuilder encoded = new StringBuilder(uri.length());
    uri.codePoints().forEach(codePoint -> {
      if (codePoint > '~') {
        for (byte b : Character.toString(codePoint).getBytes(StandardCharsets.UTF_8)) {
          encoded.append('%').append(String.format("%02X", b & 0xFF));
        }
      } else {
        encoded.appendCodePoint(codePoint);
      }
    });
    return encoded.toString();
  }

  private String getPageNotFoundSite(String username) {
    return StringUtils.isBlank(username) ? "public" : portalConfigService.getMetaPortal();
  }

  private boolean isHiddenSpace(Space space, String username) {
    return !spaceService.canViewSpace(space, username)
           && Space.HIDDEN.equals(space.getVisibility());
  }

  private PermanentLinkObject getPermanentLinkObject(String spaceId, String path) {
    return new PermanentLinkObject(OBJECT_TYPE,
                                   spaceId,
                                   Collections.singletonMap(APPLICATION_URI, path));
  }

  private String getAuthenticationUrl(String permanentLink, String queryString) {
    StringBuilder loginPath = new StringBuilder();
    // Check sso enabled
    SSOHelper ssoHelper = ExoContainerContext.getService(SSOHelper.class);
    if (ssoHelper != null && ssoHelper.isSSOEnabled() && ssoHelper.skipJSPRedirection()) {
      loginPath.append("/portal").append(ssoHelper.getSSORedirectURLSuffix());
    } else {
      loginPath.append("/portal/login");
    }
    String fullUri = StringUtils.isNotBlank(queryString) ? permanentLink + "?" + queryString : permanentLink;
    loginPath.append("?initialURI=").append(URLEncoder.encode(fullUri, StandardCharsets.UTF_8));
    return loginPath.toString();
  }

}
