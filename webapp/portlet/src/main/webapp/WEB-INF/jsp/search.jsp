<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="org.exoplatform.social.core.search.SearchConnector"%>
<%@page import="java.util.Set"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.search.SearchService"%>
<%
  SearchService searchService = ExoContainerContext.getService(SearchService.class);
  Set<SearchConnector> connectors = searchService.getConnectors();
  String jsonSearchConnectors = EntityBuilder.toJsonString(connectors);
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SearchApplication">
    <div class="v-application--wrap">
      <button
        type="button"
        class="transparent v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default">
        <span class="v-btn__content">
          <i class="uiIconPLF24x24Search"></i>
        </span>
      </button>
    </div>
    <textarea id="searchConnectorsDefaultValue" class="hidden"><%= jsonSearchConnectors%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/Search'], app => app.init(JSON.parse(document.getElementById('searchConnectorsDefaultValue').value)));
    </script>
  </div>
</div>
