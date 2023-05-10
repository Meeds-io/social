<%@page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.container.PortalContainer" %>
<%
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
  IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
  int maxUploadSize = identityManager.getImageUploadLimit();
  String activityId = rcontext.getRequest().getParameter("id");
  Space space = SpaceUtils.getSpaceByContext();
  long limitToDisplay = 10;
  long initialLimit = limitToDisplay * 2;
  String activitiesLoadingURL;
  String streamType = "&streamType=ALL_STREAM";
  if (activityId != null) {
    activitiesLoadingURL = "/portal/rest/v1/social/activities/" + activityId + "?expand=identity,likes,shared,commentsPreview,subComments,favorite";
  } else if (space == null) {
    activitiesLoadingURL = "/portal/rest/v1/social/activities?limit=" + initialLimit + streamType  + "&expand=ids,identity,likes,shared,commentsPreview,subComments,favorite";
  } else {
    activitiesLoadingURL = "/portal/rest/v1/social/activities?spaceId=" + space.getId() + "&limit=" + initialLimit + "&streamType=ALL_STREAM&expand=ids,identity,likes,shared,commentsPreview,subComments,favorite";
  }
  responseWrapper.addHeader("Link", "<" + activitiesLoadingURL + ">; rel=preload; as=fetch; crossorigin=use-credentials", false);
%>
<div class="VuetifyApp">
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
      require(['SHARED/ActivityStream'], app => app.init(<%=maxUploadSize%>));
    </script>
  </div>
</div>
