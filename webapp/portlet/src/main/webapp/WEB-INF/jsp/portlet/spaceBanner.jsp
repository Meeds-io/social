<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.social.core.identity.provider.SpaceIdentityProvider"%>
<%@page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.exoplatform.portal.mop.user.UserNode"%>
<%@page import="java.util.List"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
  String bannerUrl = space == null ? "" : space.getBannerUrl();
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  int maxUploadSize = identityManager.getImageUploadLimit();
  SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
  boolean isAdmin = spaceService.isSuperManager(request.getRemoteUser())
      || spaceService.isManager(space, request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application spaceMenuParent white v-application--is-ltr theme--light"
    id="SpaceHeader">
    <v-cacheable-dom-app cache-id="SpaceHeader_<%=space.getId()%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceBannerPortlet'], app => app.init(null, '<%=bannerUrl%>', <%=maxUploadSize%>, <%=isAdmin%>));
    </script>
  </div>
</div>