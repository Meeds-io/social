<%@page import="org.exoplatform.services.security.ConversationState"%>
<%@page import="org.exoplatform.portal.config.UserACL"%>
<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.social.rest.api.RestUtils" %>
<%@ page import="io.meeds.portal.security.service.SecuritySettingService" %>
<%@ page import="io.meeds.portal.security.constant.UserRegistrationType" %>
<%@ page import="java.net.URLEncoder" %>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsUrl" />
<portlet:resourceURL var="getSettingNameUrl" />
<div class="VuetifyApp">
<%
  String portletId = (String) request.getAttribute("portletStorageId");
  String domId = "spacesListApplication" + portletId;
  String valueDomId = "spaceListSettingsValue" + portletId;

  Object settingName = (String[]) request.getAttribute("name");
  if (settingName != null) {
    settingName = ((String[]) settingName)[0];
  }
  if (RestUtils.canAccessAnonymousResources((String) settingName)) {
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
    Object publicAccess = (String[]) request.getAttribute("publicAccess");
    if (publicAccess != null) {
      publicAccess = ((String[]) publicAccess)[0];
    }
    SecuritySettingService securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
    UserRegistrationType registrationType = securitySettingService.getRegistrationType();
    boolean isExternalFeatureEnabled = registrationType == UserRegistrationType.OPEN || securitySettingService.isRegistrationExternalUser();
    UserACL userAcl = ExoContainerContext.getService(UserACL.class);
    boolean isAdministrator = ConversationState.getCurrent() != null && userAcl.isAdministrator(ConversationState.getCurrent().getIdentity());
    Object isPublicPage = request.getAttribute("isPublicPage");
    if (isPublicPage == null) {
      isPublicPage = false;
    }
%>
  <div data-app="true"
       class="v-application transparent v-application--is-ltr theme--light"
       id="<%=domId%>">
    <textarea id="<%=valueDomId%>" style="display:none;"><%=settings == null ? "{}" : URLEncoder.encode(settings.toString().replace(" ", "._.")).replace("._.", " ").replace("\\\"", "\"").replace("\\\\\"", "\\\"")%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social/SpacesList'],
          app => app.init('<%=domId%>', '<%=filter%>', <%=canCreateSpace%>, <%=isExternalFeatureEnabled%>, <%=canEdit%>, JSON.parse(decodeURIComponent(document.getElementById('<%=valueDomId%>').value)), '<%=saveSettingsUrl%>', '<%=getSettingNameUrl%>', '<%=settingName == null ? "" : settingName%>', '<%=registrationType%>', <%=isAdministrator%>, <%=isPublicPage%>)
      );
    </script>
  <% } else { %>
    <script type="text/javascript">
      require(['SHARED/vue'], () => Vue.prototype.$updateApplicationVisibility(false, document.querySelector('#<%=domId%>')));
    </script>
  <% } %>
  </div>
