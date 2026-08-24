<%@page import="io.meeds.social.security.service.AccountDeactivationService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting"%>
<%@page import="org.exoplatform.social.core.profileproperty.ProfilePropertyService"%>
<%@page import="org.gatein.sso.integration.SSOUtils" %>
<%
  boolean ssoEnabled = SSOUtils.isSSOEnabled();
  ProfilePropertyService profilePropertyService = ExoContainerContext.getService(ProfilePropertyService.class);
  ProfilePropertySetting profilePropertySetting = profilePropertyService.getProfileSettingByName("email");
  boolean emailEditable = profilePropertySetting == null || profilePropertySetting.isEditable();
  // single source of truth: the admin option AND the account being managed by
  // the platform itself, so externally synchronized users (LDAP) get no
  // deactivation option at all
  AccountDeactivationService accountDeactivationService = ExoContainerContext.getService(AccountDeactivationService.class);
  boolean deactivationAllowed = accountDeactivationService.isDeactivationAllowed(request.getRemoteUser());
  boolean deletionAllowed = deactivationAllowed && accountDeactivationService.isDeletionAllowed(request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="UserSettingSecurity">
    <v-cacheable-dom-app cache-id="UserSettingSecurity"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social/UserSettingSecurity'], app => app.init(<%=ssoEnabled%>, <%=emailEditable%>, <%=deactivationAllowed%>, <%=deletionAllowed%>));
    </script>
  </div>
</div>
