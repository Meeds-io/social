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
  <div id="ProfileAboutMe">
    <textarea
      id="profileAboutMeDefaultValue"
      class="hidden"><%=aboutMe == null ? "" : aboutMe%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/ProfileAboutMe'], app => app.init(document.getElementById('profileAboutMeDefaultValue').value));
    </script>
  </div>
</div>
