package org.example.subchecker.telegram.botKeyboards;

import org.example.subchecker.core.dto.UserDTO;
import org.example.subchecker.telegram.feature.stateDiary.CallbackData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class FamilyKeyboards {

    /**
     * Меню управления группой и инвайтами.
     * Использует: GEN_CODE_PREFIX, REVOKE_CODE_PREFIX, LIST_MEMBERS_PREFIX, SELECT_PREFIX
     */
    public static InlineKeyboardMarkup managementMenu(Long subId, String currentCode) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        if (currentCode == null) {
            rows.add(List.of(
                    KeyboardUtils.createInlineBtn("🎫 Создать инвайт-код", CallbackData.GEN_CODE_PREFIX + subId)
            ));
        } else {
            rows.add(List.of(
                    KeyboardUtils.createInlineBtn("🚫 Сбросить код (" + currentCode + ")", CallbackData.REVOKE_CODE_PREFIX + subId)
            ));
        }

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("👥 Список участников", CallbackData.LIST_MEMBERS_PREFIX + subId)
        ));

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.SELECT_PREFIX + subId)
        ));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }


    public static InlineKeyboardMarkup membersList(Long subId, List<UserDTO> members, Long ownerId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (UserDTO member : members) {
            String name = member.getFirstName() != null ? member.getFirstName() : member.getUsername();
            boolean isOwner = member.getTelegramId().equals(ownerId);

            if (isOwner) {
                rows.add(List.of(
                        KeyboardUtils.createInlineBtn("👑 " + name + " (Хост)", "NONE")
                ));
            } else {
                rows.add(List.of(
                        KeyboardUtils.createInlineBtn("👤 " + name, "NONE"),
                        KeyboardUtils.createInlineBtn("🚫 Выгнать", CallbackData.KICK_PREFIX + subId + "_" + member.getTelegramId())
                ));
            }
        }

        rows.add(List.of(KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.FAMILY_MGMT_PREFIX + subId)));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}