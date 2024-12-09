<%@page import="java.util.ResourceBundle"%>
<%@ page import="java.util.Locale"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext" %>
<%@ page import="org.exoplatform.web.application.RequestContext" %>
<%
String path = ExoContainerContext.getService(UserPortalConfigService.class)
    .getDefaultSitePath("administration", request.getRemoteUser());
  if (path != null) {
    ResourceBundle bundle;
    String tooltip;
    try {
      bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", request.getLocale());
      tooltip = bundle.getString("platformSettings.label.openAdministration");
    } catch (Exception e) {
      bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", Locale.ENGLISH);
      tooltip = bundle.getString("platformSettings.label.openAdministration");
    }
%>
  <div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="platformSettings">
      <div class="v-application--wrap">
        <a
          href="/portal/administration"
          title="<%=tooltip%>"
          target="_blank"
          class="v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default"
          aria-label="Enter the administration settings"
          aria-haspopup="true"
          aria-expanded="false">
          <span class="v-btn__content">
            <i aria-hidden="true" class="v-icon notranslate icon-default-color fas fa-cog theme--light" style="font-size: 20px;"></i>
          </span>
        </a>
      </div>
    </div>
  </div>
<% } %>
