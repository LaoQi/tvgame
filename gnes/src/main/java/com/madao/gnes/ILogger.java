package com.madao.gnes;

public interface ILogger {
    void w(String tag, String message);
    void i(String tag, String message);
    void d(String tag, String message);
    void e(String tag, String message);
}
