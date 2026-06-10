package org.example.subchecker.telegram.stateDiary;

public class BotState {
    public static final String IDLE = "IDLE";

    // Добавление
    public static final String WAIT_NAME = "WAIT_NAME";
    public static final String WAIT_CATEGORY = "WAIT_CATEGORY";
    public static final String WAIT_CUSTOM_CATEGORY = "WAIT_CUSTOM_CATEGORY";
    public static final String WAIT_PERIOD = "WAIT_PERIOD";
    public static final String WAIT_CUSTOM_PERIOD = "WAIT_CUSTOM_PERIOD";
    public static final String WAIT_PRICE = "WAIT_PRICE";
    public static final String WAIT_URL = "WAIT_URL";
    public static final String WAIT_NOTES = "WAIT_NOTES";

    // Инвайты и продление
    public static final String WAIT_INVITE_CODE = "WAIT_INVITE_CODE";
    public static final String WAIT_RENEW_PERIOD = "WAIT_RENEW_PERIOD";
    public static final String WAIT_CUSTOM_RENEW = "WAIT_CUSTOM_RENEW";

    // Настройки
    public static final String WAIT_PREF_TIME = "WAIT_PREF_TIME";
    public static final String WAIT_CUSTOM_TZ = "WAIT_CUSTOM_TZ";

    // Редактирование подписки
    public static final String WAIT_EDIT_NAME = "WAIT_EDIT_NAME";
    public static final String WAIT_EDIT_PRICE = "WAIT_EDIT_PRICE";
    public static final String WAIT_EDIT_URL = "WAIT_EDIT_URL";
    public static final String WAIT_EDIT_NOTES = "WAIT_EDIT_NOTES";
    public static final String WAIT_EDIT_BOMBER_INT = "WAIT_EDIT_BOMBER_INT";
}