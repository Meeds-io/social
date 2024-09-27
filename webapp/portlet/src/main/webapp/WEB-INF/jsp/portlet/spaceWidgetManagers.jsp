<%@page import="java.util.Arrays"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.apache.commons.lang3.ArrayUtils"%>
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
  String id = space == null ? "" : space.getId();
  String[] managers = space == null ? new String[0] : Arrays.stream(space.getManagers()).distinct().toArray(String[]::new);
  boolean canEdit = space != null
      && request.getRemoteUser() != null
      && ExoContainerContext.getService(SpaceService.class)
           .canManageSpace(space, request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SpaceManagersApplication">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceWidgetManagers'], app => app.init(<%=id%>, <%=canEdit%>, <%=managers.length == 0 ? "[]" : "['" + StringUtils.join(managers, "','") + "']"%>));
    </script>
  </div>
</div>
