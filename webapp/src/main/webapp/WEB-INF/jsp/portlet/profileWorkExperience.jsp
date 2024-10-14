<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  String profileOwnerId = Utils.getOwnerIdentityId();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light profileWorkExperience"
    id="ProfileWorkExperience">
    <v-cacheable-dom-app cache-id="ProfileWorkExperience_<%=profileOwnerId%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social/ProfileWorkExperience'], app => app.init());
    </script>
  </div>
</div>