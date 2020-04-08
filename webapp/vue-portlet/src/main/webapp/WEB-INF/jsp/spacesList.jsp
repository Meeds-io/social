<% 
  Object filter = request.getAttribute("filter");
  if (filter == null) {
    filter = "";
  } else {
    filter = ((String[])filter)[0];
  }
%>
<div class="VuetifyApp">
  <div id="spacesListApplication">
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/SpacesList'],
          app => app.init('<%=filter%>')
      );
    </script>
  </div>
</div>
