/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.oauth.openid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Set;

import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.SignatureException;

@ExtendWith(MockitoExtension.class)
public class RemoteJwkSigningKeyResolverTest {

  private static final String SIGNATURE_ALGORITHMS_PROPERTY = "exo.oauth.openid.signature.algorithms";

  // unreachable on purpose: getJson() fails to fetch it, so resolveAllowedAlgorithms()
  // always falls back to the exo.oauth.openid.signature.algorithms system property
  private static final String WELL_KNOWN_URL = "https://issuer.example.invalid/.well-known/openid-configuration";

  private static final String CLIENT_SECRET  = "s3cr3t";

  @Mock
  private JwsHeader          header;

  @AfterEach
  public void resetSignatureAlgorithmsProperty() {
    System.clearProperty(SIGNATURE_ALGORITHMS_PROPERTY);
  }

  @Test
  public void testResolveSigningKeyDerivesHmacKeyFromClientSecretForHs256() {
    System.setProperty(SIGNATURE_ALGORITHMS_PROPERTY, "HS256");
    when(header.getAlgorithm()).thenReturn("HS256");

    RemoteJwkSigningKeyResolver resolver = new RemoteJwkSigningKeyResolver(WELL_KNOWN_URL, CLIENT_SECRET);

    Key key = resolver.resolveSigningKey(header, (byte[]) null);

    assertInstanceOf(SecretKeySpec.class, key);
    assertEquals("HmacSHA256", key.getAlgorithm());
    assertArrayEquals(CLIENT_SECRET.getBytes(StandardCharsets.UTF_8), key.getEncoded());
  }

  @Test
  public void testResolveSigningKeyDerivesHmacKeyFromClientSecretForHs512ViaClaimsOverload() {
    System.setProperty(SIGNATURE_ALGORITHMS_PROPERTY, "HS512");
    when(header.getAlgorithm()).thenReturn("HS512");

    RemoteJwkSigningKeyResolver resolver = new RemoteJwkSigningKeyResolver(WELL_KNOWN_URL, CLIENT_SECRET);

    Key key = resolver.resolveSigningKey(header, (Claims) null);

    assertInstanceOf(SecretKeySpec.class, key);
    assertEquals("HmacSHA512", key.getAlgorithm());
  }

  @Test
  public void testResolveSigningKeyRejectsAlgorithmNotAllowedForThisProvider() {
    // no exo.oauth.openid.signature.algorithms set: defaults to RS256 only
    when(header.getAlgorithm()).thenReturn("HS256");

    RemoteJwkSigningKeyResolver resolver = new RemoteJwkSigningKeyResolver(WELL_KNOWN_URL, CLIENT_SECRET);

    assertThrows(SignatureException.class, () -> resolver.resolveSigningKey(header, (byte[]) null));
  }

  @Test
  public void testParseKeysSkipsUnparseableEntryButKeepsTheOthers() throws Exception {
    RemoteJwkSigningKeyResolver resolver = new RemoteJwkSigningKeyResolver(WELL_KNOWN_URL, CLIENT_SECRET);

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPairGenerator.generateKeyPair().getPublic();

    JSONObject goodRsaKey = new JSONObject();
    goodRsaKey.put("kty", "RSA");
    goodRsaKey.put("use", "sig");
    goodRsaKey.put("kid", "good-rsa");
    goodRsaKey.put("n", Encoders.BASE64URL.encode(rsaPublicKey.getModulus().toByteArray()));
    goodRsaKey.put("e", Encoders.BASE64URL.encode(rsaPublicKey.getPublicExponent().toByteArray()));

    // an EC curve outside the P-256/384/521 this resolver supports
    JSONObject badEcKey = new JSONObject();
    badEcKey.put("kty", "EC");
    badEcKey.put("use", "sig");
    badEcKey.put("kid", "bad-ec");
    badEcKey.put("crv", "secp256k1");
    badEcKey.put("x", Encoders.BASE64URL.encode(BigInteger.ONE.toByteArray()));
    badEcKey.put("y", Encoders.BASE64URL.encode(BigInteger.ONE.toByteArray()));

    JSONArray jwks = new JSONArray().put(badEcKey).put(goodRsaKey);

    Map<String, Key> keys = resolver.parseKeys(jwks);

    assertTrue(keys.containsKey("good-rsa"));
    assertFalse(keys.containsKey("bad-ec"));
  }

  @Test
  public void testUpdateKeysDoesNotCacheTheFallbackOnAFailedFetch() {
    // WELL_KNOWN_URL is unreachable by construction: getJson() returns null,
    // so this simulates a transient well-known-document fetch failure
    RemoteJwkSigningKeyResolver resolver = new RemoteJwkSigningKeyResolver(WELL_KNOWN_URL, CLIENT_SECRET);

    Set<String> algorithms = resolver.updateKeys();

    assertEquals(Set.of("RS256"), algorithms);
    assertNull(resolver.getCachedAllowedAlgorithms(),
               "a failed fetch must not permanently pin the provider to the fallback algorithms");
  }
}
