<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@ page import="org.exoplatform.social.core.space.model.Space"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  int maxUploadSize = identityManager.getImageUploadLimit();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
    id="SpaceSettings" flat="">
    <v-cacheable-dom-app cache-id="SpaceSettings_<%=space.getId()%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceSettingPortlet'],
          app => app.init(<%=maxUploadSize%>)
      );
    </script>
  </div>
</div>