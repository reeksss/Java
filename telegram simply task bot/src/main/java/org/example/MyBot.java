package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MyBot extends TelegramLongPollingBot {
    public MyBot() {
        loadTasks();
    }

    private final List<Task> tasks = new ArrayList<>();

    @Override
    public String getBotUsername() {
        return "ВАШЕ_НАЗВАНИЕ"; //из @BotFather
    }

    @Override
    public String getBotToken() {
        return "ВАШ_ТОКЕН"; //из @BotFather
    }

    public static class Task {
        String text;
        boolean completed;
        String data;
        long ownerId; // Добавили поле владельца

        public Task(String text, long ownerId) {
            this.text = text;
            this.ownerId = ownerId;
            this.completed = false;
            this.data = "Время не установлено";
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/start")) {
                sendResponse(chatId, "Добро пожаловать\n /help - список команд");
            }

            else if (messageText.startsWith("/settime")) {
                try {
                    String content = messageText.substring(8).trim();
                    String[] parts = content.split(" ", 2);
                    int userNum = Integer.parseInt(parts[0]);
                    String time = parts[1];

                    Task target = findUserTask(chatId, userNum);
                    if (target != null) {
                        target.data = time;
                        saveTasks();
                        sendResponse(chatId, "Для задачи установлено время: " + time);
                    } else {
                        sendResponse(chatId, "Задача не найдена!");
                    }
                } catch (Exception e) {
                    sendResponse(chatId, "Ошибка! Формат: /settime [номер] [время]");
                }
            }

            else if (messageText.startsWith("/clear")) {
                // Удаляем только задачи этого пользователя
                tasks.removeIf(t -> t.ownerId == chatId);
                saveTasks();
                sendResponse(chatId, "Твои задачи очищены!");
            }

            else if (messageText.startsWith("/help")) {
                sendResponse(chatId, "Команды:\n/add [текст] - добавить задачу\n/list - список задач\n/done [номер] - отметить задачу выполненной\n/delete [номер] - удалить задачу\n/settime [номер] [время] - установить время на задачу");
            }

            else if (messageText.startsWith("/done")) {
                try {
                    int userNum = Integer.parseInt(messageText.substring(5).trim());
                    Task target = findUserTask(chatId, userNum);
                    if (target != null) {
                        target.completed = true;
                        saveTasks();
                        sendResponse(chatId, "Готово: " + target.text);
                    } else {
                        sendResponse(chatId, "Задача №" + userNum + " не найдена.");
                    }
                } catch (Exception e) {
                    sendResponse(chatId, "Введи номер цифрой.");
                }
            }

            else if (messageText.startsWith("/delete")) {
                try {
                    int userNum = Integer.parseInt(messageText.substring(7).trim());
                    Task target = findUserTask(chatId, userNum);if (target != null) {
                        tasks.remove(target);
                        saveTasks();
                        sendResponse(chatId, "Удалено: " + target.text);
                    } else {
                        sendResponse(chatId, "Задача не найдена.");
                    }
                } catch (Exception e) {
                    sendResponse(chatId, "Ошибка при удалении.");
                }
            }

            else if (messageText.startsWith("/add ")) {
                String taskDescription = messageText.substring(5);
                tasks.add(new Task(taskDescription, chatId));
                saveTasks();
                sendResponse(chatId, "✅ Добавлено!");
            }

            else if (messageText.equals("/list")) {
                StringBuilder listResponse = new StringBuilder("Твои задачи:\n");
                int count = 0;
                for (Task t : tasks) {
                    if (t.ownerId == chatId) {
                        count++;
                        String status;
                        if (t.completed) {
                            status = "✅";
                        } else {
                            status = "❌";
                        }
                        listResponse.append(count).append(". ").append(status).append(" ")
                                .append(t.text).append(" | ").append(t.data).append("\n");
                    }
                }
                if (count == 0) sendResponse(chatId, "У тебя нет задач. Используй /add");
                else sendResponse(chatId, listResponse.toString());
            }
        }
    }

    private Task findUserTask(long chatId, int userNum) {
        int current = 0;
        for (Task t : tasks) {
            if (t.ownerId == chatId) {
                current++;
                if (current == userNum) return t;
            }
        }
        return null;
    }

    private void sendResponse(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
    }

    private void saveTasks() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream("tasks.txt"), StandardCharsets.UTF_8))) {
            for (Task t : tasks) {
                writer.println(t.ownerId + ";" + t.text + ";" + t.completed + ";" + t.data);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadTasks() {
        File file = new File("tasks.txt");
        if (!file.exists()) return;
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8)) {
            tasks.clear();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";", 4);
                if (parts.length == 4) {
                    long id = Long.parseLong(parts[0]);
                    Task t = new Task(parts[1], id);
                    t.completed = Boolean.parseBoolean(parts[2]);
                    t.data = parts[3];
                    tasks.add(t);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}