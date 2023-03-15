<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.web.application.RequestContext"%>
<%@page import="org.exoplatform.social.core.space.SpaceAccessType"%>
<%
  PortalRequestContext portalRequestContext = RequestContext.getCurrentInstance();
  HttpSession portalSession = portalRequestContext.getRequest().getSession();
  SpaceAccessType spaceAccessType = (SpaceAccessType) portalSession.getAttribute(SpaceAccessType.ACCESSED_TYPE_KEY);
  if (spaceAccessType == null) {
    return;
  }
  String spaceAccessTypeLabel = spaceAccessType.name();
  String spaceId = (String) portalSession.getAttribute(SpaceAccessType.ACCESSED_SPACE_ID_KEY);
  String spacePrettyName = (String) portalSession.getAttribute(SpaceAccessType.ACCESSED_SPACE_PRETTY_NAME_KEY);
  String spaceDisplayName = (String) portalSession.getAttribute(SpaceAccessType.ACCESSED_SPACE_DISPLAY_NAME_KEY);
  String originalUri = (String) portalSession.getAttribute(SpaceAccessType.ACCESSED_SPACE_REQUEST_PATH_KEY);
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SpaceAccess">
    <textarea id="SpaceAccessData" rows="0" class="d-none">{
      "spaceId": "<%=spaceId%>",
      "spaceAccessTypeLabel": "<%=spaceAccessTypeLabel%>",
      "spacePrettyName": "<%=spacePrettyName%>",
      "spaceDisplayName": "<%=spaceDisplayName%>",
      "originalUri": "<%=originalUri%>"
    }</textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SpaceAccessPortlet'],
        app => app.init(
          JSON.parse(document.getElementById('SpaceAccessData').value.replace(/\n/g, '')),
        )
      );
    </script>
  </div>
</div>