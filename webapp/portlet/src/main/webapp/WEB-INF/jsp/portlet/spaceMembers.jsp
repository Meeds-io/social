<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%
  Object filter = request.getAttribute("filter");
  if (filter == null) {
    filter = "";
  } else {
    filter = ((String[]) filter)[0];
  }

  boolean isManager = false;
  String spaceId = "0";

  Space space = SpaceUtils.getSpaceByContext();
  if (space != null) {
    spaceId = space.getId();
    SpaceService spaceService = ExoContainerContext.getService(SpaceService.class);
    isManager = spaceService.isSuperManager(request.getRemoteUser()) || spaceService.isManager(space, request.getRemoteUser());
  }

  boolean isExternalFeatureEnabled=CommonsUtils.isFeatureActive("externalUsers");
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
    id="peopleListApplication" flat="">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/MembersPortlet'],
          app => app.init('<%=filter%>', <%=isManager%>, <%=isExternalFeatureEnabled%>)
      );
    </script>
  </div>
</div>
