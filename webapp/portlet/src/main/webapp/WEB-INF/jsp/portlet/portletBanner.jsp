<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.PortletMode" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />
<portlet:actionURL var="saveSettingsURL" />
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  PortletPreferences preferences = renderRequest.getPreferences();
	String bannerUrl = preferences.getValue("bannerUrl", "");
  String fileId = preferences.getValue("fileId", "default");
  int maxUploadSize = identityManager.getImageUploadLimit();
%>
<div class="VuetifyApp">
  <div id="portletBanner">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/ImagesPortlet'], app => app.init(
        '<%=bannerUrl%>',
        '<%=fileId%>',
        <%=maxUploadSize%>,
        <%= "'" + saveSettingsURL + "'" %>
      ));
    </script>
  </div>
</div>
