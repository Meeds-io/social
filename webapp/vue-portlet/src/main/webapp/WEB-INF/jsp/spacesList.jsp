<% 
  Object filter = request.getAttribute("filter");
  if (filter == null) {
    filter = "";
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
