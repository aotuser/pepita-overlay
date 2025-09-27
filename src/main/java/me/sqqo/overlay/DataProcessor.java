package me.sqqo.overlay;

import me.sqqo.Pepita;
import net.minecraft.entity.player.EntityPlayer;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataProcessor {
    private static final OkHttpClient client = new OkHttpClient();

    private final CallBack<EntityPlayer, String> callBack;

    public long currDelay = 0;

    private final ConcurrentLinkedQueue<EntityPlayer> dataQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<EntityPlayer> waitingQueue = new ConcurrentLinkedQueue<>();

    public DataProcessor(final CallBack<EntityPlayer, String> callBack, final long ticksDelay) {
        this.callBack = callBack;

        new Thread(this::processData).start();
    }

    private void processData() {
        while (true) {
            EntityPlayer curr = null;

            if (currDelay <= 0) {
                curr = dataQueue.poll();
            }

            if (curr == null) {
                continue;
            }

            sendRequest(curr, this.callBack);

            currDelay = Pepita.config.getRequestsDelay();
        }
    }

    private void sendRequest(EntityPlayer en, CallBack<EntityPlayer, String> callBack) {
        final Request request = new Request.Builder().url("https://mush.com.br/api/player/" + en.getName()).build();
        final CompletableFuture<Void> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.body() != null) {
                    try {
                        String data = response.body().string();
                        callBack.accept(en, data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                future.complete(null);
            }
        });
    }

    public ConcurrentLinkedQueue<EntityPlayer> getDataQueue() {
        return this.dataQueue;
    }

    public ConcurrentLinkedQueue<EntityPlayer> getWaitingQueue() {
        return this.waitingQueue;
    }

    @FunctionalInterface
    public interface CallBack<T, V> {
        void accept(T t, V v);
    }
}
