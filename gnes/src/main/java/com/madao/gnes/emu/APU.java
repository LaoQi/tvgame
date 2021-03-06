package com.madao.gnes.emu;

import com.madao.gnes.IAudioDevice;

/**
 * Created by ghost_000 on 8/1/2016.
 */
public class APU {
    // Classes, variables, etc.
    // Universal registers
    private int frameStep;
    // Flags
    private final boolean[] channelEnable = new boolean[5];    // 0: Pulse1, 1: Pulse2, 2: Triangle, 3: Noise, 4: DMC
//    private boolean DMCActive;
    private boolean fcMode;             // False: 4-step, True: 5-step
    private boolean IRQInhibit;

    // Internal
    private int APUCycleCount;
    private int CPUCycleCount;
    private boolean oddCPUcycle;
    private boolean frameIRQInterrupt;
    private final WaveChannel[] channels;
    private final DMC DMCChannel;

    private float[] squareTable;   // square out = [square1 + square2]
    private float[] tndTable;      // TND Out = [3*triangle + 2*noise + dmc]
    private final float[] soundBuffer1 = new float[256];
    private final float[] soundBuffer2 = new float[256];
    private int APUBufferCount;
    IAudioDevice IAudioDevice;


    // Length table constant
    public final static int[] lengthTable = {
            10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14,
            12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30};


    public APU(Cartridge cartridge, IAudioDevice iAudioDevice){
        // Constructor
        // Init waveChannels
        channels = new WaveChannel[5];
        channels[0] = new SquareWave();
        channels[1] = new SquareWave();
        channels[2] = new TriangleWave();
        channels[3] = new NoiseWave();
        DMCChannel = new DMC(cartridge);
        channels[4] = DMCChannel;

        generateTables();
        IAudioDevice = iAudioDevice;
    }
    public int receiveData(int address){
        int returnData = 0;
        if (address == 0x4015){
            // Return status bits
            for (int i = 0; i < 5; i++){
                if (channels[i] != null){
                    returnData |= (channels[i].lengthAboveZero() ? 1:0) << i;
                }
            }
            returnData |= ((frameIRQInterrupt ? 1:0) << 6);
            returnData |= ((DMCChannel.checkIRQ() ? 1:0) << 7);
            frameIRQInterrupt = false;
        }
        else {
            System.err.printf("Invalid APU read address 0x%x. Figure out why.\n", address);
        }

        return returnData;
    }

    public void writeData(int address, int data){
        if (address < 0x4015){
            int channelNum = (address >> 2) & 0xF;
            if (channels[channelNum] != null){
                channels[channelNum].writeData(address, data);
            }
        }
        else if (address == 0x4015){
            // Enable bits
            // Serially
            for (int i = 0; i < 5; i++){
                channelEnable[i] = (data & 0x1) == 1;
                if (channels[i] != null){
                    channels[i].enabled(channelEnable[i]);
                }
                data >>= 1;
            }
        }
        else if (address == 0x4017){
            // Frame counter registers
            IRQInhibit = (data & 0x40) == 0x40;
            fcMode = (data & 0x80) == 0x80;
            frameStep = 0;
            APUCycleCount = 0;
            if (fcMode){
                quarterTick();
                halfTick();
            }
            if (IRQInhibit){
                frameIRQInterrupt = false;   // Kill IRQs if on
            }
        }
        else {
            System.err.printf("Invalid APU write address 0x%x. Figure out why.\n", address);
        }
    }

    public void step(int CPUcycles){
        // Step
        // 2 CPU Cycles = 1 APU cycle
        while (CPUcycles-- > 0){
            oddCPUcycle = !oddCPUcycle;
            CPUCycleCount++;

            if (oddCPUcycle){
                // This is the second CPU Cycle, so perform APU functions
                // Increment counter
                APUCycleCount++;
                // Tick all channels
                for (int i = 0; i < 5; i++){
                    // Don't tick triangle (channel 3) here.
                    if (channels[i] != null){
                        channels[i].tick();
                    }
                }
            }
            else{
                // Triangle wave is ticked every cycle, so tick again.
                if(channels[2] != null){
                    channels[2].tick();
                }
                // Maybe DMC as well?
                if (channels[4] != null){
                    channels[4].tick();
                }
            }

            // Frame stepper
            // This happens between APU cycles apparently?
            // Calculating against CPU cycles
            if ((CPUCycleCount % 7457) == 0){
                // Step
                frameStep++;
                // 4-step
                if (!fcMode) {
                    // 120 Hz stuff
                    if ((frameStep % 2) == 0) {
                        halfTick();
                    }
                    // 240 hz stuff
                    quarterTick();

                    // 60 Hz reset
                    // TODO: Constant IRQ interrupts after this step?
                    if (frameStep == 4) {
                        if (!IRQInhibit) {
                            frameIRQInterrupt = true;
                        }
                        frameStep = 0;
                        APUCycleCount = 0;
                        CPUCycleCount = 0;
                    }
                }

                // 5-step
                else{
                    // TODO: Improve this?
                    switch (frameStep){
                        case 1:
                            quarterTick();
                            break;
                        case 2:
                            quarterTick();
                            halfTick();
                            break;
                        case 3:
                            quarterTick();
                            break;
                        case 4:
                            break;
                        case 5:
                            quarterTick();
                            halfTick();
                            frameStep = 0;
                            APUCycleCount = 0;
                            break;

                    }
                }
            }

            // Get output buffer, play it when filled with 256
            if (APUBufferCount/40 >= soundBuffer1.length){
                APUBufferCount = 0;
                IAudioDevice.writeSamples1(soundBuffer1, 0, soundBuffer1.length);
                IAudioDevice.writeSamples2(soundBuffer2, 0, soundBuffer2.length);
            }
            if (APUBufferCount % 40 == 0) {
                float squareOutputVal = squareTable[channels[0].getOutputVol() + channels[1].getOutputVol()];
                soundBuffer1[APUBufferCount / 40] = squareOutputVal;
                float tndOutputVal = tndTable[(3 * channels[2].getOutputVol()) + (2 * channels[3].getOutputVol())
                        + channels[4].getOutputVol()];
                soundBuffer2[APUBufferCount / 40] = tndOutputVal;
            }
            APUBufferCount++;
        }
    }

    public boolean checkIRQ(){
        //frameIRQInterrupt = false;
        //DMCIRQInterrupt = false;
        return DMCChannel.checkIRQ()||frameIRQInterrupt;
    }

    private void quarterTick(){
        for (int i = 0; i < 4; i++){
            if (channels[i] != null){
                channels[i].quarterFrameTick();
            }
        }
    }

    private void halfTick(){
        for (int i = 0; i < 4; i++) {
            if (channels[i] != null) {
                channels[i].halfFrameTick();
            }
        }
    }

    private void generateTables(){
        // Square table
        squareTable = new float[31];
        for (int i = 1; i < 31; i++){
            squareTable[i] = (float)(95.52/((8128/i)+100));
        }

        // TND Table
        tndTable = new float[203];
        for (int i = 1; i < 203; i++){
            tndTable[i] = (float)(163.67/((24329/i)+100));
        }
    }

    public int getCycleAdditions(){
        return DMCChannel.getCycleAdditions();
    }

    public int getAPUCycleCount() {
        return APUCycleCount;
    }
}