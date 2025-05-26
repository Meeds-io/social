<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.social.space.template.service.SpaceTemplateService"%>
<%@page import="io.meeds.social.space.template.model.SpaceTemplate"%>
<%@page import="io.meeds.social.space.template.model.SpaceTemplateFilter"%>
<%@page import="io.meeds.social.util.JsonUtils"%>
<%@page import="org.springframework.data.domain.Pageable"%>
<%@page import="java.util.List"%>
<%
SpaceTemplateService spaceTemplateService = ExoContainerContext.getService(SpaceTemplateService.class);
SpaceTemplateFilter spaceTemplateFilter = new SpaceTemplateFilter(request.getRemoteUser(), request.getLocale(), false);
List<SpaceTemplate> spaceTemplates = spaceTemplateService.getSpaceTemplates(spaceTemplateFilter, Pageable.unpaged(), true);
%>
<div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="SpaceCreation">
      <script type="text/javascript">
        require(['PORTLET/social/SpaceCreation'], app => app.init(<%=JsonUtils.toJsonString(spaceTemplates)%>));
      </script>
    </div>
  </div>