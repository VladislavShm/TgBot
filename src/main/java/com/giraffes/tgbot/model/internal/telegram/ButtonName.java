package com.giraffes.tgbot.model.internal.telegram;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ButtonName {
    
    @Getter
    @AllArgsConstructor
    public enum OkButton implements Button {
        OK_BUTTON("common.button.ok"),
        ;

        private final String code;
    }
    
    @Getter
    @AllArgsConstructor
    public enum BackCancelButton implements Button {
        BACK_BUTTON("common.button.back"),
        CANCEL_BUTTON("common.button.cancel"),
        ;

        private final String code;
    }

    @Getter
    @AllArgsConstructor
    public enum BaseLocationButton implements Button {
        BUY_BUTTON("base_location.button.buy"),
        INVITE_INFO_BUTTON("base_location.button.invite_info"),
        MY_GIRAFFES_BUTTON("base_location.button.my_giraffes"),
        AUCTION_BUTTON("base_location.button.auction"),
        ABOUT_COLLECTION_BUTTON("base_location.button.about_collection"),
        SETTINGS_BUTTON("base_location.button.settings"),
        ;

        private final String code;
    }

    @Getter
    @AllArgsConstructor
    public enum SettingsLocationButton implements Button {
        SPECIFY_WALLET_BUTTON("settings_location.button.specify_wallet"),
        CHANGE_WALLET_BUTTON("settings_location.button.change_wallet"),
        CONFIRM_WALLET_BUTTON("settings_location.button.confirm_wallet"),
        CHANGE_LANGUAGE_BUTTON("settings_location.button.change_language"),
        ;

        private final String code;
    }

    @Getter
    @AllArgsConstructor
    public enum LanguageChangeLocationButton implements Button {
        EN_BUTTON("language_change.button.en"),
        RU_BUTTON("language_change.button.ru"),
        ;

        private final String code;
    }

}
