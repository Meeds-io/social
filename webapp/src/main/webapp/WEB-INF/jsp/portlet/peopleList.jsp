<%
  Object filterPreference = request.getAttribute("filter");
  String filter = filterPreference == null ? "" : ((String[]) filterPreference)[0];
  // A filter is a word: any other stored value is ignored
  if (filter == null || !filter.matches("[A-Za-z]*")) {
    filter = "";
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light peopleList"
    id="peopleListApplication" flat="">
    <script type="text/javascript">
      require(['PORTLET/social/PeopleList'],
          app => app.init('<%=filter%>')
      );
    </script>
  </div>
</div>