<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
%>
<div class="VuetifyApp">
  <div data-app="true" class="v-application v-application--is-ltr theme--light" id="topBarMenu">
    <%
    if (space == null) {
      PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
      PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
      responseWrapper.addHeader("Link", "</portal/rest/v1/navigations/PORTAL?siteName=" + rcontext.getPortalOwner() + "&scope=children&visibility=displayed&visibility=temporal&exclude=global>; rel=preload; as=fetch; crossorigin=use-credentials", false);
    %>
    <script type="text/javascript">
      const topBarMenuHtml = sessionStorage.getItem('topBarMenu');
      if (topBarMenuHtml) {
        document.querySelector('#topBarMenu').innerHTML = topBarMenuHtml;
      }
      require(['PORTLET/social-portlet/TopBarMenu'], app => {
        app.init()
      });
    </script>
    <% } %>
  </div>
</div>