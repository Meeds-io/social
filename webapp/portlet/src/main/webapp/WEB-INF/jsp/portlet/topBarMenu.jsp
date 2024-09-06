<%@page import="org.exoplatform.portal.webui.util.Util"%>
<%@page import="org.exoplatform.portal.mop.SiteKey"%>
<%
  SiteKey siteKey = Util.getUIPortal().getSiteKey();
  long cacheId = siteKey.hashCode();
%>
<div class="VuetifyApp">
  <div data-app="true" class="v-application v-application--is-ltr theme--light" id="topBarMenu">
    <script type="text/javascript">
      if (!window.topBarMenuLoaded) {
        window.topBarMenuLoaded = true;
        window.topBarMenuHtml = sessionStorage.getItem('topBarMenu-<%=cacheId%>');
        if (window.topBarMenuHtml) {
          document.querySelector('#topBarMenu').innerHTML = window.topBarMenuHtml;
        }
        require(['PORTLET/social-portlet/TopBarMenu'], app => app.init(<%=cacheId%>));
      }
    </script>
  </div>
</div>