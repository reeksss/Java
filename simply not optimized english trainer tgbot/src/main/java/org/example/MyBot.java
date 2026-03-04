package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MyBot extends TelegramLongPollingBot {

    // НОВАЯ СТРОКА: перечисление всех "режимов" бота
    enum BotState { IDLE, AWAITING_ADD, CHOISE, RUS, ENG, MIX, REMOVE_WORD, ACCEPT_CLEAR, CHOISE_RS, PLAY_RS, RUS_RS, ENG_RS, MIX_RS }

    // НОВАЯ СТРОКА: HashMap, которая помнит, в каком режиме находится каждый юзер (chatId -> режим)
    private final Map<Long, BotState> userStates = new HashMap<>();

    // Теперь у каждого юзера свой список
    private final Map<Long, List<Word>> userWords = new HashMap<>();

    private final Map<Long, String> trueanswer = new HashMap<>();

    private final Map<Long, String> trueanswerRS = new HashMap<>();

    private final List<Word> animals = new ArrayList<>();

    public MyBot() { // Конструктор
        try {
            List<String> lines = Files.readAllLines(Paths.get("animals"));
            for (String s : lines) {
                String[] parts = s.trim().split(":", 2);
                String ruword = parts[0].trim();
                String engword = parts[1].trim();
                Word d = new Word(ruword, engword);
                animals.add(d);
                loadFromFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "3";
    }

    @Override
    public String getBotToken() {
        return "8785779584:AAG50a4Tl_1Q6sc1c9n0Z9O37PQe-5VhkxA";
    }

    public static class Word {
        String word;
        String translation;
        public Word(String word, String translation) {
            this.word = word;
            this.translation = translation;
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            List<Word> words = userWords.computeIfAbsent(chatId, k -> new ArrayList<>());
            // Узнаем текущее состояние юзера. Если его нет ставим IDLE (покой)
            BotState currentState = userStates.getOrDefault(chatId, BotState.IDLE);


            /*
            НАЧАЛЬНЫЕ КОМАНДЫ
            НАЧАЛЬНЫЕ КОМАНДЫ
             */


            if (messageText.equals("/start")) {
                sendResponse(chatId, "Добро пожаловать в бот-тренажёр по английскому языку");
                sendMainMenu(chatId);
                // При старте сбрасываем состояние в IDLE
                userStates.put(chatId, BotState.IDLE);
            }
            // Добавление слова
            else if (messageText.equals("Добавить слово ➕")) {
                sendResponse(chatId, "Введите слово и перевод через пробел (Например Яблоко Apple)\nДля удобства рекомендуется писать все слова с большой буквы");
                userStates.put(chatId, BotState.AWAITING_ADD);
            }
            // Если юзер в режиме добавления И это не команда /start
            else if (currentState == BotState.AWAITING_ADD) {
                handleAddWordLogic(chatId, messageText, words);
            }
            // Просмотр словаря
            else if(messageText.equals("Мой словарь\uD83D\uDCD6") && currentState == BotState.IDLE) {
                if(words.isEmpty()) {
                    sendResponse(chatId, "В твоем словаре пусто!");
                } else {
                    StringBuilder wordResponse = new StringBuilder("Твой словарь:\n");
                    int k;
                    for(k = 0; k<words.size(); k++) {
                        wordResponse.append(k+1).append(". ").append(words.get(k).word).append(" - ").append(words.get(k).translation).append("\n");
                    }
                    sendResponse(chatId, wordResponse.toString());
                }

            }


            /*
            ВИКТОРИНА СО СВОИМ СЛОВАРЕМ
            ВИКТОРИНА СО СВОИМ СЛОВАРЕМ
             */


            // Викторина
            else if(messageText.equals("Викторина ❓") && currentState == BotState.IDLE) {
                userStates.put(chatId, BotState.CHOISE);
                choiseMenu(chatId);
            }
            // Выход из викторины
            else if(messageText.equals("Выйти из викторины\uD83D\uDEAA") && currentState == BotState.CHOISE) {
                userStates.put(chatId, BotState.IDLE);
                sendMainMenu(chatId);
            }
            // Русская викторина
            else if(messageText.equals("Перевести на русский\uD83C\uDDF7\uD83C\uDDFA") && currentState == BotState.CHOISE) {
                if(words.isEmpty()) {
                    sendResponse(chatId, "Твой словарь пуст! Добавь в него слова!");
                    userStates.put(chatId, BotState.CHOISE);
                    choiseMenu(chatId);
                } else {
                    Random random = new Random();
                    int number = random.nextInt(words.size());
                    String wrd = words.get(number).translation;
                    trueanswer.put(chatId, words.get(number).word);
                    sendResponse(chatId, "Переведи на русский слово \"" + wrd + "\"\nСлово писать так же, как и в словаре");
                    userStates.put(chatId, BotState.RUS);
                }
            }
            // Английская викторина
            else if(messageText.equals("Перевести на английский\uD83C\uDDEC\uD83C\uDDE7") && currentState == BotState.CHOISE) {
                if(words.isEmpty()) {
                    sendResponse(chatId, "Твой словарь пуст! Добавь в него слова!");
                    userStates.put(chatId, BotState.CHOISE);
                    choiseMenu(chatId);
                } else {
                    Random random = new Random();
                    int number = random.nextInt(words.size());
                    String wrd = words.get(number).word;
                    trueanswer.put(chatId, words.get(number).translation);
                    sendResponse(chatId, "Переведи на английский слово \"" + wrd + "\"\nСлово писать так же, как и в словаре");
                    userStates.put(chatId, BotState.ENG);
                }
            }
            // Викторина Микс
            else if(messageText.equals("Микс\uD83C\uDDF7\uD83C\uDDFA\uD83C\uDDEC\uD83C\uDDE7") && currentState == BotState.CHOISE) {
                if(words.isEmpty()) {
                    sendResponse(chatId, "Твой словарь пуст! Добавь в него слова!");
                    userStates.put(chatId, BotState.CHOISE);
                    choiseMenu(chatId);
                } else {
                    Random random = new Random();
                    int number = random.nextInt(words.size());
                    int index = random.nextInt(1,3);
                    if(index == 1) {
                        String wrd = words.get(number).word;
                        trueanswer.put(chatId, words.get(number).translation);
                        sendResponse(chatId, "Переведи на английский слово \"" + wrd + "\"\nСлово писать так же, как и в словаре");
                        userStates.put(chatId, BotState.MIX);
                    } else {
                        String wrd = words.get(number).translation;
                        trueanswer.put(chatId, words.get(number).word);
                        sendResponse(chatId, "Переведи на русский слово \"" + wrd + "\"\nСлово писать так же, как и в словаре");
                        userStates.put(chatId, BotState.RUS);
                    }

                }
            }
            //Результат для русской викторины
            else if(currentState == BotState.RUS) {
                String answer = messageText.trim();
                String correctanswer = trueanswer.get(chatId);
                if(correctanswer.equals(answer)) {
                    sendResponse(chatId, "✅Ты ответил(а) верно!\uD83C\uDF89");

                } else {
                    sendResponse(chatId, "❌К сожалению ты ответил(а) неверно.\nПравильный ответ был " + correctanswer);
                }
                userStates.put(chatId, BotState.CHOISE);
                choiseMenu(chatId);
            }
            //Результат для Микс викторины
            else if(currentState == BotState.MIX) {
                String answer = messageText.trim();
                String correctanswer = trueanswer.get(chatId);
                if(Objects.equals(correctanswer, answer)) {
                    sendResponse(chatId, "✅Ты ответил(а) верно!\uD83C\uDF89");
                } else {
                    sendResponse(chatId, "❌К сожалению ты ответил(а) неверно.\nПравильный ответ был " + correctanswer);
                }
                userStates.put(chatId, BotState.CHOISE);
                choiseMenu(chatId);
            }
            //Результат для английской викторины
            else if(currentState == BotState.ENG) {
                String answer = messageText.trim();
                String correctanswer = trueanswer.get(chatId);
                if(Objects.equals(correctanswer, answer)) {
                    sendResponse(chatId, "✅Ты ответил(а) верно!\uD83C\uDF89");
                } else {
                    sendResponse(chatId, "❌К сожалению ты ответил(а) неверно.\nПравильный ответ был " + correctanswer);
                }
                userStates.put(chatId, BotState.CHOISE);
                choiseMenu(chatId);
            }

            /*
            ГОТОВЫЕ СЛОВАРИ
            ГОТОВЫЕ СЛОВАРИ
             */


            // Переход в меню готовых словарей
            else if(messageText.equals("Викторина по готовым словарям\uD83D\uDCDA") && currentState == BotState.IDLE) {
                setMenuReadySL(chatId);
                userStates.put(chatId, BotState.CHOISE_RS);
            }
            // Выход из готовых словарей в меню
            else if(messageText.equals("Выход\uD83D\uDEAA") && currentState == BotState.CHOISE_RS) {
                userStates.put(chatId, BotState.IDLE);
                sendMainMenu(chatId);
            }
            // Переход в меню выбора викторины готовых словарей(меню викторины)
            else if(messageText.equals("Животные\uD83D\uDC15") && currentState == BotState.CHOISE_RS) {
                choiseMenuRS(chatId);
                userStates.put(chatId, BotState.PLAY_RS);
            }

            //Выход из меню выбора викторины для готовых словарей(меню викторины) в меню готовых словарей
            else if(messageText.equals("Выйти из викторины\uD83D\uDEAA") && currentState == BotState.PLAY_RS) {
                setMenuReadySL(chatId);
                userStates.put(chatId, BotState.CHOISE_RS);
            }
            // Ру викторина для словаря животных
            else if(messageText.equals("Перевести на русский\uD83C\uDDF7\uD83C\uDDFA") && currentState == BotState.PLAY_RS) {
                Random random = new Random();
                int number = random.nextInt(animals.size());
                String wrd = animals.get(number).translation;
                trueanswerRS.put(chatId, animals.get(number).word);
                sendResponse(chatId, "Переведи на русский слово \"" + wrd + "\"");
                userStates.put(chatId, BotState.RUS_RS);
            }
            //Англ викторина для словаря животных
            else if(messageText.equals("Перевести на английский\uD83C\uDDEC\uD83C\uDDE7") && currentState == BotState.PLAY_RS) {
                Random random = new Random();
                int number = random.nextInt(animals.size());
                String wrd = animals.get(number).word;
                trueanswerRS.put(chatId, animals.get(number).translation);
                sendResponse(chatId, "Переведи на английский слово \"" + wrd + "\"");
                userStates.put(chatId, BotState.ENG_RS);

            }
            // Викторина Микс для словаря животных
            else if(messageText.equals("Микс\uD83C\uDDF7\uD83C\uDDFA\uD83C\uDDEC\uD83C\uDDE7") && currentState == BotState.PLAY_RS) {
                Random random = new Random();
                int number = random.nextInt(animals.size());
                int index = random.nextInt(1,3);
                if(index == 1) {
                    String wrd = animals.get(number).word;
                    trueanswerRS.put(chatId, animals.get(number).translation);
                    sendResponse(chatId, "Переведи на английский слово \"" + wrd + "\"");
                    userStates.put(chatId, BotState.MIX_RS);
                } else {
                    String wrd = animals.get(number).translation;
                    trueanswerRS.put(chatId, animals.get(number).word);
                    sendResponse(chatId, "Переведи на русский слово \"" + wrd + "\"");
                    userStates.put(chatId, BotState.RUS_RS);
                }
            }

            // Вердикт ответа для словаря животных

            //Результат для ру викторины
            else if(currentState == BotState.RUS_RS) {
                String answer = messageText.trim();
                String correctanswer = trueanswerRS.get(chatId);
                if(correctanswer.equalsIgnoreCase(answer)) {
                    sendResponse(chatId, "✅Ты ответил(а) верно!\uD83C\uDF89");

                } else {
                    sendResponse(chatId, "❌К сожалению ты ответил(а) неверно.\nПравильный ответ был " + correctanswer);
                }
                userStates.put(chatId, BotState.PLAY_RS);
                choiseMenuRS(chatId);
            }
            //Результат для Микс викторины
            else if(currentState == BotState.MIX_RS) {
                String answer = messageText.trim();
                String correctanswer = trueanswerRS.get(chatId);
                if(Objects.equals(correctanswer, answer)) {
                    sendResponse(chatId, "✅Ты ответил(а) верно!\uD83C\uDF89");
                } else {
                    sendResponse(chatId, "❌К сожалению ты ответил(а) неверно.\nПравильный ответ был " + correctanswer);
                }
                userStates.put(chatId, BotState.PLAY_RS);
                choiseMenuRS(chatId);
            }
            //Результат для английской викторины
            else if(currentState == BotState.ENG_RS) {
                String answer = messageText.trim();
                String correctanswer = trueanswerRS.get(chatId);
                if(Objects.equals(correctanswer, answer)) {
                    sendResponse(chatId, "✅Ты ответил(а) верно!\uD83C\uDF89");
                } else {
                    sendResponse(chatId, "❌К сожалению ты ответил(а) неверно.\nПравильный ответ был " + correctanswer);
                }
                userStates.put(chatId, BotState.PLAY_RS);
                choiseMenuRS(chatId);
            }

            /*
            ОЧИСТКА И УДАЛЕНИЕ СЛОВ ИЗ СЛОВАРЯ
            ОЧИСТКА И УДАЛЕНИЕ СЛОВ ИЗ СЛОВАРЯ
             */

            // Спросить подтверждение при нажатии на очистку
            else if(messageText.equals("Очистить словарь\uD83D\uDDD1") && currentState == BotState.IDLE) {
                setAcceptMenu(chatId);
                userStates.put(chatId, BotState.ACCEPT_CLEAR);
            }
            // Очищение словаря при подтверждении
            else if(messageText.equals("Подтвердить✅") && currentState == BotState.ACCEPT_CLEAR) {
                words.clear();
                saveToFile();
                sendResponse(chatId, "Словарь успешно очищен!♻");
                userStates.put(chatId, BotState.IDLE);
                sendMainMenu(chatId);
            }
            // Отмена очищения и выход в меню
            else if(messageText.equals("Выход\uD83D\uDEAA") && currentState == BotState.ACCEPT_CLEAR) {
                userStates.put(chatId, BotState.IDLE);
                sendMainMenu(chatId);
            }

            // Удаление слова из словаря
            else if(messageText.equals("Удалить слово из словаря\uD83D\uDEAB") && currentState == BotState.IDLE) {
                sendResponse(chatId, "Введи слово которое надо удалить:");
                userStates.put(chatId, BotState.REMOVE_WORD);
            }
            // Ввод удаляемого слова и его удаление
            else if(currentState == BotState.REMOVE_WORD) {
                String deleteword = messageText.trim();
                // Найдено ли слово
                boolean wordfound = false;
                for(Word w: words) {
                    if(w.word.equals(deleteword)) {
                        wordfound = true;
                        break;
                    }
                }
                // Найдено ли слово по переводу
                boolean translationwordfound = false;
                for(Word k: words) {
                    if(k.translation.equals(deleteword)) {
                        translationwordfound = true;
                        break;
                    }
                }
                // Если Слова в словаре нет
                if(!translationwordfound && !wordfound) {
                    sendResponse(chatId, "Такого слова в словаре нету!\uD83D\uDD34\nПроверь написание");
                    userStates.put(chatId, BotState.IDLE);
                    sendMainMenu(chatId);
                } else {
                    if(translationwordfound) {
                        for(int i = 0; i < words.size(); i++) {
                            if(words.get(i).translation.equals(deleteword)) {
                                words.remove(words.get(i));
                                saveToFile();
                                break;
                            }
                        }
                    }
                    // Если слово найдено
                    else {
                        for(int i = 0; i < words.size(); i++) {
                            if(words.get(i).word.equals(deleteword)) {
                                words.remove(words.get(i));
                                saveToFile();
                                break;
                            }
                        }
                    }
                    sendResponse(chatId, "Слово \"" + deleteword + "\" удалено из словаря\uD83D\uDDD1");
                    userStates.put(chatId, BotState.IDLE);
                    sendMainMenu(chatId);
                }

            }
        }
    }


    /*
    МЕТОДЫ И КЛАВИАТУРЫ
    МЕТОДЫ И КЛАВИАТУРЫ
     */


    //Метод для логики добавления
    private void handleAddWordLogic(long chatId, String messageText, List<Word> words) {
        try {
            String[] parts = messageText.trim().split(" ", 2);
            if (parts.length < 2) {
                sendResponse(chatId, "⚠️Ошибка! Нужно ввести два слова через пробел.");
                userStates.put(chatId, BotState.IDLE);
            } else {
                String addword = parts[0].trim();
                String addtranslationword = parts[1].trim();

                // Проверка на дубликаты именно в списке этого юзера
                boolean exists = false;
                for (Word w : words) {
                    if (w.word.equalsIgnoreCase(addword) || w.translation.equalsIgnoreCase(addtranslationword)) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    sendResponse(chatId, "⚠️Такое слово или перевод уже есть");
                    userStates.put(chatId, BotState.IDLE);
                } else {
                    words.add(new Word(addword, addtranslationword));
                    saveToFile(); // СОХРАНЯЕМ В ФАЙЛ
                    sendResponse(chatId, "✅ Слово добавлено!");
                    userStates.put(chatId, BotState.IDLE);
                }
            }
        } catch (Exception e) {
            sendResponse(chatId, "Что-то пошло не так...");
            userStates.put(chatId, BotState.IDLE);
        }
    }
    // Меню выбора режима викторины
    private void choiseMenu(long chatId) {
        SendMessage message= new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выбери режим викторины:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Перевести на русский\uD83C\uDDF7\uD83C\uDDFA");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Перевести на английский\uD83C\uDDEC\uD83C\uDDE7");
        KeyboardRow row3 = new KeyboardRow();
        row3.add("Микс\uD83C\uDDF7\uD83C\uDDFA\uD83C\uDDEC\uD83C\uDDE7");
        KeyboardRow row4 = new KeyboardRow();
        row4.add("Выйти из викторины\uD83D\uDEAA");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void hideMenu(long chatId, String text) {

    }

    //Меню выбора режима викторины для готовых словарей
    private void choiseMenuRS(long chatId) {
        SendMessage message= new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выбери режим викторины для выбранного готового словаря:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Перевести на русский\uD83C\uDDF7\uD83C\uDDFA");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Перевести на английский\uD83C\uDDEC\uD83C\uDDE7");
        KeyboardRow row3 = new KeyboardRow();
        row3.add("Микс\uD83C\uDDF7\uD83C\uDDFA\uD83C\uDDEC\uD83C\uDDE7");
        KeyboardRow row4 = new KeyboardRow();
        row4.add("Выйти из викторины\uD83D\uDEAA");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Начальное меню
    private void sendMainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выбери действие:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить слово ➕");
        row1.add("Викторина ❓");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Мой словарь\uD83D\uDCD6");
        KeyboardRow row3 = new KeyboardRow();
        row3.add("Очистить словарь\uD83D\uDDD1");
        KeyboardRow row4 = new KeyboardRow();
        row4.add("Удалить слово из словаря\uD83D\uDEAB");
        KeyboardRow row5 = new KeyboardRow();
        row5.add("Викторина по готовым словарям\uD83D\uDCDA");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    // Меню выбора готового словаря
    private void setMenuReadySL(long chatId) {
        SendMessage message= new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите готовый словарь:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Животные\uD83D\uDC15");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Выход\uD83D\uDEAA");

        keyboard.add(row1);
        keyboard.add(row2);


        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Меню подтверждения очистки
    private void setAcceptMenu(long chatId) {
        SendMessage message= new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Подтвердите очистку словаря\uD83D\uDDD1:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Подтвердить✅");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Выход\uD83D\uDEAA");

        keyboard.add(row1);
        keyboard.add(row2);


        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    // Отправка сообщения
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
    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("database.txt"))) {
            for (Map.Entry<Long, List<Word>> entry : userWords.entrySet()) {
                for (Word w : entry.getValue()) {
                    writer.println(entry.getKey() + ":" + w.word + ":" + w.translation);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        try {
            File file = new File("database.txt");
            if (!file.exists()) return;
            List<String> lines = Files.readAllLines(file.toPath());
            for (String s : lines) {
                String[] p = s.split(":", 3);
                if (p.length == 3) {
                    long chatId = Long.parseLong(p[0]);
                    userWords.computeIfAbsent(chatId, k -> new ArrayList<>()).add(new Word(p[1], p[2]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}