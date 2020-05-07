<%
  Object filter = request.getAttribute("filter");
			if (filter == null) {
				filter = "";
			} else {
				filter = ((String[]) filter)[0];
			}
%>
<div class="VuetifyApp">
  <div id="spaceMembersApplication">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/MembersPortlet'],
          app => app.init('<%=filter%>')
      );
    </script>
  </div>
</div>