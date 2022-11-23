package com.phil.shumei;

import com.github.unidbg.Emulator;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.context.EditableArm32RegisterContext;
import com.github.unidbg.arm.context.EditableArm64RegisterContext;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.linux.ARM32SyscallHandler;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.file.ByteArrayFileIO;
import com.github.unidbg.linux.file.DumpFileIO;
import com.github.unidbg.memory.SvcMemory;
import com.github.unidbg.pointer.UnidbgPointer;
import com.sun.jna.Pointer;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class MyARMSyscallHandler extends ARM32SyscallHandler {
    public MyARMSyscallHandler(SvcMemory svcMemory) {
        super(svcMemory);
    }

    @Override
    public void hook(Backend backend, int intno, int swi, Object user) {
//        System.out.println("[syscall] NR = " + backend.reg_read(Arm64Const.UC_ARM64_REG_X8).intValue());
        super.hook(backend, intno, swi, user);
    }

    @Override
    protected boolean handleUnknownSyscall(Emulator<?> emulator, int NR) {
        System.out.println("[unknownSyscall] NR = " + NR);
        if (NR==190){
            vfork(emulator);
            return true;
        }
        return super.handleUnknownSyscall(emulator, NR);
    }

    private void vfork(Emulator<?> emulator) {
//        emulator.getUnwinder().unwind();
        EditableArm32RegisterContext context = emulator.getContext();
        int childPid = emulator.getPid() + ThreadLocalRandom.current().nextInt(256);
        context.setR0(childPid);
    }

    @Override
    protected int pipe2(Emulator<?> emulator) {
        EditableArm32RegisterContext context = emulator.getContext();
        UnidbgPointer pipeFd = context.getPointerArg(0);
        int flags = context.getIntArg(1);
        int write = getMinFd();
        this.fdMap.put(write, new DumpFileIO(write));

        int read = getMinFd();
        String stdout = "9ab5f193-ca2a-4d7e-8dc2-09e0ff1f257f\n";
        this.fdMap.put(read, new ByteArrayFileIO(0, "pipe2_read_side", stdout.getBytes(StandardCharsets.UTF_8)));
        pipeFd.setInt(0, read);
        pipeFd.setInt(4, write);
        System.out.println("pipe2 pipeFd=" + pipeFd + ", flags=0x" + flags + ", read=" + read + ", write=" + write + ", stdout=" + stdout);
        context.setR0(0);
        return 0;
    }
}
