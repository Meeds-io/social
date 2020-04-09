<%
  Object suggestionsType = request.getAttribute("suggestionsType");
  if (suggestionsType == null) {
    suggestionsType = "";
  } else {
    suggestionsType = ((String[])suggestionsType)[0];
  }
%>
<div class="VuetifyApp">
  <div id="SuggestionsPeopleAndSpace">
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/SuggestionsPeopleAndSpace'],
        app => app.init('<%=suggestionsType%>')
      );
    </script>
  </div>
</div>
