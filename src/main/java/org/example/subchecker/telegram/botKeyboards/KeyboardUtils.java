package org.example.subchecker.telegram.botKeyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class KeyboardUtils {

    public static InlineKeyboardButton createInlineBtn(String text, String callbackData){
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callbackData);
        return btn;
    }

    public static InlineKeyboardButton createUrlBtn(String text, String url){
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setUrl(url);
        return btn;
    }
}
