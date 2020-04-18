<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  int maxUploadSize = identityManager.getImageUploadLimit();
%>
<div class="VuetifyApp">
  <div id="ProfileHeader">
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/ProfileHeader'], app => app.init(<%=maxUploadSize%>));
    </script>
  </div>
</div>
