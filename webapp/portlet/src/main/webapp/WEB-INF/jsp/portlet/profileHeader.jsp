<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
	int maxUploadSize = identityManager.getImageUploadLimit();
  String profileOwnerId = Utils.getOwnerIdentityId();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light profileHeaderOwner"
    id="ProfileHeader">
    <v-cacheable-dom-app cache-id="ProfileHeader_<%=profileOwnerId%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/ProfileHeader'], app => app.init(<%=maxUploadSize%>));
    </script>
  </div>
</div>