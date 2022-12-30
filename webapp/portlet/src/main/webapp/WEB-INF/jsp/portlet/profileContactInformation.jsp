<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="java.util.List"%>
<%@page import="java.util.stream.Collectors"%>
<%@page import="org.exoplatform.social.core.profile.settings.IMType"%>
<%@page import="org.exoplatform.social.core.profile.settings.UserProfileSettingsService"%>
<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
	String profileOwnerId = Utils.getOwnerIdentityId();
	int maxUploadSize = identityManager.getImageUploadLimit();

	UserProfileSettingsService userProfileSettingsService = CommonsUtils.getService(UserProfileSettingsService.class);
	List<String> imTypes = userProfileSettingsService.getIMTypes().stream().map(IMType::getId).collect(Collectors.toList());
  String jsonImTypes = EntityBuilder.toJsonString(imTypes);
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light profileContactInformation"
    id="ProfileContactInformation">
    <textarea id="imTypesDefault" class="hidden"><%=jsonImTypes%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/ProfileContactInformation'], 
          app => app.init(<%=maxUploadSize%>, JSON.parse(document.getElementById('imTypesDefault').value))
      );
    </script>
  </div>
</div>
