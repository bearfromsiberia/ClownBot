package com.bots.clownbot.repositories;

import com.bots.clownbot.models.ChatUsers;
import org.springframework.data.repository.CrudRepository;

public interface ChatUsersRepository extends CrudRepository<ChatUsers, Long> {
}
