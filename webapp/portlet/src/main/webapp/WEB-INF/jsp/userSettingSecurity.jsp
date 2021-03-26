<%@ page import="org.gatein.sso.integration.SSOUtils" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.web.login.recovery.PasswordRecoveryService" %>

<%
  boolean ssoEnabled = SSOUtils.isSSOEnabled();
%>

<% if (!ssoEnabled) { %>
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
