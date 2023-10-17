<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
  responseWrapper.addHeader("Link", "</portal/rest/notifications/settings/" + request.getRemoteUser() + ">; rel=preload; as=fetch; crossorigin=use-credentials", false);
%>
<div class="VuetifyApp">
  <div
    data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="UserSettingNotifications">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/UserSettingNotifications'],
        app => app.init()
      );
    </script>
  </div>
</div>