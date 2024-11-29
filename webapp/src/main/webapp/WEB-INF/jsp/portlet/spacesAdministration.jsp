<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.portal.security.service.SecuritySettingService"%>
<%@page import="io.meeds.portal.security.constant.UserRegistrationType"%>
<%
  SecuritySettingService securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
  boolean isExternalFeatureEnabled = securitySettingService.getRegistrationType() == UserRegistrationType.OPEN || securitySettingService.isRegistrationExternalUser();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light spacesAdministration"
    id="spacesAdministration">
    <script>
      require(['PORTLET/social/SpacesAdministration'], app =>app.init(<%=isExternalFeatureEnabled%>));
    </script>
  </div>
</div>