<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  int maxUploadSize = identityManager.getImageUploadLimit();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
    id="SpaceSettings" flat="">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceSettingPortlet'],
          app => app.init(<%=maxUploadSize%>)
      );
    </script>
  </div>
</div>