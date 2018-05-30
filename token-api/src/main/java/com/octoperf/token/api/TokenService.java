package com.octoperf.token.api;

import java.util.Map;

/**
 * Creates and validates credentials.
 */
public interface TokenService {

  String permanent(Map<String, String> attributes);

  String expiring(Map<String, String> attributes);

  /**
   * Checks the validity of the given credentials.
   *
   * @param token
   * @return attributes if verified
   */
  Map<String, String> untrusted(String token);

  /**
   * Checks the validity of the given credentials.
   *
   * @param token
   * @return attributes if verified
   */
  Map<String, String> verify(String token);
}
