package com.madao.joypad;

public class MsgPing implements IMsg {

    @Override
    public MsgType getType() {
        return MsgType.Ping;
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }
}
