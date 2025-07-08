<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@page import="org.apache.commons.text.StringEscapeUtils"%>


<%
  BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
  String authenticationBackground = brandingService.getLoginBackgroundPath();
  String authenticationTitle = brandingService.getLoginTitle(request.getLocale());
  String authenticationSubtitle = brandingService.getLoginSubtitle(request.getLocale());
  String loginBackgroundAltText = brandingService.getLoginBackgroundAltText();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light sidebarLogin"
    id="sidebarLoginApplication">
    <script type="text/javascript">
      require(['PORTLET/social/SidebarLogin'], app =>app.init(
        <%=authenticationBackground == null ? null : String.format("\"%s\"", authenticationBackground)%>,
        <%=loginBackgroundAltText == null ? null : String.format("\"%s\"", StringEscapeUtils.escapeJava(loginBackgroundAltText))%>,
        <%=authenticationTitle == null ? null : String.format("\"%s\"", StringEscapeUtils.escapeJava(authenticationTitle))%>,
        <%=authenticationSubtitle == null ? null : String.format("\"%s\"", StringEscapeUtils.escapeJava(authenticationSubtitle))%>,
      ));
    </script>
  </div>
</div>
