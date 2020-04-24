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
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Shared.SplitPatterns;
import io.lettuce.core.pubsub.RedisPubSubListener;

/**
 * Created by Trace Bachi (tracebachi@gmail.com) on 10/18/15.
 */
public class DRPubSubListener implements RedisPubSubListener<String, String>, Shutdownable {
    private DeltaRedisInterface plugin;

    public DRPubSubListener(DeltaRedisInterface plugin) {
        this.plugin = plugin;
    }

    @Override
    public void shutdown() {
        this.plugin = null;
    }

    /**
     * Called when a message is received by the RedisPubSub listener.
     * <p>
     * The received message is structured into 3 parts: serverName,
     * message channel, and the actual message. Those parts are used to
     * create and call a DeltaRedisMessageEvent.
     * </p>
     *
     * @param channel         Ignored as the listener is only registered to explicit channels.
     * @param completeMessage Complete received message.
     */
    public void message(String channel, String completeMessage) {
        String[] messageParts = SplitPatterns.DELTA.split(completeMessage, 3);

        if (messageParts.length == 3) {
            plugin.debug("Received message. {source: " + messageParts[0] +
                    " , channel: " + messageParts[1] +
                    " , message: " + messageParts[2] + "}");

            plugin.onRedisMessageEvent(messageParts[0], messageParts[1], messageParts[2]);
        } else {
            plugin.severe("Received badly formatted message in DRPubSubListener. " +
                    "{message: " + completeMessage + "}");
        }
    }

    /**
     * @param channel Channel that the listener was registered to.
     * @param count   Number of other listeners (on that Redis instance) on the channel.
     */
    public void subscribed(String channel, long count) {
        plugin.debug("Listener subscribed to {channel: " + channel + "}");
    }

    /**
     * @param channel Channel that the listener was unregistered from.
     * @param count   Number of other listeners (on that Redis instance) on the channel.
     */
    public void unsubscribed(String channel, long count) {
        plugin.debug("Listener unsubscribed from {channel: " + channel + "}");
    }

    public void message(String pattern, String channel, String message) {
    }

    public void psubscribed(String pattern, long count) {
    }

    public void punsubscribed(String pattern, long count) {
    }
}
