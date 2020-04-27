<%@page import="org.exoplatform.social.rest.entity.ProfileEntity"%>
<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
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
    class="v-application white v-application--is-ltr theme--light profileWorkExperience"
    id="ProfileWorkExperience">
    <textarea id="profileWorkExperienceDefaultValue" class="hidden"><%=jsonProfileEntity == null ? "{}" : jsonProfileEntity%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/ProfileWorkExperience'], 
          app => app.init(
              JSON.parse(document.getElementById('profileWorkExperienceDefaultValue').value), 
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
            &nbsp;</div>
          <div class="spacer"></div>
          <button type="button" disabled="disabled"
            class="v-btn v-btn--depressed v-btn--disabled v-btn--flat v-btn--icon v-btn--outlined v-btn--round theme--light v-size--small skeleton-background">
            <span class="v-btn__content"></span>
          </button>
        </div>
      </header>
      <div class="px-4 pb-6 white">
        <div
          class="v-timeline workExperienceTimeLine v-timeline--align-top theme--light">
          <div
            class="v-timeline-item workExperienceTimeLineItem v-timeline-item--after theme--light">
            <div class="v-timeline-item__body">
              <div
                class="v-card v-sheet theme--light elevation-0 skeleton-border">
                <div class="v-card__text pb-3">
                  <div
                    class="text-color skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius mb-3">
                    &nbsp;&nbsp;</div>
                  <div
                    class="text-sub-title skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius mb-3">
                    &nbsp;&nbsp;</div>
                </div>
                <div class="v-card__text pt-0">
                  <div
                    class="skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-3">
                    &nbsp;</div>
                  <div
                    class="skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-5">
                    &nbsp;</div>
                  <div
                    class="skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-3">
                    &nbsp;</div>
                </div>
              </div>
            </div>
            <div class="v-timeline-item__divider">
              <div class="v-timeline-item__dot">
                <div
                  class="v-timeline-item__inner-dot skeleton-background"></div>
              </div>
            </div>
            <div class="v-timeline-item__opposite">
              <div
                class="workExperienceTimeLineItemTime skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
                &nbsp;</div>
            </div>
          </div>
          <div
            class="v-timeline-item workExperienceTimeLineItem v-timeline-item--after theme--light">
            <div class="v-timeline-item__body">
              <div
                class="v-card v-sheet theme--light elevation-0 skeleton-border">
                <div class="v-card__text pb-3">
                  <div
                    class="text-color skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius mb-3">
                    &nbsp;&nbsp;</div>
                  <div
                    class="text-sub-title skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius mb-3">
                    &nbsp;&nbsp;</div>
                </div>
                <div class="v-card__text pt-0">
                  <div
                    class="skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-3">
                    &nbsp;</div>
                  <div
                    class="skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-5">
                    &nbsp;</div>
                  <div
                    class="skeleton-text skeleton-text-width-full-width skeleton-background skeleton-text-height skeleton-border-radius mb-3">
                    &nbsp;</div>
                </div>
              </div>
            </div>
            <div class="v-timeline-item__divider">
              <div class="v-timeline-item__dot">
                <div
                  class="v-timeline-item__inner-dot skeleton-background"></div>
              </div>
            </div>
            <div class="v-timeline-item__opposite">
              <div
                class="workExperienceTimeLineItemTime skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
                &nbsp;</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>