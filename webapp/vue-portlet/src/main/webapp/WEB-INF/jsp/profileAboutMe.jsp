<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
	String profileOwnerId = Utils.getOwnerIdentityId();
	String aboutMe = null;
	if (profileOwnerId != null) {
		Identity identity = identityManager.getIdentity(profileOwnerId);
		if (identity != null) {
			aboutMe = identity.getProfile().getAboutMe();
		}
	}
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light profileAboutMe"
    id="ProfileAboutMe">
    <textarea id="profileAboutMeDefaultValue" class="hidden"><%=aboutMe == null ? "" : aboutMe%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/ProfileAboutMe'], app => app.init(document.getElementById('profileAboutMeDefaultValue').value));
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
            <span class="v-btn__content"><i class="pb-2"></i></span>
          </button>
        </div>
      </header>
      <div
        class="border-box-sizing v-card v-card--flat v-sheet theme--light pr-7">
        <div
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius skeleton-text-half-width">
          &nbsp;</div>
        <div
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius skeleton-text-full-width">
          &nbsp;</div>
        <div
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius skeleton-text-half-width">
          &nbsp;</div>
        <div
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius skeleton-text-full-width">
          &nbsp;</div>
        <div
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius skeleton-text-half-width">
          &nbsp;</div>
        <div
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius skeleton-text-full-width">
          &nbsp;</div>
        <div
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius skeleton-text-half-width">
          &nbsp;</div>
        <div
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius skeleton-text-full-width">
          &nbsp;</div>
      </div>
    </div>
  </div>
</div>