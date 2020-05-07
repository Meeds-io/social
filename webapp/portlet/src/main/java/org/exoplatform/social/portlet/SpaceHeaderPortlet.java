package org.exoplatform.social.portlet;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;

public class SpaceHeaderPortlet extends GenericPortlet {

  private static final Log LOG = ExoLogger.getLogger(SpaceHeaderPortlet.class);

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
    // If menu is not displayed yet in top bar, then display it with the banner
    // (like for intranet)
    boolean isMenuPortletDisplayed = httpRequest.getAttribute("SPACE_MENU_DISPLAYED") != null;
    if (!isMenuPortletDisplayed) {
      Locale locale = request.getLocale();
      String currentUser = request.getRemoteUser();
      try {
        List<UserNode> navigations = SpaceUtils.getSpaceNavigations(space,
                                                                    locale,
                                                                    currentUser);
        request.setAttribute("navigations", navigations);

        Map<String, String> navigationMap = navigations.stream()
                                                       .collect(Collectors.toMap(UserNode::getId,
                                                                                 userNode -> SpaceUtils.getUri(userNode)));
        request.setAttribute("navigationsUri", navigationMap);

        UserNode selectedUserNode = Util.getUIPortal().getSelectedUserNode();
        request.setAttribute("selectedUri", navigationMap.get(selectedUserNode.getId()));
        request.setAttribute("isSpaceHome", SpaceUtils.isHomeNavigation(selectedUserNode));

        SpaceUtils.setPageTitle(navigations, request, space, locale);
      } catch (Exception e) {
        LOG.warn("Error displaying space menu", e);
      }
    }

    boolean isHeaderAlreadyDisplayed = httpRequest.getAttribute("SPACE_HEADER_DISPLAYED") != null;
    if (isHeaderAlreadyDisplayed) {
      return;
    } else {
      httpRequest.setAttribute("SPACE_HEADER_DISPLAYED", true);

      try {
        UserNode selectedUserNode = Util.getUIPortal().getSelectedUserNode();
        // Display header only on homepage of space or the topbar menu is not
        // displayed
        if (isMenuPortletDisplayed && !SpaceUtils.isHomeNavigation(selectedUserNode)) {
          return;
        }
      } catch (Exception e) {
        LOG.error("Error retrieving current navigation node", e);
        return;
      }
    }

    PortletRequestDispatcher requestDispatcher = getPortletContext().getRequestDispatcher(viewDispatchedPath);
    requestDispatcher.include(request, response);
  }

}
