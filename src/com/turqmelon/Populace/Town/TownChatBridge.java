package com.turqmelon.Populace.Town;

import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Utils.Msg;
import com.turqmelon.PopulaceChat.Channels.ChannelManager;
import com.turqmelon.PopulaceChat.Channels.ChatChannel;
import com.turqmelon.PopulaceChat.Channels.core.TownChat;

/******************************************************************************
 * *
 * CONFIDENTIAL                                                               *
 * __________________                                                         *
 * *
 * [2012 - 2016] Devon "Turqmelon" Thome                                      *
 * All Rights Reserved.                                                      *
 * *
 * NOTICE:  All information contained herein is, and remains                  *
 * the property of Turqmelon and its suppliers,                               *
 * if any.  The intellectual and technical concepts contained                 *
 * herein are proprietary to Turqmelon and its suppliers and                  *
 * may be covered by U.S. and Foreign Patents,                                *
 * patents in process, and are protected by trade secret or copyright law.    *
 * Dissemination of this information or reproduction of this material         *
 * is strictly forbidden unless prior written permission is obtained          *
 * from Turqmelon.                                                            *
 * *
 ******************************************************************************/
public class TownChatBridge {

    public static void firstTownJoin(Resident resident) {
        TownChat townChannel = null;
        for (ChatChannel chatChannel : ChannelManager.getChannels()) {
            if ((chatChannel instanceof TownChat)) {
                townChannel = (TownChat) chatChannel;
                break;
            }
        }
        if (townChannel != null) {
            ChannelManager.focusChannel(resident, townChannel);
            resident.sendMessage(Msg.INFO + "The " + townChannel.getColor() + "Town§b chat can only be seen by other members of this town.");
            resident.sendMessage(Msg.INFO + "Send chat to it with §f/t <Message>§b or focus it with §f/t§b.");
        }
    }

}
