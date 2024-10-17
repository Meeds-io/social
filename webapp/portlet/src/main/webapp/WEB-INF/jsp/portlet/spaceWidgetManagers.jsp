<%@page contentType="text/html; charset=UTF-8" %>
<%@page import="java.net.URLEncoder"%>
<%@page import="io.meeds.social.util.JsonUtils"%>
<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="java.util.Objects"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@page import="org.hibernate.query.spi.Limit"%>
<%@page import="org.apache.commons.lang3.ArrayUtils"%>
<%@page import="java.util.Arrays"%>
<%@page import="io.meeds.portal.security.constant.UserRegistrationType"%>
<%@page import="io.meeds.portal.security.service.SecuritySettingService"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%
  SecuritySettingService securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
  boolean isExternalFeatureEnabled = securitySettingService.getRegistrationType() == UserRegistrationType.OPEN
    || securitySettingService.isRegistrationExternalUser();

  boolean isManager = false;
  boolean isMember = false;
  boolean isAnonymous = StringUtils.isBlank(request.getRemoteUser());
  String spaceId = "0";
  String[] spaceManagerDetails = null;

  Space space = SpaceUtils.getSpaceByContext();
  if (space != null) {
    spaceId = space.getId();
    isManager = ExoContainerContext.getService(SpaceService.class)
      .canManageSpace(space, request.getRemoteUser());
    isMember = ExoContainerContext.getService(SpaceService.class)
      .canViewSpace(space, request.getRemoteUser());
    String[] spaceManagers = Arrays.stream(space.getManagers())
                                   .distinct()
                                   .toArray(String[]::new);
    if (spaceManagers.length > 0) {
      IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
      spaceManagerDetails = Arrays.stream(spaceManagers)
                                  .map(m -> identityManager.getOrCreateUserIdentity(m))
                                  .filter(Objects::nonNull)
                                  .map(i -> i.getProfile())
                                  .map(p -> isAnonymous ? ("{\"avatar\": \"" + p.getAvatarUrl() + "\", \"fullname\": \"" + p.getFullName() + "\", \"enabled\": true}")
                                                        : JsonUtils.toJsonString(EntityBuilder.buildEntityProfile(space, p, "", "").getDataEntity()))
                                  .toArray(String[]::new);
    }
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
    id="SpaceManagersApplication" flat="">
    <textarea id="managersWidgetValue" class="d-none"><%=spaceManagerDetails == null || spaceManagerDetails.length == 0 ? "[]" : "[" + URLEncoder.encode(StringUtils.join(spaceManagerDetails, ",").replace(" ", "._.")).replace("._.", " ") + "]"%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceWidgetManagers'],
        app => app.init(<%=spaceId%>, <%=isManager%>, <%=isMember%>, JSON.parse(decodeURIComponent(document.getElementById('managersWidgetValue').value)))
      );
    </script>
  </div>
</div>