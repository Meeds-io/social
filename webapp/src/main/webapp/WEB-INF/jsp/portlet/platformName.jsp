<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="java.net.URLEncoder"%>

<%
  BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
  String platformName = brandingService.getCompanyName();
%>

<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light platformName"
    id="platformNameApplication">
    <script type="text/javascript">
      require(['PORTLET/social/PlatformName'], app =>app.init('<%=URLEncoder.encode(platformName.replace(" ", "._.")).replace("._.", " ")%>'));
    </script>
  </div>
</div>
