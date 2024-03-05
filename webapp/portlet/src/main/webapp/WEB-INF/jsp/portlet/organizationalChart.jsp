<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects/>

<div class="VuetifyApp">
    <div data-app="true"
         id="organizationalChart"
         class="v-application transparent v-application--is-ltr theme--light">
        <script type="text/javascript">
            require(['PORTLET/social-portlet/OrganizationalChart'], app => app.init());
        </script>
    </div>
</div>
