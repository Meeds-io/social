<%@ page import="org.exoplatform.social.core.space.SpaceUtils" %>
<%@ page import="org.exoplatform.social.core.space.model.Space" %>
<%@ page import="org.exoplatform.social.core.space.spi.SpaceService" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%@page import="org.apache.commons.text.StringEscapeUtils"%>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsUrl" />
<%
  boolean isManager = false;
  Object rawSettings = request.getAttribute("settings");
  String settings = null;
  if (rawSettings instanceof String[]) {
    settings = ((String[]) rawSettings)[0];
  }

  String portletId = (String) request.getAttribute("portletStorageId");
  String domId = "parentSpaceListingApplication" + portletId;
  String valueDomId = "parentSpaceApplicationSettingsValue" + portletId;
  Space currentSpace = SpaceUtils.getSpaceByContext();
  Long parentSpaceId = null;
  if (currentSpace != null) {
    parentSpaceId = currentSpace.getParentSpaceId();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    isManager = spaceService.canManageSpace(currentSpace, request.getRemoteUser());
  }
%>
<% if (parentSpaceId != null && parentSpaceId > 0) { %>
<div class="VuetifyApp">
  <div data-app="true"
       class="v-application v-application--is-ltr theme--light"
       id="<%=domId%>">
    <textarea id="<%=valueDomId%>" style="display:none;"><%=settings == null ? "{}" : StringEscapeUtils.escapeJava(settings).replace("\\\"", "\"").replace("\\\\\"", "\\\"").replace("\\n", "") %></textarea>
    <script type="text/javascript">
        require(['PORTLET/social/ParentSpaceListing'],
            app => app.init('<%=domId%>', '<%=parentSpaceId%>', <%=isManager%>, JSON.parse(document.getElementById('<%=valueDomId%>').value), '<%=saveSettingsUrl%>')
        );
    </script>
  </div>
</div>
<% } %>