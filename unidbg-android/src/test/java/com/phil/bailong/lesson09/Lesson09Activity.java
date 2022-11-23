package com.phil.bailong.lesson09;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Lesson09Activity extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private Lesson09Activity() throws FileNotFoundException {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .setProcessName("com.bilibili.app")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);


        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/bailong/lesson09/小黑盒.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/bailong/lesson09/libnative-lib.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);

        // 填入自己的path
//        String traceFile = "unidbg-android/src/test/java/com/phil/bailong/lesson09/encode.txt";
//        PrintStream traceStream = new PrintStream(new FileOutputStream(traceFile), true);
//        emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);

        emulator.traceRead(0xbffff5e0L, 0xbffff5e0L + 20L);
        emulator.traceWrite(0xbffff5b8L, 0xbffff5b8L + 7L);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        Lesson09Activity lesson09Activity = new Lesson09Activity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");
        lesson09Activity.consoleDebugger();
        lesson09Activity.hook37a0();
        lesson09Activity.callEncode();
    }

    public void consoleDebugger() {
//        emulator.attach().addBreakPoint(module.base + 0x1DDC + 1);
//        emulator.attach().addBreakPoint(module.base + 0x3c3e + 1);
//        emulator.attach().addBreakPoint(module.base + 0x3c56 + 1);
        emulator.attach().addBreakPoint(module.base + 0x18F8 + 1);
    }


    public void hook37a0() {
        // 获取HookZz对象
        IHookZz hookZz = HookZz.getInstance(emulator); // 加载HookZz，支持inline hook，文档看https://github.com/jmpews/HookZz
        // enable hook
        hookZz.enable_arm_arm64_b_branch(); // 测试enable_arm_arm64_b_branch，可有可无
        // hook MDStringOld
        hookZz.wrap(module.base + 0x37a4 + 1, new WrapCallback<HookZzArm32RegisterContext>() { // inline wrap导出函数
            @Override
            // 方法执行前
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                System.out.println("input:" + ctx.getR0Int() + " " + ctx.getR1Int());
            }

            ;

            @Override
            // 方法执行后
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                System.out.println("output:" + ctx.getR0Int() + " " + ctx.getR1Int());
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }


    public void callEncode() {
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        Object custom = null;
        DvmObject<?> context = vm.resolveClass("android/content/Context").newObject(custom);// context
        list.add(vm.addLocalObject(context));
        list.add(vm.addLocalObject(new StringObject(vm, "r0env")));
        list.add(vm.addLocalObject(new StringObject(vm, "1622343722")));
        Number number = module.callFunction(emulator, 0x3b41, list.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println("result = " + result);
    }

    ;
}
