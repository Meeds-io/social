export function verifyToken(token, tokenType) {
  const formData = new FormData();
  formData.append('token', token);
  if (tokenType) {
    formData.append('tokenType', tokenType);
  }
  const params = new URLSearchParams(formData).toString();
  return fetch(`/social/${eXo.env.portal.rest}/login/verify?${params}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    return resp;
  });
}

export function finishRegistration(token, tokenType) {
  const formData = new FormData();
  formData.append('token', token);
  if (tokenType) {
    formData.append('tokenType', tokenType);
  }
  const params = new URLSearchParams(formData).toString();
  return fetch(`/social/${eXo.env.portal.rest}/login/finishRegistration?${params}`, {
    method: 'POST',
    credentials: 'include',
  }).then(resp => {
    return resp;
  });
}

export function register(email, firstName, lastName, password, confirmPassword, token, captcha) {
  const formData = new FormData();
  formData.append('email', email);
  formData.append('firstName', firstName);
  formData.append('lastName', lastName);
  formData.append('password', password);
  formData.append('password2', confirmPassword);
  formData.append('token', token);
  formData.append('captcha', captcha);
  const body = new URLSearchParams(formData).toString();
  return fetch(`/social/${eXo.env.portal.rest}/login/register`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body,
  }).then(resp => {
    return resp;
  });
}

export function sendEmailVerificationEmail(email, firstName, lastName, password, confirmPassword, token, captcha, username) {
  const formData = new FormData();
  formData.append('email', email);
  formData.append('firstName', firstName);
  formData.append('lastName', lastName);
  formData.append('password', password);
  formData.append('password2', confirmPassword);
  formData.append('username', username);
  formData.append('token', token);
  formData.append('captcha', captcha);
  const body = new URLSearchParams(formData).toString();
  return fetch(`/social/${eXo.env.portal.rest}/login/verifyEmail`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body,
  }).then(resp => {
    return resp;
  });
}

export function requestRegister(email, captcha, initialUri) {
  const formData = new FormData();
  formData.append('email', email);
  formData.append('captcha', captcha);
  if (initialUri) {
    formData.append('initialURI', initialUri);
  }
  const body = new URLSearchParams(formData).toString();
  return fetch(`/social/${eXo.env.portal.rest}/login/requestRegister`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body,
  }).then(resp => {
    return resp;
  });
}

export function requestResetPassword(username) {
  const formData = new FormData();
  formData.append('username', username);
  const body = new URLSearchParams(formData).toString();
  return fetch(`/social/${eXo.env.portal.rest}/login/requestResetPassword`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body,
  }).then(resp => {
    return resp;
  });
}

export function resetPassword(username, password, confirmPassword, token) {
  const formData = new FormData();
  formData.append('username', username);
  formData.append('password', password);
  formData.append('password2', confirmPassword);
  formData.append('token', token);
  const body = new URLSearchParams(formData).toString();
  return fetch(`/social/${eXo.env.portal.rest}/login/resetPassword`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body,
  }).then(resp => {
    return resp;
  });
}

export function setPassword(username, password, confirmPassword, token, captcha) {
  const formData = new FormData();
  formData.append('username', username);
  formData.append('password', password);
  formData.append('password2', confirmPassword);
  formData.append('token', token);
  formData.append('captcha', captcha);
  const body = new URLSearchParams(formData).toString();
  return fetch(`/social/${eXo.env.portal.rest}/login/setPassword`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body,
  }).then(resp => {
    return resp;
  });
}

