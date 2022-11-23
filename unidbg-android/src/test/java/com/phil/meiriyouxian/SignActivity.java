package com.phil.meiriyouxian;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.CodeHook;
import com.github.unidbg.arm.backend.UnHook;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.ARM32SyscallHandler;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.AssetManager;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.utils.Inspector;
import com.github.unidbg.virtualmodule.android.AndroidModule;
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

public class SignActivity extends AbstractJni implements IOResolver<AndroidFileIO> {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private SignActivity() throws FileNotFoundException {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
//                .setProcessName("com.sina.oasis")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);
        emulator.getSyscallHandler().addIOResolver(this);
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/meiriyouxian/meiriyouxian9942.apk"));
        vm.setJni(this);
        vm.setVerbose(true);

        // 注册 libandroid.so
        new AndroidModule(emulator, vm).register(memory);

        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/meiriyouxian/libsign.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);

//        String traceFile = "unidbg-android/src/test/java/com/phil/meiriyouxian/sign.txt";
//        PrintStream traceStream = new PrintStream(new FileOutputStream(traceFile), true);
//        emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
//        Logger.getLogger(ARM32SyscallHandler.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        SignActivity signActivity = new SignActivity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");
        signActivity.ConsoleDebugger();
        signActivity.hook2f638yUnicorn();
        signActivity.init();
        signActivity.main();
    }


    public void ConsoleDebugger() {
//        // trace 结果来源(第一层)
//        emulator.traceWrite(0x402e7000, 0x402e7000 + 122);
//
//        // 118个字符串来源(第二层)
//        emulator.attach().addBreakPoint(module.base + 0x2F9EE);
//
//        // 魔改base64(第三层)
//        emulator.attach().addBreakPoint(module.base + 0x37F3C);

//         base64参数来源 不包括时间戳(第四层)
//        emulator.traceWrite(0x402e4000, 0x402e4052);

//        emulator.traceWrite(0x402a10f0, 0x402a10f0 + 0x4b);
        emulator.attach().addBreakPoint(module.base + 0x364BE);
    }

    private void hook2f638yUnicorn() {
        emulator.getBackend().hook_add_new(new CodeHook() {
            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                if (address == (module.base + 0x2f638)) {
                    System.out.println("Hook By Unicorn");
                    RegisterContext ctx = emulator.getContext();
                    Pointer r1 = ctx.getPointerArg(1);
                    int length = ctx.getIntArg(2);
                    System.out.println("source Address:0x" + Integer.toHexString(ctx.getPointerArg(1).toIntPeer()));
                    System.out.println("target Address:0x" + Integer.toHexString(ctx.getPointerArg(0).toIntPeer()));
                    Inspector.inspect(r1.getByteArray(0, length), "memcpy source");
                }
            }

            @Override
            public void onAttach(UnHook unHook) {

            }

            @Override
            public void detach() {

            }
        }, module.base + 0x2f638, module.base + 0x2f638, null);
    }


    private void init() {
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0);
        DvmObject<?> context =
                vm.resolveClass("cn/missfresh/application/MissFreshApplication",
                        vm.resolveClass("android/content/Context")).newObject(null);// context
        list.add(vm.addLocalObject(context));
        list.add(vm.addLocalObject(new StringObject(vm, "01000002")));
        module.callFunction(emulator, 0x38bb4 + 1, list.toArray());
    }

    private void main() {
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。 DvmObject<?> context =
        DvmObject<?> context = vm.resolveClass("cn/missfresh/application/MissFreshApplication",
                vm.resolveClass("android/content/Context")).newObject(null);// context
        list.add(vm.addLocalObject(context));
        list.add(null);
        list.add(0x17Ac4917cb5L);
        list.add(vm.addLocalObject(new
                ByteArray(vm, "version".getBytes(StandardCharsets.UTF_8))));
        Number number = module.callFunction(emulator, 0x38BF4 + 1, list.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
//        mfsnmtyCnRY4ntuEV0B40yooVLG2G6ddFJgEH5GCKZo9H7oBFZGGE7c0IYoEI6C0F6kHGKSCIJkHJJk4JJkCF69DHJSGJlgCEl9FH61GKZs9IYdeFkL5nRU1oq
        System.out.println("result = " + result);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "android/content/Context->getAssets()Landroid/content/res/AssetManager;": {
                return new AssetManager(vm, signature);
            }
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public FileResult<AndroidFileIO> resolve(Emulator<AndroidFileIO> emulator, String pathname, int oflags) {
        System.out.println("emulator = " + emulator + ", pathname = " + pathname + ", oflags = " + oflags);
        if (pathname.equals("/proc/self/maps")) {
            return FileResult.success(new SimpleFileIO(oflags, new File("unidbg-android/src/test/java/com/phil/meiriyouxian/maps"), pathname));
        }
        return null;
    }
}
