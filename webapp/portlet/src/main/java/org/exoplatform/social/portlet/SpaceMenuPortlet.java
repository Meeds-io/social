package org.exoplatform.social.portlet;

import java.io.IOException;
import java.util.*;

import javax.portlet.*;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;

/**
 * @deprecated use SpaceBannerPortlet instead
 */
@Deprecated(forRemoval = true, since = "7.0")
public class SpaceMenuPortlet extends GenericPortlet {

  private static final Log LOG = ExoLogger.getLogger(SpaceMenuPortlet.class);

  private String           viewDispatchedPath;

  @Override
  public void init(PortletConfig config) throws PortletException {
    super.init(config);
    viewDispatchedPath = config.getInitParameter("portlet-view-dispatched-file-path");
    if (StringUtils.isBlank(viewDispatchedPath)) {
      throw new IllegalStateException("Portlet init parameter 'portlet-view-dispatched-file-path' is mandatory");
    }
  }

  @Override
  public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    Space space = SpaceUtils.getSpaceByContext();
    if (space == null) {
      return;
    }

    HttpServletRequest httpRequest = Util.getPortalRequestContext().getRequest();
    httpRequest.setAttribute("SPACE_MENU_DISPLAYED", true);

    Locale locale = request.getLocale();
    String currentUser = request.getRemoteUser();
    try {
      List<UserNode> navigations = SpaceUtils.getSpaceNavigations(space,
                                                                  locale,
                                                                  currentUser);
      request.setAttribute("navigations", navigations);

      Map<String, String> navigationMap = new HashMap<>();
      navigations.stream().forEach(userNode -> {
        if (userNode.getPageRef() != null) {
          Page navigationNodePage = SpaceUtils.getLayoutService().getPage(userNode.getPageRef());
          if (PageType.LINK.equals(PageType.valueOf(navigationNodePage.getType()))) {
            navigationMap.put(userNode.getId(), navigationNodePage.getLink());
          } else {
            navigationMap.put(userNode.getId(), SpaceUtils.getUri(userNode));
          }
        } else {
          navigationMap.put(userNode.getId(), SpaceUtils.getUri(userNode));
        }
      });
      request.setAttribute("navigationsUri", navigationMap);

      UserNode selectedUserNode = Util.getUIPortal().getSelectedUserNode();
      request.setAttribute("selectedUri", navigationMap.get(selectedUserNode.getId()));
      request.setAttribute("isSpaceHome", SpaceUtils.isHomeNavigation(selectedUserNode));

      SpaceUtils.setPageTitle(navigations, request, space, locale);
    } catch (Exception e) {
      LOG.warn("Error displaying space menu", e);
    }

    PortletRequestDispatcher requestDispatcher = getPortletContext().getRequestDispatcher(viewDispatchedPath);
    requestDispatcher.include(request, response);
  }
}
