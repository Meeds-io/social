<template>
  <v-app>
    <v-main>
      <v-container fluid>
        <div class="loginBGLight">
          <span></span>
        </div>
        <div class="uiLogin">
          <div class="loginContainer">
            <div class="loginHeader introBox">
              <div class="userLoginIcon">
                {{ $t('portal.login.Connectlabel') }}
              </div>
            </div>
            <div class="loginContent">
              <p class="brandingComanyClass mt-5 mb-0">{{ companyName }}</p>
              <div class="titleLogin">
                <div
                  v-if="errorCode"
                  class="signinFail"
                  role="alert">
                  <i class="errorIcon uiIconError"></i>{{ errorMessage }}
                </div>
                <div class="centerLoginContent">
                  <form
                    name="loginForm"
                    action="/portal/login"
                    method="post"
                    style="margin: 0px;"
                    autocomplete="off">
                    <input
                      v-if="initialUri"
                      type="hidden"
                      name="initialURI"
                      :value="initialUri">
                    <div class="userCredentials">
                      <span class="iconUser"></span>
                      <input
                        id="username"
                        :placeholder="$t('portal.login.Username')"
                        name="username"
                        tabindex="1"
                        type="text"
                        aria-required="true">
                    </div>
                    <div class="userCredentials">
                      <span class="iconPswrd"></span>
                      <input
                        id="password"
                        :placeholder="$t('portal.login.Password')"
                        tabindex="2"
                        type="password"
                        name="password"
                        aria-required="true">
                    </div>
                    <div class="rememberContent ms-1">
                      <input
                        v-model="rememberme"
                        id="rememberme"
                        name="rememberme"
                        type="checkbox"
                        class="checkbox mb-2">
                      <label class="uiCheckbox" for="rememberme">
                        {{ $t('portal.login.RememberOnComputer') }}
                      </label>
                    </div>
                    <div id="UIPortalLoginFormAction" class="loginButton">
                      <button
                        :aria-label="$t('portal.login.Signin')"
                        tabindex="4">
                        {{ $t('portal.login.Signin') }}
                      </button>
                    </div>
                    <div class="forgotPasswordClass">
                      <a
                        :href="forgotPasswordPath"
                        :title="$t('gatein.forgotPassword.loginLinkTitle')">
                        {{ $t('portal.login.forgotPassword') }} </a>
                    </div>
                  </form>
                </div>
                <div v-if="oAuthEnabled">
                  <div id="social-pane">
                    <div class="signInDelimiter">
                      {{ $t('UILoginForm.label.Delimiter') }}
                    </div>
                    <div id="social-login">
                      <portal-oauth-login
                        v-for="oAuthProvider in oAuthProviders"
                        :key="oAuthProvider.key"
                        :provider="oAuthProvider"
                        :rememberme="rememberme" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="brandingImageContent">
          <img
            :src="brandingLogo"
            class="brandingImage"
            role="presentation">
        </div>
      </v-container>
    </v-main>
  </v-app>
</template>
<script>
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    rememberme: true,
  }),
  computed: {
    companyName() {
      return this.params && this.params.companyName;
    },
    initialUri() {
      return this.params && this.params.initialUri;
    },
    forgotPasswordPath() {
      return this.params && this.params.forgotPasswordPath;
    },
    brandingLogo() {
      return this.params && this.params.brandingLogo;
    },
    oAuthEnabled() {
      return this.params && this.params.oAuthEnabled;
    },
    oAuthProviderTypes() {
      return this.params && this.params.oAuthProviderTypes || [];
    },
    oAuthProviders() {
      return this.params && this.params.oAuthProviderTypes && this.params.oAuthProviderTypes.map(key => ({
        key,
        url: this.params[`oAuthInitURL-${key}`],
      })) || [];
    },
    errorCode() {
      return this.params && this.params.errorCode;
    },
    errorMessage() {
      return this.errorCode && this.$t(`UILoginForm.label.${this.errorCode}`);
    },
  },
};
</script>