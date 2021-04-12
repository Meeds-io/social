<%@ page import="org.exoplatform.social.core.space.model.Space"%>
<%@ page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.web.application.RequestContext"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.portal.branding.Branding"%>
<%@ page import="org.exoplatform.portal.branding.Logo"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page language="java" %>

<%
  String logoPath = null;
  String logoTitle = null;
  String portalPath = null;
  String titleClass = "";
  String imageClass = "";
  String spaceAvatarClass = "";

  Space space = SpaceUtils.getSpaceByContext();
  PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());
  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  ResourceBundleService service = (ResourceBundleService) portalContainer.getComponentInstanceOfType(ResourceBundleService.class);
  ResourceBundle res = service.getResourceBundle(service.getSharedResourceBundleNames(), request.getLocale()) ;

  if (space == null) {
    BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
    Branding branding = brandingService.getBrandingInformation();
    Logo logo = branding.getLogo();
    logoPath = logo == null ? null : "/portal/rest/v1/platform/branding/logo?lastModified=" + logo.getUpdatedDate();
    logoTitle = branding.getCompanyName();

    UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
    portalPath = portalConfigService.getUserHomePage(request.getRemoteUser());
    if (portalPath == null) {
      portalPath = "/portal/" + requestContext.getPortalOwner();
    }
    titleClass = "company";
  } else {
    logoPath = space.getAvatarUrl();
    logoTitle = space.getDisplayName();
    String permanentSpaceName = space.getGroupId().split("/")[2];
    portalPath = "/portal/g/:spaces:" + permanentSpaceName + "/" + space.getPrettyName();
    imageClass="spaceAvatar";
    titleClass = "space";
    spaceAvatarClass = "space-avatar";
  }

  String directionVuetifyClass = requestContext.getOrientation().isRT() ? "v-application--is-rtl" : "v-application--is-ltr";
%>
<script type="text/javascript">
  document.addEventListener('spaceDetailUpdated', event => {
    space = event && event.detail;
    if (space && space.displayName) {
      document.querySelector('.logoTitle').innerText = space.displayName;
      document.querySelector('.logoContainer .spaceAvatar').src = space.avatarUrl;
    }
  });
</script>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application border-box-sizing <%= directionVuetifyClass %> theme--light"
    id="brandingTopBar" flat="">
    <div class="v-application--wrap">
      <div class="container pa-0 ps-3">
        <div class="d-flex mx-0 pa-0">
          <% if (logoPath != null) { %>

          <a id="UserHomePortalLink" href="<%=portalPath%>" class="pr-3 logoContainer <%=spaceAvatarClass%> " >
            <img src="<%=logoPath%>" class="<%=imageClass%>" alt="<%= logoTitle%> - Homepage">
          </a>
          <% } %>
          <a href="<%=portalPath%>" title="<%=logoTitle%>" class="pl-2 align-self-center brandingContainer <%=titleClass%> <%=spaceAvatarClass%> ">
            <div class="logoTitle subtitle-2 font-weight-bold text-truncate">
              <%= logoTitle%>
            </div>
          </a>
          <a id="SpaceTitleActionComponnetsContainer"></a>
        </div>
      </div>
    </div>
  </div>
</div>
<script type="text/javascript">
  window.require(['SHARED/social-ui-profile'], function(socialProfile) {
    const labels = {
      StatusTitle: '<%=res.getString("UserProfilePopup.label.Loading")%>',
      Connect: '<%=res.getString("UserProfilePopup.label.Connect")%>',
      Confirm:'<%=res.getString("UserProfilePopup.label.Confirm")%>',
      CancelRequest: '<%=res.getString("UserProfilePopup.label.CancelRequest")%>',
      RemoveConnection: '<%=res.getString("UserProfilePopup.label.RemoveConnection")%>',
      Ignore: '<%=res.getString("UserProfilePopup.label.Ignore")%>',
      join: '<%=res.getString("UIManageAllSpaces.label.action_join")%>',
      leave: '<%=res.getString("UIManageAllSpaces.label.action_leave_space")%>',
      members: '<%=res.getString("UIManageInvitationSpaces.label.Members")%>',
    };
    socialProfile.initSpaceInfoPopup('UserHomePortalLink', labels,"<%=logoPath%>");
    });
</script>
