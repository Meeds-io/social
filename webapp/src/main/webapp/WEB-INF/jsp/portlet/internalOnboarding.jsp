<%@page import="org.json.JSONObject"%>
<%@page import="java.util.Collections"%>
<%@page import="jakarta.servlet.http.HttpSession"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%
  String id = "internalOnboarding-" + renderRequest.getWindowID();
  JSONObject params = new JSONObject();

  Collections.list(request.getAttributeNames()).forEach(s -> params.put(s, request.getAttribute(s)));

%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light internalOnboarding"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/InternalOnboarding'], app =>app.init('<%=id%>',JSON.stringify(<%=params.toString()%>)));
    </script>
  </div>
</div>
