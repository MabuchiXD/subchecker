package org.example.subchecker.telegram.logic.handlers;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.SubscriptionMemberService;
import org.example.subchecker.telegram.logic.AddSubscriptionScript;
import org.example.subchecker.telegram.logic.EditSubscriptionScript; // ИМПОРТ
import org.example.subchecker.telegram.logic.RenewSubscriptionScript;
import org.example.subchecker.telegram.logic.SettingsFlow;
import org.example.subchecker.telegram.messageServiceTg.SubscriptionFlowMessageService;
import org.example.subchecker.telegram.stateDiary.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TextStateHandler {

    private final BotSessionService sessionService;
    private final SubscriptionMemberService memberService;
    private final AddSubscriptionScript addSubscriptionScript;
    private final EditSubscriptionScript editSubscriptionScript; // Внедряем скрипт правок
    private final RenewSubscriptionScript renewSubscriptionScript;
    private final SettingsFlow settingsFlow;
    private final SubscriptionFlowMessageService flowMessageService;

    public List<SendMessage> handle(Long chatId, String text, String state) {
        // 1. Инвайт-коды
        if (BotState.WAIT_INVITE_CODE.equals(state)) {
            try {
                String name = memberService.joinByCode(chatId, text);
                sessionService.updateState(chatId, BotState.IDLE);
                return List.of(flowMessageService.getInviteSuccess(chatId, name));
            } catch (Exception e) {
                return List.of(flowMessageService.getAskInviteCode(chatId));
            }
        }

        // 2. Настройки профиля (время, пояс)
        if (state.startsWith("WAIT_PREF_") || state.equals(BotState.WAIT_CUSTOM_TZ)) {
            return List.of(settingsFlow.handle(chatId, text, state));
        }

        // 3. РЕДАКТИРОВАНИЕ ПОДПИСКИ (имя, цена, ссылка, заметки)
        if (state.startsWith("WAIT_EDIT_")) {
            return List.of(editSubscriptionScript.handle(chatId, text, state));
        }

        // 4. Добавление новой подписки
        if (state.startsWith("WAIT_") && !state.contains("RENEW")) {
            return List.of(addSubscriptionScript.handle(chatId, text, state));
        }

        // 5. Продление
        if (state.contains("RENEW")) {
            return List.of(renewSubscriptionScript.handle(chatId, text, state));
        }

        return null;
    }
}