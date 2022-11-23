package com.phil.dewu;

import com.github.unidbg.Emulator;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.context.EditableArm64RegisterContext;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.file.ByteArrayFileIO;
import com.github.unidbg.linux.file.DumpFileIO;
import com.github.unidbg.memory.SvcMemory;
import com.sun.jna.Pointer;
import unicorn.Arm64Const;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class MyARMSyscallHandler extends ARM64SyscallHandler {
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
        return super.handleUnknownSyscall(emulator, NR);
    }

    @Override
    protected long fork(Emulator<?> emulator) {
        EditableArm64RegisterContext context = emulator.getContext();
        int childPid = emulator.getPid() + ThreadLocalRandom.current().nextInt(256);
        int r0 = 0;
        r0 = childPid;
        System.out.println("[vfork] pid = " + r0);
//        context.setR0(r0);
        return 1;
    }

    @Override
    protected int pipe2(Emulator<?> emulator) {
        EditableArm64RegisterContext context = emulator.getContext();
        Pointer pipefd = context.getPointerArg(0);
        int flags = context.getIntArg(1);
        String cmd = context.getPointerArg(12).getString(0, "utf-8");
        int write = getMinFd();
        this.fdMap.put(write, new DumpFileIO(write));
        int read = getMinFd();
        String stdout = "";
        switch (cmd) {
            // ndatamf
            case "stat /data":
                stdout = "Modify: 2022-04-28 13:43:26.664668331";
                break;
            // nrootmf
            case "stat /root":
                stdout = "Modify: 2020-01-29 09:51:17.000000000";
                break;
            // ncachemf
            case "stat /cache":
                stdout = "Modify: 2022-04-28 08:59:18.583725863";
                break;
            // randmf   nrandst
            case "stat /proc/sys/kernel/random":
            case "cd  /proc/sys/kernel/ && stat random":
                stdout = "  File: `random'\n" +
                        "  Size: 0        Blocks: 0       IO Blocks: 512 directory\n" +
                        "Device: 5h/5d    Inode: 78580    Links: 1\n" +
                        "Access: (555/dr-xr-xr-x)        Uid: (    0/    root)   Gid: (    0/    root)\n" +
                        "Access: 2022-05-03 21:33:30.492821801\n" +
                        "Modify: 2022-05-03 21:33:30.492821801\n" +
                        "Change: 2022-05-03 21:33:30.492821801";
                break;
            // nfirmmf
            case "stat /system/etc/firmware":
                stdout = "";
                break;
            // nmac
            case "cd /sys/class/net/wlan0/ && cat address":
                stdout = "";
                break;
            // nunamea
            case "uname -a":
                stdout = "Linux localhost 4.4.78-perf+ #1 SMP PREEMPT Fri Feb 22 15:54:43 CST 2019 aarch64";
                break;
            // nunamer
            case "uname -r":
                stdout = "4.4.78-perf+";
                break;
            // nbin
            case "cd /system/bin && ls -i":
                stdout = "435 hwservicemanager    544 realpath";
                break;
            // nbinl
            case "cd /system/bin && ls -l":
                stdout = "-rwxr-xr-x 1 root   shell     170848 2021-02-02 23:55 zip";
                break;
            // nfnid
            case "stat -c %i /system/framework/am.jar":
                stdout = "1469";
                break;
            case "stat -c %i /system/framework":
                stdout = "1465";
                break;
            case "stat -c %i /system/build.prop":
                stdout = "846";
                break;
            case "stat -c %i /system/fonts":
                stdout = "1290";
                break;
            case "stat -c %i /system/app":
                stdout = "11";
                break;
            case "stat -c %i /system":
                stdout = "2";
                break;
            // nfsid
            case "stat -c %d /data":
                stdout = "64512";
                break;
            case "stat -c %d /system":
                stdout = "66331";
                break;
            case "stat -f -c %i /data":
                stdout = "e467eabace9d1e85";
                break;
            // natid
            case "stat -c %X /storage/emulated/":
                stdout = "1650423984";
                break;
            case "stat -c %X /data":
                stdout = "1650423983";
                break;
        }
        this.fdMap.put(read, new ByteArrayFileIO(0, "pipe2_read_side", stdout.getBytes()));
        pipefd.setInt(0, read);
        pipefd.setInt(4, write);
        System.out.println("[pipe2] cmd = " + cmd + ", pipefd = " + pipefd + ", flags = 0x" + flags + ", read = " + read + ", write = " + write + ", stdout = " + stdout);
        return 1;
    }
}
