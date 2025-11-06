package com.cocos.lib;

/**
 * Cocos Javascript Bridge - 提供JS调用能力
 * 编译期占位实现，运行时应由 Cocos 引擎提供
 */
public final class CocosJavascriptJavaBridge {

    private CocosJavascriptJavaBridge() {
    }

    /**
     * 执行JS脚本
     * @param script JS脚本字符串
     * @return 执行结果(0表示成功)
     * 注意:native方法,运行时由Cocos引擎libcocos.so提供真实实现
     */
    public static native int evalString(String script);
}

