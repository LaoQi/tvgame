package com.madao.joypad;


public interface IMsg {
    enum MsgType {
        Ping,
        State
    }

    MsgType getType();

    byte[] serialize();
}
