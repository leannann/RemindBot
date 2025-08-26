package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationTaskRepository repo;
    private final TelegramBot bot;

    public NotificationScheduler(NotificationTaskRepository repo, TelegramBot bot) {
        this.repo = repo;
        this.bot = bot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void tick() {
        LocalDateTime nowMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> due = repo.findBySentFalseAndScheduledAt(nowMinute);
        if (due.isEmpty()) return;

        for (NotificationTask t : due) {
            try {
                bot.execute(new SendMessage(t.getChatId(), t.getMessage()));
                t.setSent(true);
                t.setUpdatedAt(LocalDateTime.now());
                repo.save(t);
                log.info("sent task id={} chat={} at {}", t.getId(), t.getChatId(), nowMinute);
            } catch (Exception e) {
                log.error("failed to send task id={}", t.getId(), e);
            }
        }
    }
}