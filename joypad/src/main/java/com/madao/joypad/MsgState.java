package com.madao.joypad;

public class MsgState implements IMsg {
    @Override
    public MsgType getType() {
        return MsgType.State;
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }
}
