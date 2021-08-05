<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.gatein.portal.controller.resource.ResourceRequestHandler"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.commons.api.settings.ExoFeatureService"%>
<%@page import="org.exoplatform.services.security.ConversationState"%>
<%
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  List<String> activitiesListURL = new ArrayList<>();
  String activityId = rcontext.getRequest().getParameter("id");
  long limitToDisplay = 10;
  long initialLimit = limitToDisplay * 2;
  String activitiesLoadingURL;
  if (activityId == null) {
    Space space = SpaceUtils.getSpaceByContext();
    ExoFeatureService featureService = ExoContainerContext.getService(ExoFeatureService.class);
    if (featureService.isActiveFeature("ActivitiesSmoothLoading")) {
      activitiesLoadingURL = "/portal/rest/v1/social/activities?spaceId=" + (space == null ? "" : space.getId()) + "&limit=" + initialLimit + "&expand=ids";
    } else {
      activitiesLoadingURL = "/portal/rest/v1/social/activities?spaceId=" + (space == null ? "" : space.getId()) + "&limit=" + initialLimit + "&expand=identity,likes,shared,commentsPreview,subComments";
    }
  } else {
    activitiesLoadingURL = "/portal/rest/v1/social/activities/" + activityId + "?expand=identity,likes,shared,commentsPreview,subComments";
  }
%>
<div class="VuetifyApp">
  <link rel="preload" href="<%=activitiesLoadingURL%>" as="fetch">
  <div id="ActivityStream"
    class="v-application transparent v-application--is-ltr theme--light activity-stream"
    data-app="true" flat="">
    <div class="white border-radius activity-detail flex d-flex flex-column">
      <div role="progressbar" aria-valuemin="0" aria-valuemax="100"
        class="v-progress-circular mx-auto my-10 v-progress-circular--indeterminate primary--text"
        style="height: 32px; width: 32px;">
        <svg xmlns="http://www.w3.org/2000/svg"
          viewBox="22.857142857142858 22.857142857142858 45.714285714285715 45.714285714285715"
          style="transform: rotate(0deg);">
          <circle fill="transparent" cx="45.714285714285715"
            cy="45.714285714285715" r="20"
            stroke-width="5.714285714285714" stroke-dasharray="125.664"
            stroke-dashoffset="125.66370614359172px"
            class="v-progress-circular__overlay"></circle></svg>
        <div class="v-progress-circular__info"></div>
      </div>
    </div>
    <script type="text/javascript">
      document.dispatchEvent(new CustomEvent('vue-app-loading-start', {detail: 'Stream'}));
      fetch('<%=activitiesLoadingURL%>', {
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
<% if (activityId == null) { %>
        if (initialData.activityIds && initialData.activityIds.length) {
          initialData.activityIds.slice(0, <%=limitToDisplay%>).forEach(activity => {
            const preloadLink = document.createElement("link");
            preloadLink.href = `/portal/rest/v1/social/activities/\${activity.id}?expand=identity,likes,shared,commentsPreview,subComments`;
            preloadLink.rel = 'preload';
            preloadLink.as = 'fetch';
            preloadLink.crossOrigin = 'use-credentials';
            document.head.appendChild(preloadLink);
          });
        }
<% } %>
        require(['SHARED/ActivityStream'], app => app.init(initialData, <%=initialLimit%>));
      });
    </script>
  </div>
</div>
