<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%
  String name = (String) request.getAttribute("settingName");
  boolean canEdit = (boolean) request.getAttribute("canEdit");
  String id = "LinkPortlet" + renderRequest.getWindowID();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light" flat=""
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/Links'], app => app.init('<%=id%>', '<%=name%>', <%=canEdit%>));
    </script>
  </div>
</div>