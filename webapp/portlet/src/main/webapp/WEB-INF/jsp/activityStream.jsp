<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.gatein.portal.controller.resource.ResourceRequestHandler"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  List<String> activitiesListURL = new ArrayList<>();
  String activityId = rcontext.getRequest().getParameter("id");
  long initialLimit = 20;
  if (activityId == null) {
    Space space = SpaceUtils.getSpaceByContext();
    activitiesListURL.add("/portal/rest/v1/social/activities?spaceId=" + (space == null ? "" : space.getId()) + "&limit=" + initialLimit + "&expand=ids");
  } else {
    activitiesListURL.add("/portal/rest/v1/social/activities/" + activityId + "?expand=identity,likes,shared");
    activitiesListURL.add("/portal/rest/v1/social/activities/" + activityId + "/comments?returnSize=true&sortDescending=true&offset=0&limit=2&expand=identity,likes,subComments");
  }
%>
<div class="VuetifyApp">
  <% for (String activityStreamURL : activitiesListURL) { %>
  <link rel="preload" href="<%=activityStreamURL%>" as="fetch">
  <% } %>
  <div id="ActivityStream"
    class="v-application transparent v-application--is-ltr theme--light activity-stream"
    data-app="true" flat="">
    <script type="text/javascript">
      fetch('<%=activitiesListURL.get(0)%>', {
        method: 'GET',
        credentials: 'include',
        mode: 'no-cors',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error('Response code indicates a server error', resp);
        } else {
          return resp.json();
        }
      }).then(initialData => {
        require(['SHARED/ActivityStream'], app => app.init(initialData, <%=initialLimit%>));
      });
    </script>
  </div>
</div>
