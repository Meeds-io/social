<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
%>
<div class="VuetifyApp">
  <div data-app="true" class="v-application v-application--is-ltr theme--light" id="topBarMenu">
    <% if (space == null) { %>
    <script type="text/javascript">
      const topBarMenuHtml = sessionStorage.getItem('topBarMenu');
      if (topBarMenuHtml) {
        document.querySelector('#topBarMenu').innerHTML = topBarMenuHtml;
      }
      require(['PORTLET/social-portlet/TopBarMenu'], app => {
        app.init()
      });
    </script>
    <% } %>
  </div>
</div>