<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.social.core.identity.provider.SpaceIdentityProvider"%>
<%@page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.exoplatform.portal.mop.user.UserNode"%>
<%@page import="java.util.List"%>
<%
  request.setAttribute("SPACE_HEADER_DISPLAYED", true);

  Space space = SpaceUtils.getSpaceByContext();
	String bannerUrl = space.getBannerUrl();

	IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
	int maxUploadSize = identityManager.getImageUploadLimit();

	SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
	boolean isAdmin = spaceService.isSuperManager(request.getRemoteUser())
			|| spaceService.isManager(space, request.getRemoteUser());

	List<UserNode> navigations = (List<UserNode>) request.getAttribute("navigations");
%>

<div class="VuetifyApp">
  <div data-app="true"
    class="v-application spaceMenuParent white v-application--is-ltr theme--light <%= navigations != null ? "hasNavigations" : ""%>"
    id="SpaceHeader">
    <v-cacheable-dom-app cache-id="SpaceHeader_<%=space.getId()%>"></v-cacheable-dom-app>
    <%
      if (navigations != null) {
    				Iterator<UserNode> navIterator = navigations.iterator();
    				Map<String, String> navigationsUri = (Map<String, String>) request.getAttribute("navigationsUri");
    				String selectedUri = (String) request.getAttribute("selectedUri");
    %>
    <textarea id="SpaceHeaderNavigationsValue" class="hidden">{
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
    					out.print("\"icon\": \"");
    					out.print(userNode.getIcon());
    					out.print("\",");
    					out.print("\"uri\": \"");
    					out.print(navigationsUri.get(userNode.getId()));
    					out.print("\"");
    					out.print("}");
    					if (navIterator.hasNext()) {
    						out.print(",");
    					}
    				}
    %>
        ],
        "selectedNavigationUri": "<%=selectedUri%>"
      }</textarea>
    <script type="text/javascript">
        require(['PORTLET/social-portlet/SpaceMenuPortlet'],
          app => app.init(
            JSON.parse(document.getElementById('SpaceHeaderNavigationsValue').value),
            '<%=bannerUrl%>',
            <%=maxUploadSize%>,
            <%=isAdmin%>
          )
        );
      </script>
    <%
      } else {
    %>
    <script type="text/javascript">
        require(['PORTLET/social-portlet/SpaceMenuPortlet'], app => app.init(null, '<%=bannerUrl%>', <%=maxUploadSize%>, <%=isAdmin%>));
      </script>
    <%
      }
    %>
  </div>
</div>