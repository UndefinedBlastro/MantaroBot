package net.kodehawa.mantarobot.utils.exporters;

import io.prometheus.client.Gauge;
import net.kodehawa.mantarobot.MantaroBot;

import java.util.concurrent.TimeUnit;

public class DiscordLatencyExports {
    private static final Gauge GATEWAY_LATENCY = Gauge.build()
            .name("mantaro_shard_latency")
            .help("Gateway latency in seconds, per shard")
            .labelNames("shard")
            .create();
    private static final Gauge REST_LATENCY = Gauge.build()
            .name("mantaro_rest_latency")
            .help("Rest latency in seconds")
            .create();

    public static void register() {
        GATEWAY_LATENCY.register();
        REST_LATENCY.register();

        MantaroBot.getInstance().getExecutorService().scheduleAtFixedRate(() -> {
            var shards = MantaroBot.getInstance().getShardManager().getShardCache();
            shards.forEach(s -> {
                var ping = s.getGatewayPing();
                if(ping >= 0) {
                    GATEWAY_LATENCY.labels(String.valueOf(s.getShardInfo().getShardId()))
                            .set(ping);
                }
            });
            shards.iterator().next().getRestPing().queue(REST_LATENCY::set);
        }, 0, 3, TimeUnit.SECONDS);
    }
}
