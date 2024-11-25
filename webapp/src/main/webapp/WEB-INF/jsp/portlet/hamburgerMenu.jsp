<%@page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@page import="io.meeds.social.util.JsonUtils"%>
<%@page import="org.exoplatform.social.notification.service.SpaceWebNotificationService"%>
<%@page import="java.util.Map"%>
<%@page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.commons.api.settings.SettingValue"%>
<%@page import="org.exoplatform.commons.api.settings.data.Scope"%>
<%@page import="org.exoplatform.commons.api.settings.data.Context"%>
<%@page import="org.exoplatform.commons.api.settings.SettingService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  boolean canCreateSpace = ExoContainerContext.getService(SpaceTemplateService.class).canCreateSpace(request.getRemoteUser());
  SettingValue stickySettingValue = ExoContainerContext.getService(SettingService.class).get(Context.USER.id(request.getRemoteUser()), Scope.APPLICATION.id("HamburgerMenu"), "Sticky");
  boolean sticky = stickySettingValue == null ? Boolean.parseBoolean(System.getProperty("io.meeds.userPrefs.HamburgerMenu.sticky", "false")) : Boolean.parseBoolean(stickySettingValue.getValue().toString());

  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
  if (rcontext.getRequest().getParameter("sticky") != null) {
    sticky = StringUtils.equals("true", rcontext.getRequest().getParameter("sticky"));
  }
  UserPortalConfigService portalConfigService = ExoContainerContext.getService(UserPortalConfigService.class);
  SpaceWebNotificationService spaceWebNotificationService = ExoContainerContext.getService(SpaceWebNotificationService.class);
  Identity viewerIdentity = Utils.getViewerIdentity();
  String avatarUrl = viewerIdentity == null ? "" : viewerIdentity.getProfile().getAvatarUrl();

  Map<Long, Long> unreadPerSpace = spaceWebNotificationService.countUnreadItemsBySpace(request.getRemoteUser());

  String defaultHomePath = "/portal/" + rcontext.getPortalOwner();
  String defaultUserPath = defaultHomePath;
  if (StringUtils.equals(rcontext.getPortalOwner(), "public")) {
    defaultUserPath = "/portal/public";
  } else {
    defaultUserPath = portalConfigService.getUserHomePage(request.getRemoteUser());
    if (defaultUserPath == null) {
      defaultUserPath = portalConfigService.computePortalPath(rcontext.getRequest());
      if (defaultUserPath == null) {
        defaultUserPath = defaultHomePath;
      }
    }
  }

  responseWrapper.addHeader("Link", "</social/rest/navigation/settings/sidebar>; rel=preload; as=fetch; crossorigin=use-credentials", false);
%>
<div class="VuetifyApp">
  <div id="HamburgerNavigationMenu" data-app="true" class="v-application HamburgerNavigationMenu v-application--is-ltr theme--light" id="app" color="transaprent" flat="">
    <div class="v-application--wrap">
      <% if (sticky) { %>
      <script type="text/javascript">
        if (!window.siteStickyMenuLoaded) {
          window.siteStickyMenuLoaded = true;
          if (window.innerWidth >= 1280) {
            window.siteStickyMenuHtml = sessionStorage.getItem('ParentSiteStickyMenu');
            if (window.siteStickyMenuHtml) {
              document.querySelector('#ParentSiteStickyMenu').innerHTML = window.siteStickyMenuHtml;
            }
          } else {
            document.querySelector('#HamburgerNavigationMenu > .v-application--wrap').innerHTML = `
            <a class="HamburgerNavigationMenuLink">
              <div class="px-5 py-3">
                <i aria-hidden="true"
                  class="v-icon notranslate fa fa-bars theme--light"
                  style="font-size: 24px;"></i>
              </div>
            </a>`;
          }
        }
      </script>
      <% } else { %>
      <a class="HamburgerNavigationMenuLink">
        <div class="px-5 py-3">
          <i aria-hidden="true"
            class="v-icon notranslate fa fa-bars theme--light"
            style="font-size: 24px;"></i>
        </div>
      </a>
      <% } %>
    </div>
    <script type="text/javascript">
      if (!window.siteStickyMenuInitialized) {
        window.siteStickyMenuInitialized = true;
        require(['PORTLET/social/HamburgerMenu'], app => app.init(<%=canCreateSpace%>, '<%=defaultUserPath%>', <%=unreadPerSpace == null ? "{}" : JsonUtils.toJsonString(unreadPerSpace)%>, '<%=avatarUrl == null ? "" : avatarUrl%>'));
      }
    </script>
  </div>
</div>
