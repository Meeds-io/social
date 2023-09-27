<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsURL" />
<%
  PortletPreferences preferences = renderRequest.getPreferences();
  String name = preferences.getValue("name", null);
  boolean nameExists = name != null;
  if (!nameExists) {
    name = renderRequest.getWindowID();
  }
  String id = "LinkPortlet" + renderRequest.getWindowID();
  Boolean canEdit = (Boolean) request.getAttribute("canEdit");
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light" flat=""
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/Links'], app => app.init('<%=id%>', '<%=name%>', <%=canEdit%>, <%=nameExists%>, '<%=saveSettingsURL%>'));
    </script>
  </div>
</div>