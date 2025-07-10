<%@ page import="org.exoplatform.web.login.recovery.PasswordRecoveryService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>

<%



  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  String contextPath = portalContainer.getPortalContext().getContextPath();

  PasswordRecoveryService passwordRecoveryService = CommonsUtils.getService(PasswordRecoveryService.class);
  String forgotPasswordPath = contextPath + passwordRecoveryService.getPasswordRecoverURL(null, null);

%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light loginForm"
    id="loginFormApplication">
    <script type="text/javascript">
      require(['PORTLET/social/LoginForm'], app =>app.init('<%=URLEncoder.encode(forgotPasswordPath.replace(" ", "._.")).replace("._.", " ")%>'));
    </script>
  </div>
</div>
