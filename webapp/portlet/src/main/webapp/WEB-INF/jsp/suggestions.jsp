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
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application hiddenable-widget transparent v-application--is-ltr theme--light"
    id="SuggestionsPeopleAndSpace" flat="">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SuggestionsPeopleAndSpace'],
        app => app.init('<%=suggestionsType%>')
      );
    </script>
  </div>
</div>