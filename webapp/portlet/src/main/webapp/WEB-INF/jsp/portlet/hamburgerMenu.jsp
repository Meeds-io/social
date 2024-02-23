<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.commons.api.settings.SettingValue"%>
<%@page import="org.exoplatform.commons.api.settings.data.Scope"%>
<%@page import="org.exoplatform.commons.api.settings.data.Context"%>
<%@page import="org.exoplatform.commons.api.settings.SettingService"%>
<%@page import="org.exoplatform.social.core.space.SpacesAdministrationService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%
  boolean canCreateSpace = ExoContainerContext.getService(SpacesAdministrationService.class).canCreateSpace(request.getRemoteUser());
  SettingValue stickySettingValue = ExoContainerContext.getService(SettingService.class).get(Context.USER.id(request.getRemoteUser()), Scope.APPLICATION.id("HamburgerMenu"), "Sticky");
  boolean sticky = stickySettingValue == null ? Boolean.parseBoolean(System.getProperty("io.meeds.userPrefs.HamburgerMenu.sticky", "false")) : Boolean.parseBoolean(stickySettingValue.getValue().toString());

  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();

  responseWrapper.addHeader("Link", "</portal/rest/v1/social/sites?siteType=PORTAL&excludedSiteName=global&lang=en&excludeEmptyNavigationSites=true&excludeGroupNodesWithoutPageChildNodes=true&temporalCheck=true&excludeSpaceSites=true&expandNavigations=true&visibility=displayed&visibility=temporal&filterByDisplayed=true&sortByDisplayOrder=true&displayed=true&filterByPermissions=true>; rel=preload; as=fetch; crossorigin=use-credentials", false);
  responseWrapper.addHeader("Link", "</portal/rest/v1/social/spaces?q=&offset=0&limit=7&filterType=lastVisited&returnSize=true&expand=member,managers,favorite,unread,muted>; rel=preload; as=fetch; crossorigin=use-credentials", false);

%>
<div class="VuetifyApp">
  <div id="HamburgerNavigationMenu" data-app="true" class="v-application HamburgerNavigationMenu v-application--is-ltr theme--light" id="app" color="transaprent" flat="">
    <div class="v-application--wrap">
      <% if (sticky) { %>
      <script type="text/javascript">
        if (window.innerWidth >= 1280) {
          const siteStickyMenuHtml = sessionStorage.getItem('ParentSiteStickyMenu');
          if (siteStickyMenuHtml) {
            document.querySelector('#ParentSiteStickyMenu').innerHTML = siteStickyMenuHtml;
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
      require(['PORTLET/social-portlet/HamburgerMenu'], app => app.init(<%=canCreateSpace%>));
    </script>
  </div>
</div>