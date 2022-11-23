package com.phil.sougou;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.arm.HookStatus;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.CodeHook;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.arm.backend.UnHook;
import com.github.unidbg.arm.backend.unicorn.Unicorn;
import com.github.unidbg.arm.context.Arm32RegisterContext;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.hook.HookContext;
import com.github.unidbg.hook.ReplaceCallback;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.utils.Inspector;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import unicorn.ArmConst;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static unicorn.ArmConst.UC_ARM_REG_R0;

public class EncryptActivity extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private EncryptActivity() {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .setProcessName("com.sina.oasis")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/sougou/sougou.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/sougou/libSCoreTools.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);

        emulator.traceWrite(module.base + 0x3A0C0, module.base + 0x3A0C0);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        EncryptActivity encryptActivity = new EncryptActivity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");
//        encryptActivity.hookEncryptWallEncode();
//        encryptActivity.inlineHookEncryptWallEncode();
//        encryptActivity.hookByUnicorn();
        encryptActivity.hookByConsoleDebugger();
        encryptActivity.hookLrand48();
        encryptActivity.init();
        encryptActivity.encrypt();
    }

    public void hookByConsoleDebugger() {
        Debugger debugger = emulator.attach();
        // hook j_Sc_EncryptWallEncode执行前后
//        debugger.addBreakPoint(module.base+0x9d24);
//        debugger.addBreakPoint(module.base+0x9d28);
//
//        // hook 0xa27a 此处修改了"开关"
//        debugger.addBreakPoint(module.base+0xA27A);

        // hook EncryptHttpRequest3
//        debugger.addBreakPoint(module.base + 0xB300);

        // hook rsa
//        debugger.addBreakPoint(module.base + 0xB34E);
//        debugger.addBreakPoint(module.base + 0xB352);

//        debugger.addBreakPoint(module.base + 0xB38C);
    }

    private void encrypt() {
        Pointer jniEnv = vm.getJNIEnv();
        DvmObject<?> dvmObject = vm.resolveClass("com.sogou.scoretools").newObject(null);
        List<Object> args = new ArrayList<>();
        args.add(jniEnv);
        args.add(vm.addLocalObject(dvmObject));
        String str = "http://app.weixin.sogou.com/api/searchapp";
        String str2 = "type=2&ie=utf8&page=1&query=%E5%A5%8B%E9%A3%9E%E5%AE%89%E5%85%A8&select_count=1&tsn=1&usip=";
        String str3 = "lilac";
        args.add(vm.addLocalObject(new StringObject(vm, str)));
        args.add(vm.addLocalObject(new StringObject(vm, str2)));
        args.add(vm.addLocalObject(new StringObject(vm, str3)));
        Number number = module.callFunction(emulator, 0x9ca1, args.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        // k=VGbQ3uWhgn6lExyMw+eNtU28iwE8zSWjL3QhU5d4PmIUrX1Qxyik5F7CxY7y/IkGwqsgNP6R7tkLEnk03rwUfRBt2UCw0egZdywdRBUBylBP9eJg1U7zxkx94flqZixYzZ846/h2hfEm+4HJf8Y5DANsDCq0pBMJaM/bLst8E5g=
        // &v=UjGgHmFGhBy67wE03NkwDQ==
        // &u=RenKbdTgxtv3pU05o0jl+XSUjjsti31W/VEbzsAF0xnHMi7Fto2tgbdyqdISxwfv
        // &r=DYWEWJCorJ13GZbTfGyQvSaSpTuJ7OLawG7oK3BPhsE=
        // &g=XCxe9d6KvuYk0mh+Q377gSXzU8Scbzua45fLShX6FlFwl+OCUNVn4UoKb9tiEbxX145CGuXwcNo3f7tG5fq9Kz+0Xmcv0oVjvWqkr7xnY3grtnnGtovgWFiIrpeTzNqG
        // &p=YbG7SH41/c380LbR9SKIDA==
        System.out.println(result);

    }

    private void init() {
        Pointer jniEnv = vm.getJNIEnv();
        DvmObject<?> dvmObject = vm.resolveClass("com.sogou.scoretools").newObject(null);
        List<Object> args = new ArrayList<>();
        args.add(jniEnv);
        args.add(vm.addLocalObject(dvmObject));
        args.add(vm.addLocalObject(vm.resolveClass("android/content/Context").newObject(null)));
        module.callFunction(emulator, 0x9565, args.toArray());
    }


    public void hookLrand48() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        hookZz.replace(module.findSymbolByName("lrand48").getAddress(), new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
//                System.out.println(context.getIntArg(0));
                return super.onCall(emulator, context, originFunction);
            }
        });
    }


    public void hookEncryptWallEncode() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        hookZz.wrap(module.base + 0xA284 + 1, new WrapCallback<HookZzArm32RegisterContext>() {
            Pointer buffer;

            @Override
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                Pointer arg0 = ctx.getPointerArg(0);
                Pointer arg1 = ctx.getPointerArg(1);
                Pointer arg2 = ctx.getPointerArg(2);
                buffer = ctx.getPointerArg(3);
                System.out.println("参数1：" + arg0.getString(0));
                System.out.println("参数2：" + arg1.getString(0));
                System.out.println("参数3：" + arg2.getString(0));
            }

            ;

            @Override
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                byte[] outputhex = buffer.getByteArray(0, 0x100);
                Inspector.inspect(outputhex, "EncryptWallEncode output");
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }


    public void inlineHookEncryptWallEncode() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.enable_arm_arm64_b_branch();
        final Pointer[] buffer = new Pointer[1];
        hookZz.instrument(module.base + 0x9d24 + 1, new InstrumentCallback<Arm32RegisterContext>() {
            @Override
            public void dbiCall(Emulator<?> emulator, Arm32RegisterContext ctx, HookEntryInfo info) {
                System.out.println("HookZz inline hook EncryptWallEncode");
                Pointer input1 = ctx.getPointerArg(0);
                Pointer input2 = ctx.getPointerArg(1);
                Pointer input3 = ctx.getPointerArg(2);
                System.out.println("参数1：" + input1.getString(0));
                System.out.println("参数2：" + input2.getString(0));
                System.out.println("参数3：" + input3.getString(0));
                buffer[0] = ctx.getPointerArg(3);
            }
        });
        hookZz.instrument(module.base + 0x9d28 + 1, new InstrumentCallback<Arm32RegisterContext>() {
            @Override
            public void dbiCall(Emulator<?> emulator, Arm32RegisterContext ctx, HookEntryInfo info) {
                Inspector.inspect(buffer[0].getByteArray(0, 0x100), "inline hook EncryptWallEncode");
            }
        });
    }


    public void hookByUnicorn() {
        emulator.getBackend().hook_add_new(new CodeHook() {
            Pointer buffer;

            @Override
            public void onAttach(UnHook unHook) {

            }

            @Override
            public void detach() {
            }

            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                // 原生Unicorn API进行Hook时，不需要管thumb arm的地址转换，即不需要考虑+1
                if (address == (module.base + 0x9d24)) {
                    System.out.println("Hook By Unicorn");
                    RegisterContext ctx = emulator.getContext();
                    Pointer input1 = ctx.getPointerArg(0);
                    Pointer input2 = ctx.getPointerArg(1);
                    Pointer input3 = ctx.getPointerArg(2);
                    System.out.println("参数1：" + input1.getString(0));
                    System.out.println("参数2：" + input2.getString(0));
                    System.out.println("参数3：" + input3.getString(0));
                    buffer = ctx.getPointerArg(3);
                }
                if (address == (module.base + 0x9d28)) {
                    Inspector.inspect(buffer.getByteArray(0, 0x100), "Unicorn hook EncryptWallEncode");
                }
            }
        }, module.base + 0x9d24, module.base + 0x9d28, null);
    }
}
