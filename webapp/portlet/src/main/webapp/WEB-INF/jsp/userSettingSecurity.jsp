<%@ page import="org.gatein.sso.integration.SSOUtils" %>
<%@ page import="org.exoplatform.services.organization.OrganizationService" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>

<%
  OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
  boolean isInternalUser = organizationService.getUserHandler().findUserByName(request.getRemoteUser()).isInternalStore();
  boolean ssoEnabled = SSOUtils.isSSOEnabled();
  String allowChangeExternalPassword = System.getProperty("exo.portal.allow.change.external.password");
%>

<% if (!ssoEnabled && (isInternalUser || allowChangeExternalPassword.equals("true"))) { %>
  <div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="UserSettingSecurity">
      <v-cacheable-dom-app cache-id="UserSettingSecurity"></v-cacheable-dom-app>
      <script type="text/javascript">
        require(['PORTLET/social-portlet/UserSettingSecurity'],
          app => app.init()
        );
      </script>
    </div>
  </div>
<% } %>
