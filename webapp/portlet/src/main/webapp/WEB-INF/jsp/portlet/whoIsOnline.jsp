<%@page import="org.exoplatform.social.webui.Utils"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="java.util.ResourceBundle"%>
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
  Identity viewerIdentity = Utils.getViewerIdentity();
  if (viewerIdentity != null && !viewerIdentity.isExternal()) {
    Space space = SpaceUtils.getSpaceByContext();
    Identity[] identities = space == null ? RestUtils.getOnlineIdentities(null, request.getRemoteUser(), 20)
                                           : RestUtils.getOnlineIdentitiesOfSpace(null, request.getRemoteUser(), space, 20);
    List<DataEntity> profileInfos = new ArrayList<>();
    for (Identity identity : identities) {
      try {
        if (identity == null || identity.getProfile() == null) {
          continue;
        }
        ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), "/v1/social/identities", null);
        if (profileInfo == null) {
          continue;
        }
        profileInfos.add(profileInfo.getDataEntity());
      } catch (Exception e) {
        // When computing User attributes fails, display the application anyway
      }
    }
    CollectionEntity collectionUsers = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, 0, 20);
    String usersOnlineString = EntityBuilder.toJsonString(collectionUsers);
    String title;
    try {
      ResourceBundle bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.whoisonline.whoisonline", request.getLocale());
      title = bundle.getString("header.label");
    } catch (Exception e) {
      ResourceBundle bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.whoisonline.whoisonline", Locale.ENGLISH);
      title = bundle.getString("header.label");
    }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application hiddenable-widget v-application--is-ltr theme--light"
    id="OnlinePortlet">
    <textarea id="whoIsOnlineDefaultValue" class="hidden"><%=usersOnlineString%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/WhoIsOnLinePortlet'], app => app.init());
    </script>
  </div>
</div>
<% } else { %>
  <div id="OnlinePortlet">
    <script type="text/javascript">
      require(['PORTLET/vue'], () => Vue.prototype.$updateApplicationVisibility(false, document.querySelector('#OnlinePortlet')));
    </script>
  </div>
<% } %>