import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyBot extends TelegramLongPollingBot {

    // Вставь сюда свой ID (цифрами), который получил от @userinfobot
    private final String ADMIN_ID = ""; //Сюда ваш telegram id(аккаунта с которого будете отвечать на сообщения) можно посмотреть в @userinfobot

    @Override
    public String getBotUsername() {
        return ""; //В кавычках название бота с @BotFather
    }

    @Override
    public String getBotToken() {
        return ""; //В кавычках токен бота с @BotFather
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            //Если пишет админ и хочет ответить пользователю
            if (String.valueOf(chatId).equals(ADMIN_ID) && messageText.startsWith("/reply")) {
                handleAdminReply(messageText);
                return; //Выходим, чтобы бот не записывал твой ответ сам себе в логи
            }

            //Если пишет ОБЫЧНЫЙ ЮЗЕР
            saveMessageToFile(userFirstName, messageText);

            if (messageText.equals("/start")) {
                sendResponse(chatId, "Привет! Жду твоего обращения.");
            } else {
                //Уведомляем админа о новом сообщении
                sendResponse(Long.parseLong(ADMIN_ID), "Новое сообщение!\nОт: " + userFirstName +
                        "\nID: " + chatId + "\nТекст: " + messageText +
                        "\n\nЧтобы ответить, введи:\n/reply " + chatId + " [твой текст]");

                //Отвечаем юзеру
                sendResponse(chatId, "Спасибо за обращение! Ждите ответ.");
            }
        }
    }

    //Метод для обработки твоего ответа через команду /reply
    private void handleAdminReply(String adminText) {
        try {
            //Разбиваем строку "/reply 1234567 Текст" по пробелам
            String[] parts = adminText.split(" ", 3);
            if (parts.length < 3) {
                sendResponse(Long.parseLong(ADMIN_ID), "Ошибка! Пиши так: /reply [ID] [Текст]");
                return;
            }

            String targetChatId = parts[1];
            String replyMessage = parts[2];

            sendResponse(Long.parseLong(targetChatId), "Ответ от поддержки: " + replyMessage);
            sendResponse(Long.parseLong(ADMIN_ID), "✅ Ответ отправлен пользователю " + targetChatId);

        } catch (Exception e) {
            sendResponse(Long.parseLong(ADMIN_ID), "Ошибка при разборе команды!");
        }
    }

    private void sendResponse(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void saveMessageToFile(String name, String text) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (FileWriter fw = new FileWriter("log.txt", true);
             PrintWriter out = new PrintWriter(fw)) {
            out.println("[" + timestamp + "] " + name + ": " + text);
        } catch (IOException e) {
            System.out.println("Ошибка записи: " + e.getMessage());
        }
    }
}