<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="java.net.URLEncoder" %>
<portlet:defineObjects/>
<portlet:actionURL var="saveSettingsUrl" />
<%
  int maxUploadSize = ExoContainerContext.getService(IdentityManager.class).getImageUploadLimit();
  String portletId = (String) request.getAttribute("portletStorageId");
  String domId = "activityStream" + portletId;
  String valueDomId = "activityStreamValue" + portletId;
  boolean canEdit = (boolean) request.getAttribute("canEdit");
  Object settings = (String[]) request.getAttribute("settings");
  if (settings != null) {
    settings = ((String[]) settings)[0];
  }
%>
<div class="VuetifyApp">
  <div
    class="v-application transparent v-application--is-ltr theme--light activity-stream"
    data-app="true"
    flat=""
    id="<%=domId%>">
    <div class="white border-radius activity-detail flex d-flex flex-column">
      <div role="progressbar" aria-valuemin="0" aria-valuemax="100"
        class="v-progress-circular mx-auto my-10 v-progress-circular--indeterminate primary--text"
        style="height: 32px; width: 32px;">
        <svg xmlns="http://www.w3.org/2000/svg"
          viewBox="22.857142857142858 22.857142857142858 45.714285714285715 45.714285714285715"
          style="transform: rotate(0deg);">
          <circle fill="transparent" cx="45.714285714285715"
            cy="45.714285714285715" r="20"
            stroke-width="5.714285714285714" stroke-dasharray="125.664"
            stroke-dashoffset="125.66370614359172px"
            class="v-progress-circular__overlay"></circle></svg>
        <div class="v-progress-circular__info"></div>
      </div>
    </div>
    <textarea id="<%=valueDomId%>" style="display:none;"><%=settings == null ? "{}" : URLEncoder.encode(settings.toString().replace(" ", "._.")).replace("._.", " ").replace("\\\"", "\"").replace("\\\\\"", "\\\"")%></textarea>
    <script type="text/javascript">
      require(['SHARED/ActivityStream'], app => app.init({
        appId: '<%=domId%>',
        settings: JSON.parse(decodeURIComponent(document.getElementById('<%=valueDomId%>').value)),
        saveSettingsUrl: '<%=saveSettingsUrl%>',
        canEdit: <%=canEdit%>,
        maxUploadSize: <%=maxUploadSize%>,
      }));
    </script>
  </div>
</div>
