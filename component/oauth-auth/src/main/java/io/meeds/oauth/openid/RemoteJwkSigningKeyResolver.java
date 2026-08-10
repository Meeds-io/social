/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;

public class RemoteJwkSigningKeyResolver implements SigningKeyResolver {

  private final String     wellKnownUrl;

  private final String     clientSecret;

  private final Object     lock   = new Object();

  private volatile Map<String, Key> keyMap = new HashMap<>();

  // null until the well-known document (or the fallback) has been resolved
  private volatile Set<String> allowedAlgorithms;

  private static final Log LOG    = ExoLogger.getLogger(RemoteJwkSigningKeyResolver.class);

  RemoteJwkSigningKeyResolver(String wellKnownUrl, String clientSecret) {
    this.wellKnownUrl = wellKnownUrl;
    this.clientSecret = clientSecret;
  }

  @Override
  public Key resolveSigningKey(JwsHeader header, Claims claims) {
    return resolveSigningKey(header);
  }

  @Override
  public Key resolveSigningKey(JwsHeader header, byte[] plaintext) {
    return resolveSigningKey(header);
  }

  private Key resolveSigningKey(JwsHeader header) {
    String algorithm = header.getAlgorithm();
    if (algorithm == null || !getAllowedAlgorithms().contains(algorithm)) {
      throw new SignatureException("OpenId token signature algorithm '" + algorithm
          + "' is not among the algorithms this provider advertises (or is configured to accept)");
    }
    // HMAC-signed tokens use the client_secret itself as the shared key,
    // per the OIDC spec — it is never published in the JWKS
    if (algorithm.startsWith("HS")) {
      return new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "Hmac" + algorithm.replace("HS", "SHA"));
    }
    return getKey(header.getKeyId());
  }

  // test-only: expose the cached state (null means "not cached")
  Set<String> getCachedAllowedAlgorithms() {
    return allowedAlgorithms;
  }

  private Set<String> getAllowedAlgorithms() {
    // check non synchronized to avoid a lock
    Set<String> result = allowedAlgorithms;
    if (result != null) {
      return result;
    }

    synchronized (lock) {
      // once synchronized, check once again: a previously synchronized
      // thread could have already resolved the allowed algorithms
      result = allowedAlgorithms;
      if (result != null) {
        return result;
      }
      return updateKeys();
    }
  }

  private Key getKey(String keyId) {
    // check non synchronized to avoid a lock
    Key result = keyMap.get(keyId);
    if (result != null) {
      return result;
    }

    synchronized (lock) {
      // once synchronized, check the map once again the a previously
      // synchronized thread could have already updated they keys
      result = keyMap.get(keyId);
      if (result != null) {
        return result;
      }
      // finally, fallback to updating the keys, an return a value (or null)
      updateKeys();
      return keyMap.get(keyId);
    }
  }

  Set<String> updateKeys() {

    JSONObject configuration = getJson(wellKnownUrl);
    Set<String> algorithms = resolveAllowedAlgorithms(configuration);
    // only cache when the well-known document was actually fetched: a
    // transient failure (network, timeout) must not permanently pin the
    // provider to the fallback algorithms for the rest of the JVM's life
    if (configuration != null) {
      allowedAlgorithms = algorithms;
    }
    try {
      String jwksUrl = configuration != null ? configuration.getString("jwks_uri") : null;
      JSONObject keys = getJson(jwksUrl);
      JSONArray arraylist = keys != null ? keys.getJSONArray("keys") : null;
      if (arraylist != null) {
        keyMap = Collections.unmodifiableMap(parseKeys(arraylist));
      }
    } catch (JSONException e) {
      LOG.error("can't get keys in JSONObject");
    }
    return algorithms;
  }

  // A JWKS is a set of independent keys: one entry this RP cannot parse
  // (an unknown "kty", a malformed field, an EC curve outside P-256/384/521)
  // must not prevent every other — otherwise valid — key from being usable,
  // or every login through this provider breaks until the document changes.
  Map<String, Key> parseKeys(JSONArray arraylist) {
    Map<String, Key> newKeys = new HashMap<>();
    for (int i = 0; i < arraylist.length(); i++) {
      try {
        JSONObject jsonobjects = arraylist.getJSONObject(i);
        if (!"sig".equals(jsonobjects.get("use"))) {
          continue;
        }
        if ("RSA".equals(jsonobjects.get("kty"))) {
          BigInteger modulus = base64ToBigInteger(jsonobjects.getString("n"));
          BigInteger exponent = base64ToBigInteger(jsonobjects.getString("e"));
          RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
          PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);
          newKeys.put(jsonobjects.getString("kid"), publicKey);
        } else if ("EC".equals(jsonobjects.get("kty"))) {
          PublicKey publicKey = parseEcPublicKey(jsonobjects);
          newKeys.put(jsonobjects.getString("kid"), publicKey);
        }
      } catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidParameterSpecException
               | IllegalArgumentException e) {
        LOG.error("Skipping unparseable JWKS entry at index {}", i, e);
      }
    }
    return newKeys;
  }

  // The well-known document's own list of supported signing algorithms
  // takes precedence; the exo.oauth.openid.signature.algorithms system
  // property is used only when that field is absent or unparseable, so a
  // token can never claim an algorithm (e.g. HS256, keyed on the client
  // secret) this provider was never configured — nor observed — to
  // actually use.
  private Set<String> resolveAllowedAlgorithms(JSONObject configuration) {
    if (configuration != null && configuration.has("id_token_signing_alg_values_supported")) {
      try {
        return toStringSet(configuration.getJSONArray("id_token_signing_alg_values_supported"));
      } catch (JSONException e) {
        LOG.error("can't parse id_token_signing_alg_values_supported, falling back to the configured algorithms");
      }
    }
    return toStringSet(System.getProperty("exo.oauth.openid.signature.algorithms", "RS256"));
  }

  private static Set<String> toStringSet(JSONArray array) throws JSONException {
    Set<String> result = new HashSet<>();
    for (int i = 0; i < array.length(); i++) {
      result.add(array.getString(i));
    }
    return Collections.unmodifiableSet(result);
  }

  private static Set<String> toStringSet(String commaSeparated) {
    Set<String> result = new HashSet<>();
    for (String value : commaSeparated.split(",")) {
      String trimmed = value.trim();
      if (!trimmed.isEmpty()) {
        result.add(trimmed);
      }
    }
    return Collections.unmodifiableSet(result);
  }

  private PublicKey parseEcPublicKey(JSONObject jsonobjects) throws NoSuchAlgorithmException, InvalidKeySpecException,
                                                              InvalidParameterSpecException {
    BigInteger x = base64ToBigInteger(jsonobjects.getString("x"));
    BigInteger y = base64ToBigInteger(jsonobjects.getString("y"));
    AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("EC");
    algorithmParameters.init(new ECGenParameterSpec(mapCurveName(jsonobjects.getString("crv"))));
    ECParameterSpec ecParameterSpec = algorithmParameters.getParameterSpec(ECParameterSpec.class);
    ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(new ECPoint(x, y), ecParameterSpec);
    return KeyFactory.getInstance("EC").generatePublic(ecPublicKeySpec);
  }

  private String mapCurveName(String jwkCurveName) {
    return switch (jwkCurveName) {
    case "P-256" -> "secp256r1";
    case "P-384" -> "secp384r1";
    case "P-521" -> "secp521r1";
    default -> throw new IllegalArgumentException("Unsupported EC curve: " + jwkCurveName);
    };
  }

  public static Map<String, Object> toMap(JSONObject jsonobj) throws JSONException {
    Map<String, Object> map = new HashMap<>();
    Iterator<String> keys = jsonobj.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      Object value = jsonobj.get(key);
      if (value instanceof JSONArray) {
        value = toList((JSONArray) value);
      } else if (value instanceof JSONObject) {
        value = toMap((JSONObject) value);
      }
      map.put(key, value);
    }
    return map;
  }

  public static List<Object> toList(JSONArray array) throws JSONException {
    List<Object> list = new ArrayList<>();
    for (int i = 0; i < array.length(); i++) {
      Object value = array.get(i);
      if (value instanceof JSONArray) {
        value = toList((JSONArray) value);
      } else if (value instanceof JSONObject) {
        value = toMap((JSONObject) value);
      }
      list.add(value);
    }
    return list;
  }

  private JSONObject getJson(String url) {
    try (InputStream input = new URL(url).openStream()) {
      // Input Stream Object To Start Streaming.
      BufferedReader re = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
      // Buffer Reading In UTF-8
      String text = read(re); // Handy Method To Read Data From BufferReader
      return new JSONObject(text); // Returning JSON
    } catch (Exception e) {
      LOG.error("Error when Read Data From BufferReader" + e.getMessage());
      return null;
    }
  }

  public String read(Reader re) throws IOException { // class Declaration
    StringBuilder str = new StringBuilder(); // To Store Url Data In String.
    int temp;
    do {
      temp = re.read(); // reading Charcter By Chracter.
      str.append((char) temp);

    } while (temp != -1);
    // re.read() return -1 when there is end of buffer , data or end of file.

    return str.toString();

  }

  private BigInteger base64ToBigInteger(String value) {
    return new BigInteger(1, Decoders.BASE64URL.decode(value));
  }
}
