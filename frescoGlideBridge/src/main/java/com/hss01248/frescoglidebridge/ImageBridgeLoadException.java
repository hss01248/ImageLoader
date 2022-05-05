package com.hss01248.frescoglidebridge;

import java.io.IOException;

/**
 * @Despciption todo
 * @Author hss
 * @Date 05/05/2022 10:24
 * @Version 1.0
 */
public class ImageBridgeLoadException extends IOException {
    public ImageBridgeLoadException() {
    }

    public ImageBridgeLoadException(String message) {
        super(message);
    }

    public ImageBridgeLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageBridgeLoadException(Throwable cause) {
        super(cause);
    }
}
