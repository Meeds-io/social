<%@page import="org.json.JSONObject"%>
<%@page import="java.util.Collections"%>
<%@page import="jakarta.servlet.http.HttpSession"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%
  String id = "externalOnboarding-" + renderRequest.getWindowID();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light externalOnboarding"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/ExternalOnboarding'], app =>app.init('<%=id%>'));
    </script>
  </div>
</div>
