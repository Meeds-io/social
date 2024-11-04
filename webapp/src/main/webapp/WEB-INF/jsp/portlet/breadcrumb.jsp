<%@ page import="org.apache.commons.lang3.StringUtils"%>
<%
  String[] noThreeDotsValues = (String[]) request.getAttribute("noThreeDots");
  boolean noThreeDots = noThreeDotsValues != null && noThreeDotsValues.length > 0 && StringUtils.equals("true", noThreeDotsValues[0]);
%>
<div class="VuetifyApp">
  <div id="breadcrumb">
    <script type="text/javascript">
      require(['PORTLET/social/Breadcrumb'], app => app.init(<%=noThreeDots%>));
    </script>
  </div>
</div>