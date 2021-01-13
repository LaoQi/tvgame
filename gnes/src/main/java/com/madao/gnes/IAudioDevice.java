package com.madao.gnes;

public interface IAudioDevice {
    void writeSamples1(float[] samples, int offset, int numSamples);
    void writeSamples2(float[] samples, int offset, int numSamples);
}
