/*
 * This file is part of DeltaRedis.
 *
 * DeltaRedis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DeltaRedis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeltaRedis.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaRedis.Spigot;

import com.gmail.tracebachi.DeltaRedis.Shared.Redis.DRCommandSender;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/11/15.
 */
public class DeltaRedisApi {
    private static DeltaRedisApi instance;

    private DRCommandSender deltaSender;
    private DeltaRedis plugin;

    /**
     * Package-private constructor.
     */
    DeltaRedisApi(DRCommandSender deltaSender, DeltaRedis plugin) {
        if (instance != null) {
            instance.shutdown();
        }

        this.deltaSender = deltaSender;
        this.plugin = plugin;

        instance = this;
    }

    public static DeltaRedisApi instance() {
        return instance;
    }

    /**
     * Package-private shutdown method.
     */
    void shutdown() {
        this.deltaSender = null;
        this.plugin = null;

        instance = null;
    }

    /**
     * @return Name of the BungeeCord instance to which the server belongs.
     * This value is set in the configuration file for each server.
     */
    public String getBungeeName() {
        return plugin.getBungeeName();
    }

    /**
     * @return Name of the server (String). If the server is BungeeCord, the
     * server name will be {@link Servers#BUNGEECORD}.
     */
    public String getServerName() {
        return plugin.getServerName();
    }

    /**
     * @return An unmodifiable set of servers that are part of the same
     * BungeeCord as the current server. This method will retrieve the
     * servers from the last call to {@link DRCommandSender#refresh()}.
     */
    public Set<String> getCachedServers() {
        return deltaSender.getCachedServers();
    }

    /**
     * @return True if the BungeeCord instance was last known to be online.
     * False if it was not.
     */
    public boolean isBungeeCordOnline() {
        return deltaSender.isBungeeCordOnline();
    }

    /**
     * @param partial Non-null string that is the beginning of a name
     * @return A list of player names that begins with the partial
     * sent to this method.
     */
    public List<String> matchStartOfServerName(String partial) {
        Preconditions.checkNotNull(partial, "Partial was null.");

        List<String> result = new ArrayList<>();
        partial = partial.toLowerCase();

        for (String name : getCachedServers()) {
            if (name.toLowerCase().startsWith(partial)) {
                result.add(name);
            }
        }

        return result;
    }

    /**
     * Publishes a message built from string message pieces joined by
     * the "/\" (forward-slash, back-slash) to Redis.
     *
     * @param destination   Server to send message to.
     * @param channel       Channel of the message.
     * @param messagePieces The parts of the message.
     */
    public void publish(String destination, String channel, String... messagePieces) {
        String joinedMessage = String.join("/\\", messagePieces);

        publish(destination, channel, joinedMessage);
    }

    /**
     * Publishes a message to Redis.
     *
     * @param destServer Server to send message to.
     * @param channel    Channel of the message.
     * @param message    The actual message.
     */
    public void publish(String destServer, String channel, String message) {
        Preconditions.checkNotNull(destServer, "DestServer was null.");
        Preconditions.checkNotNull(channel, "Channel was null.");
        Preconditions.checkNotNull(message, "Message was null.");

        if (plugin.getServerName().equals(destServer)) {
            plugin.onRedisMessageEvent(destServer, channel, message);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(
                plugin,
                () -> deltaSender.publish(
                        destServer,
                        channel,
                        message));
    }
}
