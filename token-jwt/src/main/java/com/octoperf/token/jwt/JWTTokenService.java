package com.octoperf.token.jwt;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.octoperf.date.service.DateService;
import com.octoperf.token.api.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.compression.GzipCompressionCodec;
import lombok.experimental.FieldDefaults;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.impl.TextCodec.BASE64;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

@Service
@FieldDefaults(level = PRIVATE, makeFinal = true)
final class JWTTokenService implements Clock, TokenService {
  private static final String DOT = ".";
  private static final GzipCompressionCodec COMPRESSION_CODEC = new GzipCompressionCodec();

  DateService dates;
  String issuer;
  int expirationSec;
  int clockSkewSec;
  String secretKey;

  JWTTokenService(final DateService dates,
                  @Value("${jwt.issuer:octoperf}") final String issuer,
                  @Value("${jwt.expiration-sec:86400}") final int expirationSec,
                  @Value("${jwt.clock-skew-sec:300}") final int clockSkewSec,
                  @Value("${jwt.secret:secret}") final String secret) {
    super();
    this.dates = requireNonNull(dates);
    this.issuer = requireNonNull(issuer);
    this.expirationSec = requireNonNull(expirationSec);
    this.clockSkewSec = requireNonNull(clockSkewSec);
    this.secretKey = BASE64.encode(requireNonNull(secret));
  }

  @Override
  public String permanent(final Map<String, String> attributes) {
    return newToken(attributes, 0);
  }

  @Override
  public String expiring(final Map<String, String> attributes) {
    return newToken(attributes, expirationSec);
  }

  private String newToken(final Map<String, String> attributes, final int expiresInSec) {
    final DateTime now = dates.now();
    final Claims claims = Jwts
      .claims()
      .setIssuer(issuer)
      .setIssuedAt(now.toDate());

    if (expiresInSec > 0) {
      final DateTime expiresAt = now.plusSeconds(expiresInSec);
      claims.setExpiration(expiresAt.toDate());
    }
    claims.putAll(attributes);

    return Jwts
      .builder()
      .setClaims(claims)
      .signWith(HS256, secretKey)
      .compressWith(COMPRESSION_CODEC)
      .compact();
  }

  @Override
  public Map<String, String> verify(final String token) {
    final JwtParser parser = Jwts
      .parser()
      .requireIssuer(issuer)
      .setClock(this)
      .setAllowedClockSkewSeconds(clockSkewSec)
      .setSigningKey(secretKey);
    return parseClaims(() -> parser.parseClaimsJws(token).getBody());
  }

  @Override
  public Map<String, String> untrusted(final String token) {
    final JwtParser parser = Jwts
      .parser()
      .requireIssuer(issuer)
      .setClock(this)
      .setAllowedClockSkewSeconds(clockSkewSec);

    // See: https://github.com/jwtk/jjwt/issues/135
    final String withoutSignature = substringBeforeLast(token, DOT) + DOT;
    return parseClaims(() -> parser.parseClaimsJwt(withoutSignature).getBody());
  }

  private static Map<String, String> parseClaims(final Supplier<Claims> toClaims) {
    try {
      final Claims claims = toClaims.get();
      final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
      for (final Map.Entry<String, Object> e: claims.entrySet()) {
        builder.put(e.getKey(), String.valueOf(e.getValue()));
      }
      return builder.build();
    } catch (final IllegalArgumentException | JwtException e) {
      return ImmutableMap.of();
    }
  }

  @Override
  public Date now() {
    final DateTime now = dates.now();
    return now.toDate();
  }
}