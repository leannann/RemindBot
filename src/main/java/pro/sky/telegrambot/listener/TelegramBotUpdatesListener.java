package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    private static final Pattern NEW_TASK = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            Message msg = update.message();
            if (msg == null || msg.text() == null) {
                return;
            }

            String text = msg.text().trim();
            long chatId = msg.chat().id();

            if ("/start".equalsIgnoreCase(text)) {
                telegramBot.execute(new SendMessage(
                        chatId,
                        "Привет! Я бот-напоминалка.\n" +
                                "Формат: DD.MM.YYYY HH:mm Текст задачи\n" +
                                "Пример: 28.08.2025 19:30 Проверить лабораторную"
                ));
                return;
            }

            Matcher m = NEW_TASK.matcher(text);
            if (m.matches()) {
                String dtStr = m.group(1);
                String taskText = m.group(3);

                try {
                    LocalDateTime when = LocalDateTime.parse(dtStr, DTF);

                    NotificationTask task = new NotificationTask();
                    task.setChatId(chatId);
                    task.setMessage(taskText);
                    task.setScheduledAt(when);
                    task.setSent(false);
                    task.setCreatedAt(LocalDateTime.now());

                    notificationTaskRepository.save(task);

                    telegramBot.execute(new SendMessage(
                            chatId,
                            "Ок! Сохранил напоминание на " + when.format(DTF) + " — «" + taskText + "»."
                    ));
                    return;
                } catch (DateTimeParseException e) {
                    logger.debug("Не смог распарсить дату: {}", dtStr, e);
                } catch (Exception e) {
                    logger.error("Ошибка сохранения задачи", e);
                    telegramBot.execute(new SendMessage(chatId, "Не получилось сохранить задачу, попробуйте ещё раз."));
                    return;
                }
            }

            telegramBot.execute(new SendMessage(
                    chatId,
                    "Не распознал формат. Используйте:\nDD.MM.YYYY HH:mm Текст задачи"
            ));
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
