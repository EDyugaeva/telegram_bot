package pro.sky.telegrambot.sender;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TelegramBotNotificationSender {

    @Autowired
    private TelegramBot telegramBot;

    private final NotificationTaskRepository notificationTaskRepository;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotNotificationSender.class);

    public TelegramBotNotificationSender(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }


    @Scheduled(cron = "0 0/1 * * * *")
    public void scanBase() {
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> notificationTask = notificationTaskRepository.findNotificationTaskByTime(time);
        for (NotificationTask notification : notificationTask) {
            if (notification != null) {
                sendNotificationMessage(notification);
                logger.info("Notification with id = {} was sent", notification.getId());
            }
        }
    }

    public void sendNotificationMessage(NotificationTask notificationTask) {
        Long chatId = notificationTask.getChatId();
        String message = notificationTask.getTextMessage();
        SendMessage sendMessage = new SendMessage(chatId, message);
        SendResponse response = telegramBot.execute(sendMessage);
        if (response.isOk()) {
            logger.info("Notification: {} was sent" + message);
        }
        else {
            logger.warn("Notification was not sent. Error code: " + response.errorCode());
        }
    }


}
