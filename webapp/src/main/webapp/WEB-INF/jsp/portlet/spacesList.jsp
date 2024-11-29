<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.portal.security.service.SecuritySettingService"%>
<%@page import="io.meeds.portal.security.constant.UserRegistrationType"%>
<%
  Object filter = request.getAttribute("filter");
  if (filter == null) {
    filter = "";
  } else {
    filter = ((String[]) filter)[0];
  }
  boolean canCreateSpace = ExoContainerContext.getService(SpaceTemplateService.class)
    .canCreateSpace(request.getRemoteUser());
  SecuritySettingService securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
  boolean isExternalFeatureEnabled = securitySettingService.getRegistrationType() == UserRegistrationType.OPEN || securitySettingService.isRegistrationExternalUser();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light"
    id="spacesListApplication" flat="">
    <script type="text/javascript">
      require(['PORTLET/social/SpacesList'],
          app => app.init('<%=filter%>', <%=canCreateSpace%>, <%=isExternalFeatureEnabled%>)
      );
    </script>
  </div>
</div>