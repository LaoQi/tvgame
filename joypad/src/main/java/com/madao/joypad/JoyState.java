package com.madao.joypad;

import java.util.Hashtable;

class JoyState {

    private static class StateData {
        private byte[] bits;
        private int length;

        StateData(int length) {
            bits = new byte[length];
            this.length = length;
        }

        void setBits(byte[] data) {
            if (data.length == length) {
                bits = data;
            }
        }

        byte[] getBits() {
            return bits;
        }
    }

    private final Hashtable<String, StateData>states = new Hashtable<>();

    public JoyState() {

    }

    void getPlayer(String identity) {

    }
}
