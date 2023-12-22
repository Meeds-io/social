<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@ page import="org.exoplatform.portal.mop.user.UserNode" %>
<%@ page import="org.exoplatform.portal.webui.util.Util" %>
<%
  UserNode selectedUserNode = Util.getUIPortal().getSelectedUserNode();
  String[] noThreeDotsValues = (String[]) request.getAttribute("noThreeDots");
  boolean noThreeDots = noThreeDotsValues != null && noThreeDotsValues.length > 0 && StringUtils.equals("true", noThreeDotsValues[0]);
%>
<div class="VuetifyApp">
  <div id="breadcrumb">
    <script type="text/javascript">
      eXo.env.portal.selectedNodeId = '<%= selectedUserNode.getId() %>'
      require(['PORTLET/social-portlet/Breadcrumb'], app => app.init(<%=noThreeDots%>));
    </script>
  </div>
</div>