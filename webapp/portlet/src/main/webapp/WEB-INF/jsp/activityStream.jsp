<%
  String profileOwnerId = org.exoplatform.social.webui.Utils.getOwnerIdentityId();
  if (profileOwnerId == null) {
    profileOwnerId = "";
  }
  String spaceId = org.exoplatform.social.webui.Utils.getSpaceIdByContext();
  if (spaceId == null) {
    spaceId = "";
  }
  String activityId = org.exoplatform.social.webui.Utils.getValueFromRequestParam("id");
  if (activityId == null) {
    activityId = "";
  }
%>
<div class="VuetifyApp">
  <div id="ActivityStream"
    class="v-application transparent v-application--is-ltr theme--light activity-stream"
    data-app="true" flat="">
    <v-cacheable-dom-app cache-id="ActivityStream_<%=profileOwnerId%>_<%=spaceId%>_<%=activityId%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['SHARED/ActivityStream'], app => app.init());
    </script>
  </div>
</div>