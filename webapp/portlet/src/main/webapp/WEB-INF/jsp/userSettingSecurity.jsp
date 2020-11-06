<%@ page import="org.gatein.sso.integration.SSOUtils" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.web.login.recovery.PasswordRecoveryService" %>

<%
  PasswordRecoveryService passwordRecoveryService = CommonsUtils.getService(PasswordRecoveryService.class);
  boolean ssoEnabled = SSOUtils.isSSOEnabled();
  boolean allowChangeExternalPassword = passwordRecoveryService.allowChangePassword(request.getRemoteUser());
%>

<% if (!ssoEnabled && allowChangeExternalPassword) { %>
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
