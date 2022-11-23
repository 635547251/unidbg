package com.phil.dewu.gamevmp;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.HookStatus;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.hook.HookContext;
import com.github.unidbg.hook.ReplaceCallback;
import com.github.unidbg.hook.hookzz.Dobby;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.AndroidSyscallHandler;
import com.github.unidbg.linux.android.AndroidARM64Emulator;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.SystemPropertyHook;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.jni.ProxyDvmObject;
import com.github.unidbg.linux.file.RandomFileIO;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.SvcMemory;
import com.github.unidbg.unix.UnixSyscallHandler;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.phil.dewu.MyARMSyscallHandler;
import com.phil.dewu.MySystemPropertyProvider;
import com.phil.dewu.utils;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;


public class GameVMPActivity extends AbstractJni{
    private final AndroidEmulator androidEmulator;
    private final VM vm;
    private final Memory memory;
    private final Module module;

    public GameVMPActivity() {
        androidEmulator = AndroidEmulatorBuilder.for64Bit()
                .setProcessName("com.shizhuang.duapp")
                .setRootDir(new File("/tmp/rootfs/default"))
//                .addBackendFactory(new DynarmicFactory(true))
                .build();

        memory = androidEmulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));

        vm = androidEmulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/dewu/com.shizhuang.duapp_4.88.0.apk"));
        vm.setVerbose(true);
        vm.setJni(this);

        // 注册
        new AndroidModule(androidEmulator, vm).register(memory);

        DalvikModule dalvikModule = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/dewu/gamevmp/libdusanwa.so"), true);
        module = dalvikModule.getModule();

        vm.callJNI_OnLoad(androidEmulator, module);
    }

    // 设置debug日志
    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(AndroidSyscallHandler.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        GameVMPActivity szstoneActivity = new GameVMPActivity();
        System.out.println("load the vm " + (System.currentTimeMillis() - start) + "ms");
    }
}
