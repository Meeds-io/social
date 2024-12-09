<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.HashSet"%>
<%@page import="org.gatein.portal.controller.resource.ResourceRequestHandler"%>
<%@page import="org.exoplatform.portal.resource.SkinService"%>
<%@page import="org.exoplatform.services.resources.Orientation"%>
<%@page import="org.exoplatform.portal.resource.SkinURL"%>
<%@page import="org.exoplatform.portal.resource.SkinConfig"%>
<%@page import="org.exoplatform.web.ControllerContext"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="org.exoplatform.social.core.search.SearchConnector"%>
<%@page import="java.util.Set"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.search.SearchService"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@page import="java.util.ResourceBundle"%>
<%
  SearchService searchService = ExoContainerContext.getService(SearchService.class);
  SkinService skinService = ExoContainerContext.getService(SkinService.class);
  Set<SearchConnector> connectors = searchService.getEnabledConnectors(request.getRemoteUser());
  String jsonSearchConnectors = EntityBuilder.toJsonString(connectors);
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  ControllerContext controllerContext = rcontext.getControllerContext();
  Orientation orientation = rcontext.getOrientation();
  Set<String> skinURLs = new HashSet<>();
  for (SearchConnector connector : connectors) {
    SkinConfig skinConfig = null;
    if (connector.getCssModule() != null) {
      skinConfig = skinService.getSkin(connector.getCssModule(), null);
    }
    if (skinConfig != null) {
      SkinURL url = skinConfig.createURL(controllerContext);
      url.setOrientation(orientation);
      skinURLs.add(url.toString());
    } 
  }
  String skinUrlsString = "[\"" + StringUtils.join(skinURLs, "\",\"") + "\"]";
  ResourceBundle bundle;
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", request.getLocale());
  } catch (Exception e) {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", Locale.ENGLISH);
  }
  String tooltip = bundle.getString("Search.button.tooltip");
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SearchApplication"
    role="search">
    <div class="v-application--wrap d-none">
      <v-tooltip bottom>
        <template #activator="{on, attrs}">
          <v-btn
            v-on="on"
            v-bind="attrs"
            aria-label="<%=tooltip%>"
            icon
            class="transparent"
            @click="Vue.startApp('PORTLET/social/Search', 'init')">
            <v-icon size="20">fa-search</v-icon>
          </v-btn>
        </template>
        <span class="tooltip"><%=tooltip%></span>
      </v-tooltip>
      <textarea id="searchConnectorsDefaultValue" aria-hidden="true" class="d-none"><%= jsonSearchConnectors%></textarea>
      <textarea id="searchSkinUrlsDefaultValue" aria-hidden="true" class="d-none"><%= skinUrlsString%></textarea>
      <% if (rcontext.getRequestURI().endsWith("/search") || rcontext.getRequestURI().equals("search")) { %>
      <script type="text/javascript">
        require(['PORTLET/social/Search'], app => app.init());
      </script>
      <% } else { %>
      <script type="text/javascript">
        require(['SHARED/commonVueComponents', 'SHARED/eXoVueI18n'], () => {
          new Vue({
            el: '#SearchApplication',
            vuetify: Vue.prototype.vuetifyOptions,
            mounted() {
              document.querySelector('#SearchApplication .v-application--wrap').classList.remove('d-none');
            },
          });
        });
      </script>
      <% } %>
    </div>
  </div>
</div>
