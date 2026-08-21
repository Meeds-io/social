<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%
  String id = "accountDeactivated-" + renderRequest.getWindowID();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light accountDeactivated"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/AccountDeactivated'], app => app.init('<%=id%>'));
    </script>
  </div>
</div>
