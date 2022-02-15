package org.hassan.fadedminions.commands;

import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hassan.fadedminions.FadedMinions;
import org.hassan.fadedminions.data.MinionData;
import org.hassan.fadedminions.menus.MainMinionInventory;
import org.hassan.fadedminions.menus.enums.Message;
import org.hassan.fadedminions.utils.Common;

@Command("fadedminions")
@Alias("m")
public class FadedMinionCommand extends CommandBase {

    private FadedMinions plugin;

    public FadedMinionCommand(FadedMinions plugin){
        this.plugin = plugin;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        if(commandSender instanceof Player){
            new MainMinionInventory(plugin, (Player) commandSender).open();
        }

    }

    @SubCommand("give")
    public void giveCommand(final CommandSender commandSender, Player target, String minionType, Integer level, Integer amount){
        if(target == null){
            Message.PLAYER_DOES_NOT_EXIST.getMessage(plugin).forEach(s -> Common.sendMessage(commandSender,s));
            return;
        }

        if(minionType.equalsIgnoreCase("") || !plugin.getMinionPlayerDataManager().getMinionItemMap().containsKey(minionType)){
            Message.MINION_DOES_NOT_EXIST.getMessage(plugin).forEach(s -> Common.sendMessage(commandSender,s));
            return;
        }
        for(int i = 0; i < amount; ++i){
            String path = "Minions." + minionType+ ".Tiers." + String.valueOf(level);
            MinionData minionData = new MinionData();
            minionData.setMinionType(minionType);
            minionData.setLevel(level);
            int timer = plugin.getMinionsConfig().getConfiguration().getInt(path + ".Timer");
            minionData.setTimer(timer);
            minionData.setCurrentTimer(timer);
            minionData.setTokens(0);
            int tokensToGenerate = plugin.getMinionsConfig().getConfiguration().getInt(path + ".Tokens-To-Generate");
            minionData.setTokensToGenerate(tokensToGenerate);
            plugin.getMinionPlayerDataManager().addMinion(target, minionData);
            Message.ADD_TARGET_MINION_COMMAND.getMessage(plugin).forEach(s -> Common.sendMessage(target,s
                    .replace("{minion}", minionType)));
        }
        Message.ADD_MINION_COMMAND.getMessage(plugin).forEach(s -> Common.sendMessage(commandSender,s
        .replace("{minion}", minionType)
        .replace("{target}", target.getName())));


    }

    @SubCommand("menu")
    public void subCommand(final CommandSender commandSender){
        new MainMinionInventory(plugin, (Player) commandSender).open();
    }
}
