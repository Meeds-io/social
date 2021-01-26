<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
	String profileOwnerId = Utils.getOwnerIdentityId();
	int maxUploadSize = identityManager.getImageUploadLimit();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light profileContactInformation"
    id="ProfileContactInformation">
    <v-cacheable-dom-app cache-id="ProfileContactInformation_<%=profileOwnerId%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/ProfileContactInformation'], 
          app => app.init(<%=maxUploadSize%>)
      );
    </script>
  </div>
</div>
