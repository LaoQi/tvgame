package com.madao.joypad;

import java.util.logging.Level;

public class Logger {

    public interface ILogger {
        void d(String message);
        void i(String message);
        void w(String message);
        void e(String message, Throwable throwable);
    }

    private static Logger logger = null;
    private ILogger iLogger;

    private static Logger getInstance() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    private Logger() {
        iLogger = new ILogger() {
            @Override
            public void d(String message) {
                java.util.logging.Logger.getGlobal().log(Level.INFO, message);
            }

            @Override
            public void i(String message) {
                java.util.logging.Logger.getGlobal().log(Level.INFO, message);
            }

            @Override
            public void w(String message) {
                java.util.logging.Logger.getGlobal().log(Level.WARNING, message);
            }

            @Override
            public void e(String message, Throwable throwable) {
                java.util.logging.Logger.getGlobal().log(Level.SEVERE, message, throwable);
            }
        };
    }

    static void setLogger(ILogger iLogger) {
        getInstance().iLogger = iLogger;
    }

    static void i(Object message) {
        getInstance().iLogger.i(message.toString());
    }

    static void d(Object message) {
        getInstance().iLogger.d(message.toString());
    }

    static void w(Object message) {
        getInstance().iLogger.w(message.toString());
    }

    static void e(Object message, Throwable throwable) {
        getInstance().iLogger.e(message.toString(), throwable);
    }
}
