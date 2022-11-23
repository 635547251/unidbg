package com.phil.bailong.lesson01;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.pointer.UnidbgPointer;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Lesson01Activity extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private Lesson01Activity() {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .setProcessName("com.sina.oasis")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/bailong/lesson01/lvzhou.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/bailong/lesson01/liboasiscore.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Lesson01Activity lesson01Activity = new Lesson01Activity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");

        lesson01Activity.hookMd5();
        lesson01Activity.s();
    }

    public void hookMd5() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        hookZz.wrap(module.base + 0x8AB2 + 1, new WrapCallback<HookZzArm32RegisterContext>() {
            @Override
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                UnidbgPointer arg0 = ctx.getPointerArg(0);
                UnidbgPointer arg1 = ctx.getPointerArg(1);
                int arg2 = ctx.getIntArg(2);
                System.out.println("参数1：" + arg0.getString(0));
                System.out.println("参数2：" + arg1.getString(0));
                System.out.println("参数3：" + arg2);
            }

            @Override
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }

    private void s() {
        Pointer jniEnv = vm.getJNIEnv();
        DvmObject<?> dvmObject = vm.resolveClass("com.sina.oasis").newObject(null);
        List<Object> args = new ArrayList<>();
        args.add(jniEnv);
        args.add(vm.addLocalObject(dvmObject));
        args.add(vm.addLocalObject(new ByteArray(vm, "aid=01A-khBWIm48A079Pz_DMW6PyZR8uyTumcCNm4e8awxyC2ANU.&cfrom=28B5295010&cuid=5999578300&noncestr=46274W9279Hr1X49A5X058z7ZVz024&platform=ANDROID&timestamp=1621437643609&ua=Xiaomi-MIX2S__oasis__3.5.8__Android__Android10&version=3.5.8&vid=1019013594003&wm=20004_90024".getBytes(StandardCharsets.UTF_8))));
        args.add(0);
        Number number = module.callFunction(emulator, 0xc365, args.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println(result);
    }
}
