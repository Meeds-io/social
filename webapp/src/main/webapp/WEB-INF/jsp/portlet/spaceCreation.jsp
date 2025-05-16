<%@page import="org.exoplatform.services.security.ConversationState"%>
<%@page import="org.exoplatform.portal.config.UserACL"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@page import="io.meeds.social.space.template.model.SpaceTemplate"%>
<%@page import="io.meeds.social.space.template.model.SpaceTemplateFilter"%>
<%@page import="io.meeds.social.util.JsonUtils"%>
<%@page import="org.springframework.data.domain.Pageable"%>
<%@page import="java.util.List"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@page import="java.util.Map" %>
<%@page import="javax.portlet.PortletPreferences" %>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsUrl" />
<%

UserACL userAcl = ExoContainerContext.getService(UserACL.class);
boolean isAdministrator = ConversationState.getCurrent() != null && userAcl.isAdministrator(ConversationState.getCurrent().getIdentity());
SpaceTemplateService spaceTemplateService = ExoContainerContext.getService(SpaceTemplateService.class);
SpaceTemplateFilter spaceTemplateFilter = new SpaceTemplateFilter(request.getRemoteUser(), request.getLocale(), false);
List<SpaceTemplate> defaultSpaceTemplates = spaceTemplateService.getSpaceTemplates(spaceTemplateFilter, Pageable.unpaged(), true);
String defaultJson = JsonUtils.toJsonString(defaultSpaceTemplates);

Object rawSettings = request.getAttribute("settings");
String settingsJson = null;
if (rawSettings instanceof String[]) {
  settingsJson = ((String[]) rawSettings)[0];
}

String settings = "{}";
if (settingsJson != null) {
  try {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> parsedSettings = mapper.readValue(settingsJson, Map.class);
    settings = mapper.writeValueAsString(parsedSettings);
  } catch (Exception e) {
    System.out.println("Error parsing settings: " + e.getMessage());
  }
}

String portletId = (String) request.getAttribute("portletStorageId");
String domId = "spaceCreationApplication" + portletId;
String valueDomId = "spaceCreationApplicationSettingsValue" + portletId;

 %>
<div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="<%=domId%>">
      <textarea id="<%=valueDomId%>" style="display:none;"><%=settings%></textarea>
      <script type="text/javascript">
        require(['PORTLET/social/SpaceCreation'], app => app.init('<%=domId%>', JSON.parse(document.getElementById('<%=valueDomId%>').value), <%=isAdministrator%>, '<%=saveSettingsUrl%>', <%=defaultJson%>));
      </script>
    </div>
  </div>