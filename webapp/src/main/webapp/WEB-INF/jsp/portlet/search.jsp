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
  String tooltip;
  try {
    tooltip = bundle.getString("Search.button.tooltip") + " " + bundle.getString("Search.button.tooltip.shortcut");
  } catch (Exception e) {

    tooltip = bundle.getString("Search.button.tooltip");
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SearchApplication"
    role="search">
    <div class="v-application--wrap">
      <button
        type="button"
        title="<%=tooltip%>"
        class="transparent v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default"
        onclick="Vue.startApp('PORTLET/social/Search', 'init')">
        <span class="v-btn__content">
          <i class="v-icon fas fa-search" style="font-size: 20px"></i>
        </span>
      </button>
      <textarea id="searchConnectorsDefaultValue" aria-hidden="true" class="d-none"><%= jsonSearchConnectors%></textarea>
      <textarea id="searchSkinUrlsDefaultValue" aria-hidden="true" class="d-none"><%= skinUrlsString%></textarea>
      <script type="text/javascript">
        if (window.location.pathname?.endsWith?.('/search')) {
          window.require(['PORTLET/social/Search'], app => app.init());
        }
        window.require(['SHARED/commonVueComponents'], () => Vue.prototype.$utils.addShortcutsListener(['f'], () => {
          window.require(['PORTLET/social/Search'], app => {
            app.init();
            document.dispatchEvent(new CustomEvent('search-open'));
          });
        }));
        eXo.env.portal.topbarDisplayedApps = eXo.env.portal.topbarDisplayedApps || [];
        if (!eXo.env.portal.topbarDisplayedApps.includes('search')) {
          eXo.env.portal.topbarDisplayedApps.push('search');
        }
      </script>
    </div>
  </div>
</div>
