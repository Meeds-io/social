<%@ page import="org.exoplatform.social.rest.entity.ProfileEntity"%>
<%@ page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
	String profileOwnerId = Utils.getOwnerIdentityId();
	Identity identity = identityManager.getIdentity(profileOwnerId);
	ProfileEntity profileEntity = EntityBuilder.buildEntityProfile(identity.getProfile(),
			"/rest/v1/social/users/" + profileOwnerId, "all");
	String jsonProfileEntity = EntityBuilder.toJsonString(profileEntity);
	int maxUploadSize = identityManager.getImageUploadLimit();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light profileContactInformation"
    id="ProfileContactInformation">
    <textarea id="profileContactInformationDefaultValue"
      class="hidden"><%=jsonProfileEntity == null ? "{}" : jsonProfileEntity%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/ProfileContactInformation'], 
          app => app.init(
              JSON.parse(document.getElementById('profileContactInformationDefaultValue').value), 
              <%=maxUploadSize%>)
      );
    </script>
    <div class="v-application--wrap">
      <header
        class="border-box-sizing v-sheet v-sheet--tile theme--light v-toolbar v-toolbar--flat white"
        style="height: 64px;">
        <div class="v-toolbar__content" style="height: 64px;">
          <div
            class="text-header-title text-sub-title skeleton-text skeleton-text-width skeleton-background skeleton-text-height-thick skeleton-border-radius">
            &nbsp;
          </div>
          <div class="spacer"></div>
          <button type="button" disabled="disabled"
            class="v-btn v-btn--depressed v-btn--disabled v-btn--flat v-btn--icon v-btn--outlined v-btn--round theme--light v-size--small skeleton-background">
            <span class="v-btn__content"></span>
          </button>
        </div>
      </header>
      <div class="px-4 pb-6 white">
        <div class="flex d-flex">
          <div
            class="align-start text-no-wrap font-weight-bold mr-3 skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius">
            &nbsp;</div>
          <div
            class="align-end flex-grow-1 text-truncate text-end skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
            &nbsp;</div>
        </div>
        <hr role="separator" aria-orientation="horizontal"
          class="my-4 v-divider theme--light">
        <div class="flex d-flex">
          <div
            class="align-start text-no-wrap font-weight-bold mr-3 skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius">
            &nbsp;</div>
          <div class="align-end flex-grow-1 text-truncate text-end skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
            &nbsp;</div>
        </div>
        <hr role="separator" aria-orientation="horizontal"
          class="my-4 v-divider theme--light">
        <div class="flex d-flex">
          <div
            class="align-start text-no-wrap font-weight-bold mr-3 skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius">
            &nbsp;</div>
          <div
            class="align-end flex-grow-1 text-truncate text-end skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
            &nbsp;</div>
        </div>
        <hr role="separator" aria-orientation="horizontal"
          class="my-4 v-divider theme--light">
        <div class="flex d-flex">
          <div
            class="align-start text-no-wrap font-weight-bold mr-3 skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius">
            Phones</div>
          <div
            class="align-end flex-grow-1 text-truncate text-end skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
            &nbsp;&nbsp;&nbsp;&nbsp;</div>
        </div>
        <hr role="separator" aria-orientation="horizontal"
          class="my-4 v-divider theme--light">
        <div class="flex d-flex">
          <div
            class="align-start text-no-wrap font-weight-bold mr-3 skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius">
            IMs</div>
          <div
            class="align-end flex-grow-1 text-truncate text-end skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
            &nbsp;&nbsp;&nbsp;&nbsp;</div>
        </div>
        <hr role="separator" aria-orientation="horizontal"
          class="my-4 v-divider theme--light">
        <div class="flex d-flex">
          <div
            class="align-start text-no-wrap font-weight-bold mr-3 skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius">
            URL</div>
          <div
            class="align-end flex-grow-1 text-truncate text-end skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
            &nbsp;&nbsp;&nbsp;&nbsp;</div>
        </div>
      </div>
    </div>
  </div>
</div>
