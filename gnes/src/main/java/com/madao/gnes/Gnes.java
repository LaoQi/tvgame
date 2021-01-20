package com.madao.gnes;

import com.madao.gnes.emu.APU;
import com.madao.gnes.emu.CPU;
import com.madao.gnes.emu.CPU_MMU;
import com.madao.gnes.emu.Cartridge;
import com.madao.gnes.emu.Controller;
import com.madao.gnes.emu.PPU;
import com.madao.gnes.emu.PPU_MMU;

public class Gnes {
    // Emulator classes
    Cartridge NESCart;
    Controller NESController;
    APU NESAPU;
    PPU_MMU NESPPUMMU;
    PPU NESPPU;
    CPU_MMU NESMMU;
    CPU NESCPU;

    // Constructors
    public Gnes(byte[] rom, IAudioDevice audioDevice, IRender render){
        // If NESCart wasn't created, try loading some generic rom file
        if (NESCart == null) {
            NESCart = Cartridge.Create(rom);
        }
        NESAPU = new APU(NESCart, audioDevice);
        NESController = new Controller();
        NESPPUMMU = new PPU_MMU(NESCart);
        NESPPU = new PPU(NESPPUMMU, render);
        NESMMU = new CPU_MMU(NESCart, NESPPU, NESController, NESAPU);
        NESCPU = new CPU(NESMMU);
        NESCPU.resetNES();
    }

    public void tick() {
        while(!NESPPU.getNewVblank()){
            // Poll controller
            NESController.pollController();
            // Execute CPU
            NESCPU.execInst(NESPPU.NMITriggered(), NESCart.checkIRQ() || NESAPU.checkIRQ());
            // Get final CPU cycle count before triggering PPU step
            int cycles = NESCPU.getLastCycleCount() + NESMMU.getCycleAdditions() + NESAPU.getCycleAdditions();
            // Step the APU
            NESAPU.step(cycles);
            // Step the PPU
            NESPPU.step(cycles);
        }
    }

}