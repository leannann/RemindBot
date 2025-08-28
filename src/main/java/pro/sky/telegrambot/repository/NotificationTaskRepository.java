package pro.sky.telegrambot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findBySentFalseAndScheduledAtLessThanEqual(LocalDateTime now);

    List<NotificationTask> findAllByChatIdOrderByScheduledAtAsc(Long chatId);

    List<NotificationTask> findBySentFalseAndScheduledAt(LocalDateTime scheduledAt);
}