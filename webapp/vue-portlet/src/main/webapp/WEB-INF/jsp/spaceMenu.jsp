<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.exoplatform.portal.mop.user.UserNode"%>
<%@page import="java.util.List"%>
<%
  List<UserNode> navigations = (List<UserNode>) request.getAttribute("navigations");
  Map<String, String> navigationsUri = (Map<String, String>) request.getAttribute("navigationsUri");
  String selectedUri = (String) request.getAttribute("selectedUri");

  Iterator<UserNode> navIterator = navigations.iterator();
%>
<div class="VuetifyApp">
  <div id="SpaceMenuApplication">
    <textarea id="SpaceMenuNavigationsValue" class="hidden">{
      "navigations": [
        <%
        while (navIterator.hasNext()) {
          UserNode userNode = navIterator.next();

          out.print("{");
          out.print("\"id\": \"");
          out.print(userNode.getId());
          out.print("\",");
          out.print("\"label\": \"");
          out.print(userNode.getResolvedLabel());
          out.print("\",");
          out.print("\"uri\": \"");
          out.print(navigationsUri.get(userNode.getId()));
          out.print("\"");
          out.print("}");
          if (navIterator.hasNext()) {
            out.print(",");
          }
        } %>
      ],
      "selectedNavigationUri": "<%=selectedUri%>"
    }</textarea>
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/SpaceMenu'], app => app.init(
          JSON.parse(document.getElementById('SpaceMenuNavigationsValue').value)
        )
      );
    </script>
  </div>
</div>
