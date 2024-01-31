package com.bots.clownbot.repositories;

import com.bots.clownbot.models.Habits;
import org.springframework.data.repository.CrudRepository;

public interface HabitsRepository extends CrudRepository<Habits,Long> {
    public Iterable<Habits> findByUserId(Long userId);
}
