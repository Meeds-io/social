<%@ page import="org.exoplatform.portal.mop.user.UserNode" %>
<%@ page import="org.exoplatform.portal.webui.util.Util" %>
<%
  UserNode selectedUserNode = Util.getUIPortal().getSelectedUserNode();
  String nodeId = selectedUserNode.getId();
%>
<div class="VuetifyApp">
  <div id="breadcrumb">
    <script type="text/javascript">
      eXo.env.portal.selectedNodeId = '<%= nodeId %>'
      require(['PORTLET/social-portlet/Breadcrumb'], app => app.init());
    </script>
  </div>
</div>