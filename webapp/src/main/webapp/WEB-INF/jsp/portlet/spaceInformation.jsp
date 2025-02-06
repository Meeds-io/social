<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
  String activityId = rcontext.getRequest().getParameter("id");
  Space space = SpaceUtils.getSpaceByContext();
  if (space != null) {
    responseWrapper.addHeader("Link", "</portal/rest/v1/social/spaces/" + space.getId() + "?expand=managers,redactors>; rel=preload; as=fetch; crossorigin=use-credentials", false);
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application spaceMenuParent v-application--is-ltr theme--light"
    id="spaceInfosApp">
    <script type="text/javascript">
      require(['PORTLET/social/SpaceInfos'], app => app.init());
    </script>
  </div>
</div>
