package org.example.subchecker.repository;

import org.example.subchecker.entity.BotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotSessionRepository extends JpaRepository<BotSession,Long> {
}
