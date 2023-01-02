/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
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
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.io.Decoders;

public class RemoteJwkSigningKeyResolver implements SigningKeyResolver {

  private final String     wellKnownUrl;

  private final Object     lock   = new Object();

  private Map<String, Key> keyMap = new HashMap<>();

  private static final Log LOG    = ExoLogger.getLogger(RemoteJwkSigningKeyResolver.class);

  RemoteJwkSigningKeyResolver(String wellKnownUrl) {
    this.wellKnownUrl = wellKnownUrl;
  }

  @Override
  public Key resolveSigningKey(JwsHeader header, Claims claims) {
    return getKey(header.getKeyId());
  }

  @Override
  public Key resolveSigningKey(JwsHeader header, String plaintext) {
    return getKey(header.getKeyId());
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

  private void updateKeys() {

    JSONObject configuration = getJson(wellKnownUrl);
    try {
      String jwksUrl = configuration != null ? configuration.getString("jwks_uri") : null;
      JSONObject keys = getJson(jwksUrl);
      JSONArray arraylist = keys != null ? keys.getJSONArray("keys") : null;
      Map<String, Key> newKeys = new HashMap<>();
      if (arraylist != null) {
        for (int i = 0; i < arraylist.length(); i++) {
          JSONObject jsonobjects = arraylist.getJSONObject(i);
          if (("sig".equals(jsonobjects.get("use"))) && ("RSA".equals(jsonobjects.get("kty")))) {
            BigInteger modulus = base64ToBigInteger(jsonobjects.getString("n"));
            BigInteger exponent = base64ToBigInteger(jsonobjects.getString("e"));
            RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
            PublicKey publicKey = null;
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(rsaPublicKeySpec);
            newKeys.put(jsonobjects.getString("kid"), publicKey);
          }
        }
        keyMap = Collections.unmodifiableMap(newKeys);
      }
    } catch (JSONException e) {
      LOG.error("can't get keys in JSONObject");
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Failed to parse public key");
    }
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
