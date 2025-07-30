<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="java.net.URLEncoder"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%

  String id = "platformName-" + renderRequest.getWindowID();

  BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
  String platformName = brandingService.getCompanyName();
%>

<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light platformName"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/PlatformName'], app =>app.init('<%=id%>','<%=URLEncoder.encode(platformName.replace(" ", "._.")).replace("._.", " ")%>'));
    </script>
  </div>
</div>
