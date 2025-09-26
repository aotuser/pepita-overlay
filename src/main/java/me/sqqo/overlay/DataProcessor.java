package me.sqqo.overlay;

import net.minecraft.entity.player.EntityPlayer;
import okhttp3.*;
import okhttp3.internal.annotations.EverythingIsNonNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataProcessor {
    private static final OkHttpClient client = new OkHttpClient();

    private final CallBack<EntityPlayer, String> callBack;

    private final long ticksDelay;

    public long currDelay = 0;

    private final ConcurrentLinkedQueue<EntityPlayer> dataQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<EntityPlayer> waitingQueue = new ConcurrentLinkedQueue<>();

    public DataProcessor(final CallBack<EntityPlayer, String> callBack, final long ticksDelay) {
        this.ticksDelay = ticksDelay;
        this.callBack = callBack;

        new Thread(this::processData).start();
    }

    private void processData() {
        while (true) {
            EntityPlayer curr = null;

            if (!dataQueue.isEmpty() && currDelay <= 0) {
                curr = dataQueue.remove();
            }

            if (curr == null) {
                continue;
            }

            sendRequest(curr, this.callBack);

            currDelay = ticksDelay;
        }
    }

    private void sendRequest(EntityPlayer en, CallBack<EntityPlayer, String> callBack) {
        final Request request = new Request.Builder().url("https://mush.com.br/api/player/" + en.getName()).build();
        final CompletableFuture<Void> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, IOException e) {
            }

            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) {
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
