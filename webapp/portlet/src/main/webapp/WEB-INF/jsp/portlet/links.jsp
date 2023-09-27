<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsURL" />
<%
  String name = (String) request.getAttribute("settingName");
  boolean isInitialized = (boolean) request.getAttribute("isInitialized");
  boolean canEdit = (boolean) request.getAttribute("canEdit");
  String id = "LinkPortlet" + renderRequest.getWindowID();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light" flat=""
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/Links'], app => app.init('<%=id%>', '<%=name%>', <%=canEdit%>, <%=isInitialized%>, '<%=saveSettingsURL%>'));
    </script>
  </div>
</div>