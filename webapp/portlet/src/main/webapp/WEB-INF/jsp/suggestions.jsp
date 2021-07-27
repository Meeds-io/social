<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  Object suggestionsType = request.getAttribute("suggestionsType");
  if (suggestionsType == null) {
    suggestionsType = "";
  } else {
    suggestionsType = ((String[])suggestionsType)[0];
  }
  List<String> preloadURLs = new ArrayList<>();
  if (suggestionsType == "user") {
    preloadURLs.add("/portal/rest/homepage/intranet/people/contacts/suggestions");
  } else if (suggestionsType == "space") {
    preloadURLs.add("/portal/rest/homepage/intranet/spaces/suggestions");
  } else {
    preloadURLs.add("/portal/rest/homepage/intranet/people/contacts/suggestions");
    preloadURLs.add("/portal/rest/homepage/intranet/spaces/suggestions");
  }
%>
<div class="VuetifyApp">
  <% for (String preloadURL : preloadURLs) { %>
  <link rel="preload" href="<%=preloadURL%>" as="fetch" crossorigin="use-credentials">
  <% } %>
  <div data-app="true"
    class="v-application hiddenable-widget transparent v-application--is-ltr theme--light"
    id="SuggestionsPeopleAndSpace" flat="">
    <v-cacheable-dom-app cache-id="SuggestionsPeopleAndSpace_<%=suggestionsType%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SuggestionsPeopleAndSpace'],
        app => app.init('<%=suggestionsType%>')
      );
    </script>
  </div>
</div>