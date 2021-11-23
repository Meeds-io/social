<%@ page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@ page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.json.JSONObject"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
  JSONObject object = new JSONObject();
  object.put("id", space.getId());
  object.put("displayName", space.getDisplayName());
  object.put("groupId", space.getGroupId());
  object.put("app", space.getApp());
  object.put("description", space.getDescription());
  object.put("pendingUsers", space.getPendingUsers());
  object.put("invitedUsers", space.getInvitedUsers());
  object.put("template", space.getTemplate());
  object.put("url", space.getUrl());
  object.put("visibility", space.getVisibility());
  object.put("registration", space.getRegistration());
  object.put("priority", space.getPriority());
  object.put("createdTime", space.getCreatedTime());
  object.put("lastUpdatedTime", space.getLastUpdatedTime());
  object.put("prettyName", space.getPrettyName());
  object.put("avatarUrl", space.getAvatarUrl());
  object.put("bannerUrl", space.getBannerUrl());
  object.put("managers", space.getManagers());
  object.put("members", space.getMembers());
  object.put("redactors", space.getRedactors());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
    id="SpaceSettings" flat="">
    <v-cacheable-dom-app cache-id="SpaceSettings_<%=space.getId()%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceSettingPortlet'],
          app => app.init(<%=space%>)
      );
    </script>
  </div>
</div>