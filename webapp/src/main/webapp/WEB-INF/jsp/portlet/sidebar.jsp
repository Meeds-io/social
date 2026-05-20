<%@page import="io.meeds.portal.navigation.constant.SidebarMode"%>
<%@page import="io.meeds.portal.navigation.service.NavigationConfigurationService"%>
<%@page import="io.meeds.portal.security.service.SecuritySettingService"%>
<%@page import="io.meeds.portal.security.constant.UserRegistrationType"%>
<%@page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@page import="io.meeds.social.util.JsonUtils"%>
<%@page import="org.exoplatform.social.notification.service.SpaceWebNotificationService"%>
<%@page import="java.util.Map"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.commons.api.settings.SettingValue"%>
<%@page import="org.exoplatform.commons.api.settings.data.Scope"%>
<%@page import="org.exoplatform.commons.api.settings.data.Context"%>
<%@page import="org.exoplatform.commons.api.settings.SettingService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@page import="org.exoplatform.social.webui.Utils"%>
<%
PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();

  NavigationConfigurationService navigationConfigurationService = ExoContainerContext.getService(NavigationConfigurationService.class);
  SidebarMode mode = navigationConfigurationService.getSidebarUserMode(request.getRemoteUser());
  boolean allowUserHome = navigationConfigurationService.getConfiguration().getSidebar().isAllowUserCustomHome();

  Identity viewerIdentity = Utils.getViewerIdentity();
  String avatarUrl = viewerIdentity == null ? "" : viewerIdentity.getProfile().getAvatarUrl();

  Map<Long, Long> unreadPerSpace = ExoContainerContext.getService(SpaceWebNotificationService.class)
    .countUnreadItemsBySpace(request.getRemoteUser());

  ((PortalHttpServletResponseWrapper) rcontext.getResponse()).addHeader("Link", "</social/rest/navigation/settings/sidebar>; rel=preload; as=fetch; crossorigin=use-credentials", false);

  SecuritySettingService securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
  boolean isExternalFeatureEnabled = securitySettingService.getRegistrationType() == UserRegistrationType.OPEN || securitySettingService.isRegistrationExternalUser();
%>
<div class="VuetifyApp layout-side-bar">
  <div id="HamburgerNavigationMenu" data-app="true" class="v-application HamburgerNavigationMenu v-application--is-ltr theme--light" id="app" color="transaprent" flat="">
    <div class="v-application--wrap">
      <% if (mode == SidebarMode.STICKY) { %>
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
              <div class="d-flex justify-center" style="min-width: 69px;">
                <i aria-hidden="true"
                  class="v-icon notranslate fa fa-bars theme--light"
                  style="font-size: 20px;"></i>
              </div>
            </a>`;
          }
        }
      </script>
      <% } else { %>
      <a class="HamburgerNavigationMenuLink layout-top-bar">
        <div class="d-flex justify-center" style="min-width: 69px;">
          <% if (mode == SidebarMode.HIDDEN) { %>
          <i aria-hidden="true"
            class="v-icon notranslate fa fa-bars theme--light"
            style="font-size: 20px;"></i>
          <% } %>
        </div>
      </a>
      <% } %>
    </div>
    <script type="text/javascript">
      document.querySelector('#ParentSiteStickyMenu')?.parentElement?.classList.add('layout-side-bar');
      require(['PORTLET/social/Sidebar'], app => app.init('<%=mode%>', <%=unreadPerSpace == null ? "{}" : JsonUtils.toJsonString(unreadPerSpace)%>, '<%=avatarUrl == null ? "" : avatarUrl%>', <%=isExternalFeatureEnabled%>, <%=allowUserHome%>));
      eXo.env.portal.isExternalFeatureEnabled = <%=isExternalFeatureEnabled%>;
    </script>
  </div>
</div>