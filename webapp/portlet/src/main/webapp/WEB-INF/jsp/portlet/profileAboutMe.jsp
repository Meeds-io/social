<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
	String profileOwnerId = Utils.getOwnerIdentityId();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light profileAboutMe"
    id="ProfileAboutMe">
    <v-cacheable-dom-app cache-id="ProfileAboutMe_<%=profileOwnerId%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/ProfileAboutMe'], app => app.init());
    </script>
  </div>
</div>