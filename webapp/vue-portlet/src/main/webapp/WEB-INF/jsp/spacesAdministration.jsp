<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="org.exoplatform.application.registry.ApplicationCategory"%>
<%@page import="java.util.Collection"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.application.registry.ApplicationRegistryService"%>
<%
  ApplicationRegistryService registryService = ExoContainerContext.getService(ApplicationRegistryService.class);
  Collection<ApplicationCategory> categories = registryService.detectPortletsFromWars();
  String jsonEntity = EntityBuilder.toJsonString(categories);
%>
<div class="VuetifyApp">
  <div id="spacesAdministration">
    <textarea
      id="spaceApplicationItemsValue"
      class="hidden"><%=jsonEntity == null ? "[]" : jsonEntity%></textarea>
    <script>
      require(['PORTLET/social-vue-portlet/SpacesAdministration'], function(spacesAdministrationApp) {
        spacesAdministrationApp.init(JSON.parse(document.getElementById('spaceApplicationItemsValue').value));
      });
    </script>
  </div>
</div>