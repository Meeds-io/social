<%@page import="java.net.URLEncoder"%>
<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.portal.mop.SiteType"%>
<%@page import="io.meeds.social.core.space.service.SpaceLayoutService"%>
<%
  PortalRequestContext rcontext = PortalRequestContext.getCurrentInstance();
  Space space = SpaceUtils.getSpaceByContext();
  SpaceService spaceService = ExoContainerContext.getService(SpaceService.class);
  SpaceLayoutService spaceLayoutService = ExoContainerContext.getService(SpaceLayoutService.class);

  String activityId = rcontext.getRequest().getParameter("id");
  String username = request.getRemoteUser();
  String description = space == null || space.getDescription() == null ? "" : space.getDescription();
  String id = space == null ? "0" : space.getId();
  boolean canEdit = spaceService.canManageSpace(space, username);
  String publicSiteName = "";

  if (spaceService.canAccessSpacePublicSite(space, username)) {
    publicSiteName = spaceLayoutService.getSpacePublicSiteName(space);
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SpaceDescriptionApplication">
    <textarea id="spaceDescriptionContent" class="d-none"><%=URLEncoder.encode(description.replace(" ", "._.")).replace("._.", " ")%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social/SpaceWidgetDescription'], app => app.init(<%=id%>, <%=canEdit%>, '<%=publicSiteName%>', decodeURIComponent(document.getElementById('spaceDescriptionContent').value)));
    </script>
  </div>
</div>
