package org.example.subchecker.telegram.messageServiceTg;

import org.example.subchecker.dto.UserDTO;
import org.example.subchecker.telegram.botKeyboards.FamilyKeyboards;
import org.example.subchecker.telegram.botKeyboards.PrimaryKeyboards;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;

@Service
public class FamilyMessageService {

    public SendMessage getFamilyMenu(Long chatId, Long subId, String serviceName, String inviteCode) {
        String text = "👨‍👩‍👧‍👦 *Управление группой:* " + serviceName + "\n\n" +
                (inviteCode != null ? "🎫 Действующий код: `" + inviteCode + "`" : "_Код пока не создан_");
        return create(chatId, text, FamilyKeyboards.managementMenu(subId, inviteCode));
    }

    public SendMessage getMembersList(Long chatId, Long subId, List<UserDTO> members, Long ownerId) {

        return create(chatId, "👥 *Участники группы:*", FamilyKeyboards.membersList(subId, members, ownerId));
    }

    // 3. Сообщение с новым кодом (Новое!)
    public SendMessage getInviteCodeCreatedMessage(Long chatId, String code) {
        return create(chatId, "🎫 *Инвайт-код создан:* `" + code + "`\n\n_Перешли его другу. Он должен нажать «Принять инвайт» в меню._", null);
    }

    public SendMessage getCodeRevokedMessage(Long chatId) {
        return create(chatId, "🚫 *Инвайт-код аннулирован.*\nНовые участники больше не смогут вступить по нему.", null);
    }

    private SendMessage create(Long chatId, String text, org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard kb) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(kb)
                .parseMode("Markdown")
                .build();
    }
    public SendMessage getMemberKickedMessage(Long chatId) {
        return create(chatId, "👢 *Участник успешно удален из этой подписки.*", null);
    }

    public SendMessage getLeftSubscriptionMessage(Long chatId) {
        return create(chatId, "🚪 *Ты успешно покинул группу подписки.*\nОна больше не будет отображаться в твоем списке.",
                PrimaryKeyboards.mainMenu());
    }
}