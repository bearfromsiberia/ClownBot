package com.bots.clownbot.service;

import com.bots.clownbot.config.BotConfig;
import com.bots.clownbot.models.ChatUsers;
import com.bots.clownbot.models.Habits;
import com.bots.clownbot.repositories.ChatUsersRepository;
import com.bots.clownbot.repositories.HabitsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig botConfig;

    final HabitsRepository habitsRepository;

    final ChatUsersRepository chatUsersRepository;

    public TelegramBot(BotConfig botConfig, HabitsRepository habitsRepository, ChatUsersRepository chatUsersRepository) throws TelegramApiException {
        this.botConfig = botConfig;
        this.habitsRepository = habitsRepository;
        this.chatUsersRepository = chatUsersRepository;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
    boolean deal = true;
    boolean delete = true;
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText() && deal && delete){
            String message = update.getMessage().getText();
            long chadId = update.getMessage().getChatId();
            switch (message){
                case "/start" -> {
                    startCommandReceived(chadId,update.getMessage().getChat().getFirstName(), update);
                }
                case "Анекдот" -> {
                    sendJoke(chadId,update.getMessage().getChat().getFirstName());
                }
                case "Сохранить дело" ->{
                    deal = false;
                    sendMessage(update.getMessage().getChatId(),"Пишите дело");
                }
                case "Вывести дела" -> printDeals(update);
                case "Удалить дело" -> {
                    sendMessage(chadId,"Введите id дела");
                    delete = false;
                }
                default -> sendMessage(chadId,"Some broken time");
            }
        } else if (update.hasMessage() && update.getMessage().hasText() && !deal && delete) {
            saveDeal(update);
            deal = true;

        }
        else if (update.hasMessage() && update.getMessage().hasText() && deal && !delete) {
            delete(update);
            delete = true;

        }


    }

    public void startCommandReceived(long chatId, String name, Update update){
        String answer = "Приветствую вас";
        sendMessage(chatId,answer);
        registration(update);
        log.info("Replied to user " + name);
    }

    public void sendJoke(long chatId, String name){
        String answer = "You are dumb! ahahaha";
        sendMessage(chatId,answer);
        log.info("Replied to user " + name);
    }

    public void sendMessage(long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Анекдот");
        row.add("Сохранить дело");
        row.add("Вывести дела");
        row.add("Удалить дело");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        try{
            execute(sendMessage);
        }catch (TelegramApiException ex){
            log.error("Error occurred: " + ex.getMessage());
        }
    }
    public void registration(Update update){
        ChatUsers user = new ChatUsers();
        user.setFirstName(update.getMessage().getChat().getFirstName());
        user.setSecondName(update.getMessage().getChat().getLastName());
        user.setChatId(update.getMessage().getChatId());
        chatUsersRepository.save(user);
    }
    public void saveDeal(Update update){
        Habits deal = new Habits();
        deal.setUserId(update.getMessage().getChatId());
        deal.setDescription(update.getMessage().getText());
        habitsRepository.save(deal);
        sendMessage(update.getMessage().getChatId(),"Дело сохраненно");
    }
    public void printDeals(Update update){
        Iterable<Habits> habits = habitsRepository.findByUserId(update.getMessage().getChatId());
        StringBuilder message = new StringBuilder();
        sendMessage(update.getMessage().getChatId(),"Нашли дела");
        for(Habits habit : habits){
            message.append(habit.getId()).append(". ").append(habit.getDescription()).append("\n");
        }
        sendMessage(update.getMessage().getChatId(),message.toString());
    }
    public void delete(Update update){
        if (habitsRepository.findById(Long.valueOf(update.getMessage().getText())).isEmpty())
            sendMessage(update.getMessage().getChatId(),"Дело с таким ID не существует");
        else {
            habitsRepository.delete(habitsRepository.findById(Long.valueOf(update.getMessage().getText())).get());
            sendMessage(update.getMessage().getChatId(),"Дело удаленно");
        }
    }
}
