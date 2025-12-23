<%@ page import="org.exoplatform.social.core.space.SpaceUtils" %>
<%@ page import="org.exoplatform.social.core.space.model.Space" %>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%
  Object applicationIdParam =  request.getAttribute("applicationId");
  String applicationId;
  if (applicationIdParam instanceof String[]) {
    applicationId = ((String[]) applicationIdParam)[0];
  } else {
    applicationId = (String) applicationIdParam;
  }
  Space space = SpaceUtils.getSpaceByContext();
  Long parentSpaceId = null;
  if (space != null) {
    parentSpaceId = space.getParentSpaceId();
  }
%>
<% if (parentSpaceId != null && parentSpaceId > 0) { %>
<div class="VuetifyApp">
  <div data-app="true"
       class="v-application v-application--is-ltr theme--light"
       id="parentSpaceListing">
    <script type="text/javascript">
        require(['PORTLET/social/ParentSpaceListing'],
            app => app.init('<%=parentSpaceId%>', '<%=applicationId%>')
        );
    </script>
  </div>
</div>
<% } %>