<%@page import="org.exoplatform.services.security.ConversationState"%>
<%@page import="org.exoplatform.portal.config.UserACL"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@page import="io.meeds.social.space.template.model.SpaceTemplate"%>
<%@page import="io.meeds.social.space.template.model.SpaceTemplateFilter"%>
<%@page import="io.meeds.social.util.JsonUtils"%>
<%@page import="org.springframework.data.domain.Pageable"%>
<%@page import="java.util.List"%>
<%@ page import="javax.portlet.PortletPreferences" %>
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
PortletPreferences preferences = renderRequest.getPreferences();
String spaceCreationTemplateChoice = preferences.getValue("spaceCreationTemplateChoice", "anyTemplate");
String spaceTemplates = preferences.getValue("spaceTemplates", defaultJson);
%>
<div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="SpaceCreation">
      <script type="text/javascript">
        require(['PORTLET/social/SpaceCreation'], app => app.init('<%=spaceTemplates%>', <%=isAdministrator%>, '<%=saveSettingsUrl%>', '<%=spaceCreationTemplateChoice%>'));
      </script>
    </div>
  </div>