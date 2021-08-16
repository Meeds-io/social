<%
  Object filter = request.getAttribute("filter");
  if (filter == null) {
    filter = "";
  } else {
    filter = ((String[]) filter)[0];
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light peopleList"
    id="peopleListApplication" flat="">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/PeopleList'],
          app => app.init('<%=filter%>')
      );
    </script>
  </div>
</div>