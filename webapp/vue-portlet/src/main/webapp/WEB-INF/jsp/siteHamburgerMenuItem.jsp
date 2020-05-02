<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  Identity identity = Utils.getViewerIdentity();
  String homeLink = "";
  if (identity != null && identity.getProfile().getHomePage() != null) {
    homeLink = identity.getProfile().getHomePage();
  }
  String jsModule = ((String[])request.getAttribute("jsModule"))[0];
%>
<script type="text/javascript">
  eXo.env.portal.homeLink = "<%=homeLink%>";
  require(['<%=jsModule%>']);
</script>
