<script type="text/javascript" defer="defer">
  <% String jsModule = ((String[])request.getAttribute("jsModule"))[0]; %>
  eXo.env.portal.onLoadCallbacks.push(() => require(['<%=jsModule%>']));
</script>
