<%@ page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@ page import="org.exoplatform.social.core.space.model.Space"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
    id="SpaceSettings" flat="">
    <v-cacheable-dom-app cache-id="SpaceSettings_<%=space.getId()%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceSettingPortlet'],
          app => app.init()
      );
    </script>
  </div>
</div>