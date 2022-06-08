package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.LocalDateTime.*;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);


    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            logger.info(update.message().text());
            sendHelloMessage(update);
            findingDataAndMessage(update);
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendHelloMessage(Update update) {
        if (update.message().text().equals("/start")) {
            String message1 = "Hello! This is my first Telegram Bot!";
            String message2 = "You can send yourself a notification in format dd.mm.year hh:mm. For example: 02.06.2022 21.20 Don't forget about homework";
            sendMessage(update, message1);
            sendMessage(update, message2);
        }
    }

    private void sendMessage(Update update, String message) {
        Long chatId = update.message().chat().id();
        SendMessage sendMessage = new SendMessage(chatId, message);
        SendResponse response = telegramBot.execute(sendMessage);
        if (response.isOk()) {
            logger.info("message: {} is sent" + message);
        } else {
            logger.warn("Message was not sent. Error code:  " + response.errorCode());
        }
    }

    private void findingDataAndMessage(Update update) {
        if (update.message().text().equals("/start")) {
            return;
        }
        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(update.message().text());
        String date;
        String item;
        if (matcher.matches()) {
            date = matcher.group(1);
            item = matcher.group(3);
        } else {
            logger.warn("Incorrect message");
            sendMessage(update, "Notification is incorrect");
            return;
        }

        LocalDateTime time = parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        if (time.isBefore(now())) {
            logger.warn("Incorrect date" + date);
            sendMessage(update, "Incorrect date");
            return;
        }

        NotificationTask notificationTask = new NotificationTask(update.message().chat().id(), item, time);
        notificationTaskRepository.save(notificationTask);
        sendMessage(update, "Message is saved and will be sent" + date);
    }

}
