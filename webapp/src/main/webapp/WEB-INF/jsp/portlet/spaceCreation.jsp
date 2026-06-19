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
<%@page import="org.apache.commons.text.StringEscapeUtils"%>
<%@ page import="java.net.URLEncoder" %>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsUrl" />
<%

UserACL userAcl = ExoContainerContext.getService(UserACL.class);
boolean isAdministrator = ConversationState.getCurrent() != null && userAcl.isAdministrator(ConversationState.getCurrent().getIdentity());
SpaceTemplateService spaceTemplateService = ExoContainerContext.getService(SpaceTemplateService.class);
SpaceTemplateFilter spaceTemplateFilter = new SpaceTemplateFilter(request.getRemoteUser(), request.getLocale(), false);
List<SpaceTemplate> spaceTemplates = spaceTemplateService.getSpaceTemplates(spaceTemplateFilter, Pageable.unpaged(), true);
String spaceTemplatesJson = JsonUtils.toJsonString(spaceTemplates);

Object rawSettings = request.getAttribute("settings");
String settings = null;
if (rawSettings instanceof String[]) {
  settings = ((String[]) rawSettings)[0];
}

String portletId = (String) request.getAttribute("portletStorageId");
String domId = "spaceCreationApplication" + portletId;
String valueDomId = "spaceCreationApplicationSettingsValue" + portletId;
%>
<div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="<%=domId%>">
      <textarea id="spaceCreationSettings<%=valueDomId%>" style="display:none;"><%=settings == null ? "{}" : URLEncoder.encode(settings.replace(" ", "._.")).replace("._.", " ").replace("\\\"", "\"").replace("\\\\\"", "\\\"").replace("\\n", "") %></textarea>
      <textarea id="spaceCreationTemplate<%=valueDomId%>" style="display:none;"><%=URLEncoder.encode(spaceTemplatesJson.replace(" ", "._.")).replace("._.", " ").replace("\\\"", "\"").replace("\\\\\"", "\\\"").replace("\\\\n", "").replace("\\n", "") %></textarea>
      <script type="text/javascript">
        require(['PORTLET/social/SpaceCreation'], app => app.init('<%=domId%>', JSON.parse(decodeURIComponent(document.getElementById('spaceCreationSettings<%=valueDomId%>').value)), <%=isAdministrator%>, '<%=saveSettingsUrl%>', JSON.parse(decodeURIComponent(document.getElementById('spaceCreationTemplate<%=valueDomId%>').value))));
      </script>
    </div>
  </div>
