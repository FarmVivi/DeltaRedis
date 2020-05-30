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
package com.gmail.tracebachi.DeltaRedis.Shared.Redis;

import com.gmail.tracebachi.DeltaRedis.Shared.DeltaRedisInterface;
import com.gmail.tracebachi.DeltaRedis.Shared.Servers;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.StatefulRedisConnection;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DRCommandSender implements Shutdownable {
    private final String serverName;
    private final String bungeeName;

    private StatefulRedisConnection<String, String> connection;
    private DeltaRedisInterface plugin;
    private boolean isBungeeCordOnline;
    private Set<String> cachedServers;

    public DRCommandSender(StatefulRedisConnection<String, String> connection,
                           DeltaRedisInterface plugin) {
        this.connection = connection;
        this.plugin = plugin;
        this.bungeeName = plugin.getBungeeName();
        this.serverName = plugin.getServerName();
    }

    /**
     * Adds server to Redis, making it visible to other servers/
     */
    public synchronized void setup() {
        plugin.debug("DRCommandSender.setup()");

        connection.sync().sadd(bungeeName + ":servers", serverName);
    }

    @Override
    public synchronized void shutdown() {
        plugin.debug("DRCommandSender.shutdown()");

        connection.sync().srem(bungeeName + ":servers", serverName);
        connection.close();
        connection = null;
        plugin = null;
    }

    /**
     * @return An unmodifiable set of servers that are part of the
     * same BungeeCord. This method will retrieve the servers from Redis.
     */
    public void refresh() {
        plugin.debug("DRCommandSender.getServers()");

        RedisFuture<Set<String>> result = connection.async().smembers(bungeeName + ":servers");

        result.whenComplete((strings, throwable) -> {
            isBungeeCordOnline = strings.remove(Servers.BUNGEECORD);
            cachedServers = Collections.unmodifiableSet(strings);
        });
    }

    /**
     * @return An unmodifiable set of servers that are part of the
     * same BungeeCord. This method will retrieve the servers from the last
     * call to {@link DRCommandSender#refresh()}.
     */
    public Set<String> getCachedServers() {
        return cachedServers;
    }

    /**
     * @return True if the BungeeCord instance was last known to be online.
     * False if it was not.
     */
    public boolean isBungeeCordOnline() {
        return isBungeeCordOnline;
    }

    /**
     * Publishes a string message using Redis PubSub. The destination can
     * also be one of the special values {@link Servers#BUNGEECORD}
     * or {@link Servers#SPIGOT}.
     *
     * @param dest    Server name that message should go to.
     * @param channel Custom channel name for the message.
     * @param message Message to send.
     * @return The number of servers that received the message.
     */
    public RedisFuture<Long> publishASync(String dest, String channel, String message) {
        plugin.debug("DRCommandSender.publish(" + dest + ", " + channel + ", " + message + ")");

        return connection.async().publish(
                bungeeName + ':' + dest,
                serverName + "/\\" + channel + "/\\" + message);
    }

    /**
     * Publishes a string message using Redis PubSub. The destination can
     * also be one of the special values {@link Servers#BUNGEECORD}
     * or {@link Servers#SPIGOT}.
     *
     * @param dest    Server name that message should go to.
     * @param channel Custom channel name for the message.
     * @param message Message to send.
     * @return The number of servers that received the message.
     */
    public Long publishSync(String dest, String channel, String message) {
        plugin.debug("DRCommandSender.publish(" + dest + ", " + channel + ", " + message + ")");

        return connection.sync().publish(
                bungeeName + ':' + dest,
                serverName + "/\\" + channel + "/\\" + message);
    }
}