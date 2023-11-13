<%@ page import="org.exoplatform.portal.mop.user.UserNode" %>
<%@ page import="org.exoplatform.portal.webui.util.Util" %>
<%
  UserNode selectedUserNode = Util.getUIPortal().getSelectedUserNode();
%>
<div class="VuetifyApp singlePageApplication">
  <div id="breadcrumb">
    <script type="text/javascript">
      eXo.env.portal.selectedNodeId = '<%= selectedUserNode.getId() %>'
      require(['PORTLET/social-portlet/Breadcrumb'], app => app.init());
    </script>
  </div>
</div>