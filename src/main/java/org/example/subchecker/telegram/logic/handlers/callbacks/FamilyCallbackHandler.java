package org.example.subchecker.telegram.logic.handlers.callbacks;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.SubscriptionMemberService;
import org.example.subchecker.service.SubscriptionService;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.messageServiceTg.FamilyMessageService;
import org.example.subchecker.telegram.stateDiary.CallbackData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FamilyCallbackHandler {

    private final SubscriptionService subService;
    private final SubscriptionMemberService memberService;
    private final FamilyMessageService familyMessages;
    private final CommonMessageService commonMessages;

    public List<SendMessage> handle(Long chatId, String data) {
        if (data.startsWith(CallbackData.FAMILY_MGMT_PREFIX)) {
            Long id = extractId(data, CallbackData.FAMILY_MGMT_PREFIX);
            var sub = subService.getById(id, chatId);
            return List.of(familyMessages.getFamilyMenu(chatId, id, sub.getServiceName(), sub.getInviteCode()));
        }

        if (data.startsWith(CallbackData.GEN_CODE_PREFIX)) {
            Long id = extractId(data, CallbackData.GEN_CODE_PREFIX);
            String code = subService.generateInviteCode(id);
            return List.of(familyMessages.getInviteCodeCreatedMessage(chatId, code));
        }

        if (data.startsWith(CallbackData.LIST_MEMBERS_PREFIX)) {
            Long id = extractId(data, CallbackData.LIST_MEMBERS_PREFIX);

            var sub = subService.getById(id, chatId);
            var members = subService.getSubscriptionMembers(id);

            return List.of(familyMessages.getMembersList(chatId, id, members, sub.getOwnerId()));
        }

        if (data.startsWith(CallbackData.KICK_PREFIX)) {
            String[] p = data.split("_");
            Long subId = Long.parseLong(p[1]);
            Long targetId = Long.parseLong(p[2]);

            memberService.kickMember(subId, targetId);
            var sub = subService.getById(subId, chatId);
            var members = subService.getSubscriptionMembers(subId);

            return List.of(
                    familyMessages.getMemberKickedMessage(chatId),
                    familyMessages.getMembersList(chatId, subId, members, sub.getOwnerId())
            );
        }
        if (data.startsWith(CallbackData.REVOKE_CODE_PREFIX)) {
            Long id = extractId(data, CallbackData.REVOKE_CODE_PREFIX);

            subService.revokeInviteCode(id);

            return List.of(
                    familyMessages.getCodeRevokedMessage(chatId), // МЕТОД ОЖИЛ!
                    familyMessages.getFamilyMenu(chatId, id, "Обновление", null)
            );
        }

        if (data.startsWith(CallbackData.LEAVE_PREFIX)) {

            Long id = Long.parseLong(data.substring(CallbackData.LEAVE_PREFIX.length()));
            memberService.kickMember(id, chatId);

            return List.of(familyMessages.getLeftSubscriptionMessage(chatId));
        }
        return null;
    }

    private Long extractId(String data, String prefix) {
        return Long.parseLong(data.substring(prefix.length()));
    }
}