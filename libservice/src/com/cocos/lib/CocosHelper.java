package com.cocos.lib;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Cocos Helper - 提供游戏线程调度能力
 * 编译期占位实现，运行时应由 Cocos 引擎提供
 */
public final class CocosHelper {

    private static final Executor GAME_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();

    private CocosHelper() {
    }

    /**
     * 在游戏线程执行任务
     */
    public static void runOnGameThread(Runnable runnable) {
        GAME_THREAD_EXECUTOR.execute(runnable);
    }
}

