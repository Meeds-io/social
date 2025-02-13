<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.social.rest.api.RestUtils" %>
<%@ page import="io.meeds.portal.security.service.SecuritySettingService" %>
<%@ page import="io.meeds.portal.security.constant.UserRegistrationType" %>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsUrl" />
<div class="VuetifyApp">
<%
  String portletId = (String) request.getAttribute("portletStorageId");
  String domId = "spacesListApplication" + portletId;
  String valueDomId = "spaceListSettingsValue" + portletId;
  if (RestUtils.canAccessAnonymousResources()) {
    Object filter = request.getAttribute("filter");
    if (filter == null) {
      filter = "";
    } else {
      filter = ((String[]) filter)[0];
    }
    boolean canCreateSpace = ExoContainerContext.getService(SpaceTemplateService.class)
            .canCreateSpace(request.getRemoteUser());
    boolean canEdit = (boolean) request.getAttribute("canEdit");
    Object settings = (String[]) request.getAttribute("settings");
    if (settings != null) {
      settings = ((String[]) settings)[0];
    }
    SecuritySettingService securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
    boolean isExternalFeatureEnabled = securitySettingService.getRegistrationType() == UserRegistrationType.OPEN || securitySettingService.isRegistrationExternalUser();
%>
  <div data-app="true"
       class="v-application transparent v-application--is-ltr theme--light"
       id="<%=domId%>">
    <textarea id="<%=valueDomId%>" style="display:none;"><%=settings == null ? "{}" : settings%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social/SpacesList'],
          app => app.init('<%=domId%>', '<%=filter%>', <%=canCreateSpace%>, <%=isExternalFeatureEnabled%>, <%=canEdit%>, JSON.parse(document.getElementById('<%=valueDomId%>').value), '<%=saveSettingsUrl%>')
      );
    </script>
  <% } else { %>
    <script type="text/javascript">
      require(['SHARED/vue'], () => Vue.prototype.$updateApplicationVisibility(false, document.querySelector('#<%=domId%>')));
    </script>
  <% } %>
  </div>