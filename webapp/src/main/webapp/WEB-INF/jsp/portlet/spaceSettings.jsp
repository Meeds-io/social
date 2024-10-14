<%@page import="io.meeds.portal.security.constant.UserRegistrationType"%>
<%@page import="io.meeds.portal.security.service.SecuritySettingService"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%
  SecuritySettingService securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
  boolean isExternalFeatureEnabled = securitySettingService.getRegistrationType() == UserRegistrationType.OPEN
    || securitySettingService.isRegistrationExternalUser();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
    id="SpaceSettings" flat="">
    <script type="text/javascript">
      require(['PORTLET/social/SpaceSettingPortlet'], app => app.init(<%=isExternalFeatureEnabled%>));
    </script>
  </div>
</div>
