package org.hassan.fadedminions.menus.enums;

import org.hassan.fadedminions.FadedMinions;

import java.util.List;

public enum Message {
    PLAYER_DOES_NOT_EXIST,
    MINION_DOES_NOT_EXIST,
    ADD_MINION_COMMAND,
    ADD_TARGET_MINION_COMMAND,
    NO_TOKENS_TO_CLAIM,
    TOKENS_CLAIM,
    BOUGHT_UPGRADE,
    NO_MINIONS_IN_THAT_TIER,
    MINION_IS_LAST_RANK,
    UPGRADE_ALL,
    NOT_ENOUGH_TOKENS;

    public List<String> getMessage(FadedMinions plugin){
        return plugin.getMessageConfig().getConfiguration().getStringList(this.name());
    }

}
