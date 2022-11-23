package com.phil.bailong.lesson04;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.arm.context.Arm32RegisterContext;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.utils.Inspector;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Lesson04Activity extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private Lesson04Activity() {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .setProcessName("com.mfw.roadbook")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/bailong/lesson04/mafengwo_ziyouxing.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/bailong/lesson04/libmfw.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Lesson04Activity lesson04Activity = new Lesson04Activity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");
        lesson04Activity.hookByConsoleDebugger();
//        lesson04Activity.hookXPreAuthencode();
//        lesson04Activity.hookSha1Encrypt();
//        lesson04Activity.xPreAuthencode();

//        lesson04Activity.traceCode();
        lesson04Activity.hook2F518();
        lesson04Activity.xAuthencode();
    }

    public void traceCode() {
        String traceFile = "unidbg-android/src/test/java/com/phil/bailong/lesson04/trace.txt";
        PrintStream traceStream = null;
        try {
            traceStream = new PrintStream(new FileOutputStream(traceFile), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);
    }

    public void hookByConsoleDebugger() {
        Debugger debugger = emulator.attach();
        debugger.addBreakPoint(module.base + 0x2F7F0);
    }

    public void hookXPreAuthencode() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        hookZz.wrap(module.base + 0x312E0 + 1, new WrapCallback<HookZzArm32RegisterContext>() {
            @Override
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                UnidbgPointer input = ctx.getPointerArg(0);
                byte[] inputHex = input.getByteArray(0, ctx.getR2Int());
                Inspector.inspect(inputHex, "input");

                UnidbgPointer buffer = ctx.getPointerArg(1);
                ctx.push(buffer);
            }

            @Override
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                UnidbgPointer buffer = ctx.pop();
                Inspector.inspect(buffer.getByteArray(0, 128), "out");
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }

    public void hookSha1Encrypt() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        hookZz.wrap(module.base + 0x3151C + 1, new WrapCallback<HookZzArm32RegisterContext>() {
            @Override
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                Pointer input = ctx.getPointerArg(0);
                byte[] inputhex = input.getByteArray(0, 20);
                Inspector.inspect(inputhex, "IV");

                Pointer text = ctx.getPointerArg(1);
                byte[] texthex = text.getByteArray(0, 64);
                Inspector.inspect(texthex, "block");
                ctx.push(input);
                ctx.push(text);
            }

            @Override
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                Pointer text = ctx.pop();
                Pointer IV = ctx.pop();

                byte[] IVhex = IV.getByteArray(0, 20);
                Inspector.inspect(IVhex, "IV");

                byte[] outputhex = text.getByteArray(0, 64);
                Inspector.inspect(outputhex, "block out");
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }

    private void xPreAuthencode() {
        List<Object> args = new ArrayList<>();
        args.add(vm.getJNIEnv());
        args.add(vm.addLocalObject(vm.resolveClass("com.mfw.roadbook").newObject(null)));
        args.add(vm.addLocalObject(vm.resolveClass("android/content/Context").newObject(null)));
        args.add(vm.addLocalObject(new StringObject(vm, "r0ysue")));
        args.add(vm.addLocalObject(new StringObject(vm, "com.mfw.roadbook")));
        Number number = module.callFunction(emulator, 0x2e301, args.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println(result);
    }

    private void xAuthencode() {
        List<Object> args = new ArrayList<>();
        args.add(vm.getJNIEnv());
        args.add(vm.addLocalObject(vm.resolveClass("com.mfw.roadbook").newObject(null)));
        args.add(vm.addLocalObject(vm.resolveClass("android/content/Context").newObject(null)));
        args.add(vm.addLocalObject(new StringObject(vm, "input1")));
        args.add(vm.addLocalObject(new StringObject(vm, "")));
        args.add(vm.addLocalObject(new StringObject(vm, "com.mfw.roadbook")));
        args.add(0);
        Number number = module.callFunction(emulator, 0x2e3b9, args.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println(result);
    }

    public void hook2F518() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        hookZz.wrap(module.base + 0x2F518 + 1, new WrapCallback<HookZzArm32RegisterContext>() {
            @Override
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                UnidbgPointer buffer = ctx.getPointerArg(0);
                byte[] bufferHex = buffer.getByteArray(0, 208);
                Inspector.inspect(bufferHex, "input0");
                Inspector.inspect(ctx.getPointerArg(1).getByteArray(0, ctx.getPointerArg(2).toIntPeer()), "input1");
            }

            @Override
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }
}
