<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@page import="io.meeds.social.space.template.model.SpaceTemplate"%>
<%@page import="java.util.List"%>
<%
SpaceTemplateService spaceTemplateService = ExoContainerContext.getService(SpaceTemplateService.class);
List <SpaceTemplate> spaceTemplates = spaceTemplateService.getSpaceTemplates();
%>
<div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="SpaceCreation">
      <script type="text/javascript">
        require(['PORTLET/social/SpaceCreation'], app => app.init('<%=spaceTemplates%>'));
      </script>
    </div>
  </div>