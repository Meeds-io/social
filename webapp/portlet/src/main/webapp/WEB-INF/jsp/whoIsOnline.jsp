<%@page import="org.exoplatform.social.rest.entity.CollectionEntity"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.exoplatform.social.rest.api.RestUtils"%>
<%@page import="org.exoplatform.social.rest.entity.ProfileEntity"%>
<%@page import="org.exoplatform.social.rest.entity.DataEntity"%>
<%@page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="java.util.List"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
  Identity[] identities = space == null ? RestUtils.getOnlineIdentities(null, request.getRemoteUser(), 20)
                                         : RestUtils.getOnlineIdentitiesOfSpace(null, request.getRemoteUser(), space, 20);
  List<DataEntity> profileInfos = new ArrayList<>();
  for (Identity identity : identities) {
    ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), "/v1/social/identities", null);
    profileInfos.add(profileInfo.getDataEntity());
  }
  CollectionEntity collectionUsers = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, 0, 20);
  String usersOnlineString = EntityBuilder.toJsonString(collectionUsers);
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr hiddenable-widget theme--light"
    id="OnlinePortlet">
    <textarea id="whoIsOnlineDefaultValue" class="hidden"><%=usersOnlineString%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/WhoIsOnLinePortlet'], app => app.init());
    </script>
  </div>
</div>