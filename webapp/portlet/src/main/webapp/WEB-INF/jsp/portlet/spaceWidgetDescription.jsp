<%@page import="java.net.URLEncoder"%>
<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
  String activityId = rcontext.getRequest().getParameter("id");
  Space space = SpaceUtils.getSpaceByContext();
  String description = space == null || space.getDescription() == null ? "" : space.getDescription();
  String id = space == null ? "0" : space.getId();
  boolean canEdit = space != null
      && request.getRemoteUser() != null
      && ExoContainerContext.getService(SpaceService.class)
           .canManageSpace(space, request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SpaceDescriptionApplication">
    <textarea id="spaceDescriptionContent" class="d-none"><%=URLEncoder.encode(description.replace(" ", "._.")).replace("._.", " ")%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceWidgetDescription'], app => app.init(<%=id%>, <%=canEdit%>, decodeURIComponent(document.getElementById('spaceDescriptionContent').value)));
    </script>
  </div>
</div>