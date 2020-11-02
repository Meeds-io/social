<%@ page import="org.gatein.sso.integration.SSOUtils" %>
<% boolean ssoEnabled = SSOUtils.isSSOEnabled(); %>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="UserSettingSecurity">
    <v-cacheable-dom-app cache-id="UserSettingSecurity"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/UserSettingSecurity'],
        app => app.init(<%=ssoEnabled%>)
      );
    </script>
  </div>
</div>