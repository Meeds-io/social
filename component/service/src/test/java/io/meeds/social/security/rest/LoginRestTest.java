/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.security.rest;

import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;
import jakarta.servlet.Filter;
import lombok.SneakyThrows;
import nl.captcha.Captcha;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserDAOImpl;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.web.login.recovery.PasswordRecoveryService;
import org.exoplatform.web.register.RegisterUIParamsExtension;
import org.exoplatform.web.security.security.RemindPasswordTokenService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = { LoginRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class LoginRestTest {

  private MockMvc mockMvc;

  @Autowired
  private SecurityFilterChain filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockBean
  private PasswordRecoveryService passwordRecoveryService;

  @MockBean
  private OrganizationService organizationService;

  @MockBean
  private UserDAOImpl userHandler;

  @MockBean
  private RegisterUIParamsExtension registerUIParamsExtension;

  @MockBean
  private SecuritySettingService securitySettingService;

  @MockBean
  private RemindPasswordTokenService remindPasswordTokenService;

  @MockBean
  private Captcha captcha;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();


  }

  @Test
  @SneakyThrows
  public void testVerifyWorkingToken() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(get("/login/verify?token=123456789"));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("username", "john").toString()));
  }

  @Test
  @SneakyThrows
  public void testVerifyNotWorkingToken() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);
    ResultActions response = mockMvc.perform(get("/login/verifyToken?token=123456789"));
    response.andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  public void testRequestResetPasswordSuccess() {

    UserImpl user = new UserImpl("john");
    user.setUserName("john");
    user.setEnabled(true);
    when(userHandler.findUserByName(any(),any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.sendRecoverPasswordEmail(any(),any(),any())).thenReturn(true);

    ResultActions response = mockMvc.perform(post("/login/requestResetPassword")
                                                 .content("username=john")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "gatein.forgotPassword.emailSendSuccessful").toString()));

  }

  @Test
  @SneakyThrows
  public void testRequestResetPasswordEmptyUsername() {
    ResultActions response = mockMvc.perform(post("/login/requestResetPassword")
                                                 .content("username=")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject()
                                            .put("error", "gatein.forgotPassword.emptyUserOrEmail").toString()));

  }

  @Test
  @SneakyThrows
  public void testRequestResetPasswordProblemSendingEmail() {

    UserImpl user = new UserImpl("john");
    user.setUserName("john");
    user.setEnabled(true);
    when(userHandler.findUserByName(any(),any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.sendRecoverPasswordEmail(any(),any(),any())).thenReturn(false);

    ResultActions response = mockMvc.perform(post("/login/requestResetPassword")
                                                 .content("username=john")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.emailSendFailure").toString()));

  }

  @Test
  @SneakyThrows
  public void testResetPasswordTokenNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=test&password2=test&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  public void testResetPasswordUsernameDifferent() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("mary");
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=test&password2=test&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
        .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.usernameChanged").toString()));
  }

  @Test
  @SneakyThrows
  public void testResetPasswordPasswordDifferent() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=test1&password2=test2&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.confirmPasswordNotMatch").toString()));
  }

  @Test
  @SneakyThrows
  public void testResetPasswordNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=test&password2=test&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  public void testResetPasswordErrorOnChange() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.changePass(any(),any(),any(),any())).thenReturn(false);
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=Test1234@&password2=Test1234@&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.resetPasswordFailure").toString()));
  }

  @Test
  @SneakyThrows
  public void testResetPasswordSuccess() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.changePass(any(),any(),any(),any())).thenReturn(true);
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=Test1234@&password2=Test1234@&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal/login"));
  }

  @Test
  @SneakyThrows
  public void testSetPasswordTokenNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .content("username=john&password=test&password2=test&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  public void testSetPasswordCaptchNotValid() {
    when(captcha.isCorrect(any())).thenReturn(false);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=test&password2=test&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.captchaError").toString()));  }

  @Test
  @SneakyThrows
  public void testSetPasswordUsernameDifferent() {
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);

    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("mary");
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=test&password2=test&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.usernameChanged").toString()));
  }

  @Test
  @SneakyThrows
  public void testSetPasswordPasswordDifferent() {
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=test1&password2=test2&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.confirmPasswordNotMatch").toString()));
  }

  @Test
  @SneakyThrows
  public void testSetPasswordNotValid() {
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=test&password2=test&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  public void testSetPasswordErrorOnChange() {
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.changePass(any(),any(),any(),any())).thenReturn(false);
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=Test1234@&password2=Test1234@&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.resetPasswordFailure").toString()));
  }

  @Test
  @SneakyThrows
  public void testSetPasswordSuccess() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.changePass(any(),any(),any(),any())).thenReturn(true);

    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);

    UserImpl user = new UserImpl("john");
    user.setUserName("john");
    user.setEnabled(true);
    user.setEmail("jsmith@acme.com");
    when(userHandler.findUserByName(any(),any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);

    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=Test1234@&password2=Test1234@&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal/login"));
  }


  @Test
  @SneakyThrows
  public void testServeCaptcha() {
    ResultActions response = mockMvc.perform(get("/login/captcha?name=register"));
    response.andExpect(status().isOk())
            .andExpect(header().string("Content-Type","image/png"));
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterAlreadyConnected() {
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .with(testSimpleUser()));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal"));
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterWithRegisterDisabled() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(false);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterWithNotValidCaptcha() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(false);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.captchaError").toString()));
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterWithNotValidEmail() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterWithExistingEmail() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(true);

    ListAccess<User> users = new ListAccess<>() {
      @Override
      public User[] load(int i, int i1) throws Exception, IllegalArgumentException {
        return null;
      }

      @Override
      public int getSize() throws Exception {
        return 1;
      }
    };

    when(userHandler.findUsersByQuery(any(),any())).thenReturn(users);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "onboardingEmailSent").toString()));

  }

  @Test
  @SneakyThrows
  public void testRequestRegisterSuccessSendOnboardingEmail() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(passwordRecoveryService.sendExternalRegisterEmail(eq(null),any(),any(),eq(null),any(),anyBoolean())).thenReturn("ok");
    when(captcha.isCorrect(any())).thenReturn(true);

    ListAccess<User> users = new ListAccess<>() {
      @Override
      public User[] load(int i, int i1) throws Exception, IllegalArgumentException {
        return null;
      }

      @Override
      public int getSize() throws Exception {
        return 0;
      }
    };

    when(userHandler.findUsersByQuery(any(),any())).thenReturn(users);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "onboardingEmailSent").toString()));

  }

  @Test
  @SneakyThrows
  public void testRequestRegisterFailedSendOnboardingEmail() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(passwordRecoveryService.sendExternalRegisterEmail(eq(null),any(),any(),eq(null),any(),anyBoolean())).thenThrow(new Exception("Error"));
    when(captcha.isCorrect(any())).thenReturn(true);

    ListAccess<User> users = new ListAccess<>() {
      @Override
      public User[] load(int i, int i1) throws Exception, IllegalArgumentException {
        return null;
      }

      @Override
      public int getSize() throws Exception {
        return 0;
      }
    };

    when(userHandler.findUsersByQuery(any(),any())).thenReturn(users);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.emailSendFailure").toString()));

  }

  @Test
  @SneakyThrows
  public void testFinishRegistrationTokenNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);
    ResultActions response = mockMvc.perform(post("/login/finishRegistration")
                                                 .content("token=123456798&tokenType=email-validation")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  public void testFinishRegistrationTokenSuccess() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john::John::Smith::jsmith@acme.com::Password1234");

    when(securitySettingService.isRegistrationExternalUser()).thenReturn(false);

    User user = new UserImpl();
    user.setUserName("john");
    when(userHandler.createUserInstance(any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    ResultActions response = mockMvc.perform(post("/login/finishRegistration")
                                                 .content("token=123456798&tokenType=email-validation")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal/login"));
  }

  @Test
  @SneakyThrows
  public void testRegisterAlreadyConnected() {
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .with(testSimpleUser()));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal"));
  }

  @Test
  @SneakyThrows
  public void testRegisterTokenNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);

    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());

  }

  @Test
  @SneakyThrows
  public void testRegisterExistingUser() {
    User user = new UserImpl();
    user.setUserName("john");

    when(userHandler.findUserByName(any(), any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .with(testSimpleUser()));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal"));
  }

  @Test
  @SneakyThrows
  public void testRegisterExistingNotValidCaptcha() {
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("jsmith@acme.com");


    when(captcha.isCorrect(any())).thenReturn(false);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.captchaError").toString()));

  }

  @Test
  @SneakyThrows
  public void testRegisterNotValidPassword() {
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("jsmith@acme.com");


    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=&password=pass&password2=pass&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

  }

  @Test
  @SneakyThrows
  public void testRegisterNotFirstName() {
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("jsmith@acme.com");


    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=123John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

  }

  @Test
  @SneakyThrows
  public void testRegisterSuccessAccountVerificationEmail() {
    User john = new UserImpl();
    john.setUserName("john");
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(userHandler.createUserInstance(any())).thenReturn(john);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.sendAccountVerificationEmail(any(),any(),any(),any(),any(),any(),any())).thenReturn(true);

    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/verifyEmail")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=john&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "emailVerificationSent").toString()));
  }


  @Test
  @SneakyThrows
  public void testRegisterSuccess() {
    User john = new UserImpl();
    john.setUserName("john");
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(userHandler.createUserInstance(any())).thenReturn(john);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("jsmith@acme.com");
    when(passwordRecoveryService.sendAccountCreatedConfirmationEmail(any(),any(),any())).thenReturn(true);

    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal/login"));
  }

  private RequestPostProcessor testSimpleUser() {
    return user("john").password("password")
                            .authorities(new SimpleGrantedAuthority("users"));
  }



}
