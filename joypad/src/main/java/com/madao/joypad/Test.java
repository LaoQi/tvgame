package com.madao.joypad;

import java.util.Random;

public class Test {
    private static Random rand = new Random(System.currentTimeMillis());
    private static final int NBR_WORKERS = 5;

    private static class Worker extends Thread
    {
        @Override
        public void run()
        {

        }
    }

    public static void main(String[] args) throws Exception
    {
        Logger.setLogger(new Logger.ILogger() {
            @Override
            public void d(String message) {
                System.out.println(message);
            }

            @Override
            public void i(String message) {
                System.out.println(message);
            }

            @Override
            public void w(String message) {
                System.out.println(message);
            }

            @Override
            public void e(String message, Throwable throwable) {
                System.out.println(message);
                throwable.printStackTrace();
            }
        });

        new JoyServer(5671, 2).start();
    }
}
