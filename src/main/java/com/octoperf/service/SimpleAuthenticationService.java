package com.octoperf.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
final class SimpleAuthenticationService implements UserAuthenticationService {
  Map<String, User> users = new HashMap<>();

  @Override
  public Optional<String> login(final String username, final String password) {
    final String token = UUID.randomUUID().toString();
    final User user = User
      .builder()
      .id(token)
      .username(username)
      .password(password)
      .build();

    users.put(token, user);
    return Optional.of(token);
  }

  @Override
  public Optional<User> findByToken(final String token) {
    return Optional.ofNullable(users.get(token));
  }

  @Override
  public void logout(final User user) {
    users.remove(user.getId());
  }
}
