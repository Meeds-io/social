<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.space.SpacesAdministrationService"%>
<%
  Object filter = request.getAttribute("filter");
  if (filter == null) {
    filter = "";
  } else {
    filter = ((String[]) filter)[0];
  }
  SpacesAdministrationService spacesAdministrationService = ExoContainerContext.getService(SpacesAdministrationService.class);
  boolean canCreateSpace = spacesAdministrationService.canCreateSpace(request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light"
    id="spacesListApplication" flat="">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpacesList'],
          app => app.init('<%=filter%>', <%=canCreateSpace%>)
      );
    </script>
  </div>
</div>