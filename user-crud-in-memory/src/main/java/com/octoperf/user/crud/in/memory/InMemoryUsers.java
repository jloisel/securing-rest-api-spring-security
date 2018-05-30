package com.octoperf.user.crud.in.memory;

import com.octoperf.user.crud.api.UserCrudService;
import com.octoperf.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
final class InMemoryUsers implements UserCrudService {

  Map<String, User> users = new HashMap<>();

  @Override
  public User save(final User user) {
    return users.put(user.getId(), user);
  }

  @Override
  public Optional<User> find(final String id) {
    return ofNullable(users.get(id));
  }

  @Override
  public Optional<User> findByUsername(final String username) {
    return users
      .values()
      .stream()
      .filter(u -> Objects.equals(username, u.getUsername()))
      .findFirst();
  }
}
