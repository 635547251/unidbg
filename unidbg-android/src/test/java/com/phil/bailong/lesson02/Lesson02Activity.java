package com.phil.bailong.lesson02;

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
import com.github.unidbg.memory.Memory;
import com.github.unidbg.pointer.UnidbgPointer;
import com.sun.jna.Pointer;
import keystone.Keystone;
import keystone.KeystoneArchitecture;
import keystone.KeystoneEncoded;
import keystone.KeystoneMode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Lesson02Activity extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private Lesson02Activity() {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .setProcessName("com.sina.oasis")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/bailong/lesson02/sinaInternational.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/bailong/lesson02/libutility.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Lesson02Activity lesson02Activity = new Lesson02Activity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");

        lesson02Activity.patch2();
        lesson02Activity.hookMDStringOld();
        lesson02Activity.calculateS();
    }

    public void patch() {
        int patchCode = 0x4FF00100;
        emulator.getMemory().pointer(module.base + 0x1E86).setInt(0, patchCode);
    }

    private void patch2() {
        long patchAddr = module.base + 0x1E86;
        Keystone keystone = new Keystone(KeystoneArchitecture.Arm, KeystoneMode.ArmThumb);
        KeystoneEncoded assemble = keystone.assemble("mov r0, 1");
        byte[] machineCode = assemble.getMachineCode();
        UnidbgPointer.pointer(emulator, patchAddr).write(0, machineCode, 0, machineCode.length);
    }

    public void hookMDStringOld() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        hookZz.wrap(module.base + 0x1BD0 + 1, new WrapCallback<HookZzArm32RegisterContext>() {
            @Override
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                UnidbgPointer arg0 = ctx.getPointerArg(0);
                System.out.println("参数1：" + arg0.getString(0));
            }

            @Override
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                UnidbgPointer arg0 = ctx.getPointerArg(0);
                System.out.println("返回：" + arg0.getString(0));
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }

    private void calculateS() {
        Pointer jniEnv = vm.getJNIEnv();
        List<Object> args = new ArrayList<>();
        args.add(jniEnv);
        args.add(vm.addLocalObject(vm.resolveClass("com.sina.oasis").newObject(null)));
        args.add(vm.addLocalObject(vm.resolveClass("android/content/Context").newObject(null)));
        args.add(vm.addLocalObject(new StringObject(vm, "12345")));
        args.add(vm.addLocalObject(new StringObject(vm, "r0ysue")));
        Number number = module.callFunction(emulator, 0x1E7C + 1, args.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println(result);
    }
}
