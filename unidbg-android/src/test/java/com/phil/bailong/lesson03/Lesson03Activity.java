package com.phil.bailong.lesson03;

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
import com.github.unidbg.memory.MemoryBlock;
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.utils.Inspector;
import com.sun.jna.Pointer;
import keystone.Keystone;
import keystone.KeystoneArchitecture;
import keystone.KeystoneEncoded;
import keystone.KeystoneMode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Lesson03Activity extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private Lesson03Activity() {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .setProcessName("com.sina.oasis")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/bailong/lesson03/right573.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/bailong/lesson03/libnet_crypto.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Lesson03Activity lesson03Activity = new Lesson03Activity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");

        lesson03Activity.hookSign();
        lesson03Activity.native_init();
        lesson03Activity.sign();
        lesson03Activity.callMd5();
    }


    public void hookSign() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        hookZz.wrap(module.base + 0x65540 + 1, new WrapCallback<HookZzArm32RegisterContext>() {
            @Override
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                Inspector.inspect(ctx.getPointerArg(0).getByteArray(0, 128), "参数1");
                System.out.println("参数2：" + ctx.getIntArg(1));
                Inspector.inspect(ctx.getPointerArg(2).getByteArray(0, 128), "参数3");
                ctx.push(ctx.getPointerArg(2));
            }

            @Override
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                Pointer output = ctx.pop();
                Inspector.inspect(output.getByteArray(0, 128), "参数3");
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }

    private void native_init() {
        List<Object> args = new ArrayList<>();
        args.add(vm.getJNIEnv());
        args.add(vm.addLocalObject(vm.resolveClass("com.sina.oasis").newObject(null)));
        module.callFunction(emulator, 0x4a069, args.toArray());
    }

    private void sign() {
        List<Object> args = new ArrayList<>();
        args.add(vm.getJNIEnv());
        args.add(vm.addLocalObject(vm.resolveClass("com.sina.oasis").newObject(null)));
        args.add(vm.addLocalObject(new StringObject(vm, "12345")));
        args.add(vm.addLocalObject(new ByteArray(vm, "r0ysue".getBytes(StandardCharsets.UTF_8))));
        Number number = module.callFunction(emulator, 0x4a28d, args.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println(result);
    }

    private void callMd5() {
        // arg1
        String input = "r0ysue";
        MemoryBlock memoryBlock = emulator.getMemory().malloc(16, false);
        UnidbgPointer input_ptr = memoryBlock.getPointer();
        input_ptr.write(input.getBytes(StandardCharsets.UTF_8));

        // arg2
        int input_len = input.length();

        // arg3
        MemoryBlock memoryBlock2 = emulator.getMemory().malloc(16, false);
        UnidbgPointer output_buffer = memoryBlock2.getPointer();

        // call
        List<Object> args = new ArrayList<>();
        args.add(input_ptr);
        args.add(input_len);
        args.add(output_buffer);
        module.callFunction(emulator, 0x65540 + 1, args.toArray());

        // print arg3
        Inspector.inspect(output_buffer.getByteArray(0, 0x10), "output");
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        if (signature.equals("com/izuiyou/common/base/BaseApplication->getAppContext()Landroid/content/Context;")) {
            return vm.resolveClass("android/content/Context").newObject(null);
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "android/content/Context->getClass()Ljava/lang/Class;":
                return dvmObject.getObjectType();
            case "java/lang/Class->getSimpleName()Ljava/lang/String;":
                return new StringObject(vm, "AppController");
            case "android/content/Context->getFilesDir()Ljava/io/File;":
            case "java/lang/String->getAbsolutePath()Ljava/lang/String;":
                return new StringObject(vm, "/data/user/0/cn.xiaochuankeji.tieba/files");
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public boolean callStaticBooleanMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "android/os/Debug->isDebuggerConnected()Z":
                return false;
        }
        return super.callStaticBooleanMethodV(vm, dvmClass, signature, vaList);
    }

    @Override
    public int callStaticIntMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "android/os/Process->myPid()I":
                return emulator.getPid();
        }
        return super.callStaticIntMethodV(vm, dvmClass, signature, vaList);
    }
}
