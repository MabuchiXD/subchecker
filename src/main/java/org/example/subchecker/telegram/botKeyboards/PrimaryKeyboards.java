package org.example.subchecker.telegram.botKeyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class PrimaryKeyboards {

    public static ReplyKeyboardMarkup mainMenu() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить подписку ➕");
        row1.add("Мои подписки 📓");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Войти в группу 📩");
        row2.add("Настройки ⚙️");

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .resizeKeyboard(true)
                .build();
    }
}