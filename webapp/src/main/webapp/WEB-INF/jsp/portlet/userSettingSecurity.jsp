<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting"%>
<%@page import="org.exoplatform.social.core.profileproperty.ProfilePropertyService"%>
<%@page import="org.gatein.sso.integration.SSOUtils" %>
<%
  boolean ssoEnabled = SSOUtils.isSSOEnabled();
  ProfilePropertyService profilePropertyService = ExoContainerContext.getService(ProfilePropertyService.class);
  ProfilePropertySetting profilePropertySetting = profilePropertyService.getProfileSettingByName("email");
  boolean emailEditable = profilePropertySetting == null || profilePropertySetting.isEditable();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="UserSettingSecurity">
    <v-cacheable-dom-app cache-id="UserSettingSecurity"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social/UserSettingSecurity'], app => app.init(<%=ssoEnabled%>, <%=emailEditable%>));
    </script>
  </div>
</div>
