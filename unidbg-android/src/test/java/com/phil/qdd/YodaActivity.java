package com.phil.qdd;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.arm.context.EditableArm32RegisterContext;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.hook.hookzz.HookEntryInfo;
import com.github.unidbg.hook.hookzz.HookZz;
import com.github.unidbg.hook.hookzz.IHookZz;
import com.github.unidbg.hook.hookzz.WrapCallback;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.android.*;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.MemoryBlock;
import com.github.unidbg.memory.SvcMemory;
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.unix.UnixSyscallHandler;
import com.github.unidbg.utils.Inspector;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class YodaActivity extends AbstractJni implements IOResolver<AndroidFileIO> {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private YodaActivity() {
//        emulator = AndroidEmulatorBuilder
//                .for32Bit()
////                .setProcessName("com.sina.oasis")
////                .addBackendFactory(new DynarmicFactory(true))
//                .build();
        AndroidEmulatorBuilder androidEmulatorBuilder = new AndroidEmulatorBuilder(false) {
            @Override
            public AndroidEmulator build() {
                return new AndroidARMEmulator(processName, rootDir, backendFactories) {
                    protected UnixSyscallHandler<AndroidFileIO> createSyscallHandler(SvcMemory svcMemory) {
                        return new yodaSyscallHandler(svcMemory);
                    }
                };
            }
        };
        emulator = androidEmulatorBuilder.build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/qdd/qtt_new.apk"));

        emulator.getSyscallHandler().addIOResolver(this);
        new AndroidModule(emulator, vm).register(memory);

        SystemPropertyHook systemPropertyHook = new SystemPropertyHook(emulator);
        systemPropertyHook.setPropertyProvider(key -> {
            System.out.println("lilac Systemkey:" + key);
            switch (key) {
                case "ro.serialno":
                    return "f8a995f5";
                case "ro.product.manufacturer":
                case "ro.product.brand":
                    return "Xiaomi";
                case "ro.product.model":
                    return "MIX 2S";
            }
            return "";
        });
        memory.addHookListener(systemPropertyHook);

        DalvikModule dmLibc = vm.loadLibrary(new File("unidbg-android/src/main/resources/android/sdk23/lib/libc.so"), true);
        Module moduleLibc = dmLibc.getModule();
        // HOOK popen
        int popenAddress = (int) moduleLibc.findSymbolByName("popen").getAddress();
        // 函数原型：FILE *popen(const char *command, const char *type);
        emulator.attach().addBreakPoint(popenAddress, new BreakPointCallback() {
            @Override
            public boolean onHit(Emulator<?> emulator, long address) {
                RegisterContext registerContext = emulator.getContext();
                String command = registerContext.getPointerArg(0).getString(0);
                System.out.println("lilac popen command:" + command);
                emulator.set("command" , command);
                return true;
            }
        });

        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/qdd/libyoda.so"), true);
        module = dm.getModule();
        vm.setVerbose(true);

        vm.setJni(this);
        dm.callJNI_OnLoad(emulator);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        YodaActivity yodaActivity = new YodaActivity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");
        yodaActivity.callBulwark();
    }

    public void hookGetEnvByHookZz() {
        IHookZz hookZz = HookZz.getInstance(emulator);
        hookZz.wrap(module.findSymbolByName("getenv"), new WrapCallback<EditableArm32RegisterContext>() {
            String name;

            @Override
            public void preCall(Emulator<?> emulator, EditableArm32RegisterContext ctx, HookEntryInfo info) {
                name = ctx.getPointerArg(0).getString(0);
            }

            @Override
            public void postCall(Emulator<?> emulator, EditableArm32RegisterContext ctx, HookEntryInfo info) {
                switch (name) {
                    case "PATH": {
                        MemoryBlock replaceBlock = emulator.getMemory().malloc(0x100, true);
                        UnidbgPointer replacePtr = replaceBlock.getPointer();
                        String pathValue = "3";
                        replacePtr.write(0, pathValue.getBytes(StandardCharsets.UTF_8), 0, pathValue.length());
                        ctx.setR0(replacePtr.toIntPeer());
                    }
                }
            }
        });
    }


    private void callBulwark() {
        Pointer jniEnv = vm.getJNIEnv();
        DvmObject<?> dvmObject = vm.resolveClass("com.sogou.scoretools").newObject(null);
        List<Object> args = new ArrayList<>();
        args.add(jniEnv);
        args.add(vm.addLocalObject(dvmObject));
        String str = "{\"screen_brightness\":\"82\",\"tk\":\"ACHaSnpgYUzPHTlyVie2s7LThdzXV_vhfZ40NzUxNDk1MDg5NTIyNQ\",\"cpu_model\":\"AArch64 Processor rev 3 (aarch64) ,8,2016000\",\"carrier\":\"46007\",\"instance\":\"com.inno.yodasdk.info.Infos@ac3ab7c\",\"sim_state\":\"5\",\"sid\":\"56a91d6a-204d-48ea-b170-4c5cd713e05e\",\"imei\":\"869593867257804\",\"gyro\":\"0.02,0.0,1.0\",\"manufacturer\":\"HUAWEI\",\"screen_scale\":\"5.2\",\"android_id\":\"86ee835487a1f4e4\",\"boot_time\":\"1626514060336\",\"volume\":\"4,5,5,11,6\",\"serial_number\":\"LNX11WPJ5M627459\",\"bt_mac\":\"14:09:DC:99:DB:89\",\"wifi_mac\":\"08:40:f3:f6:9a:21\",\"mac\":\"14:09:dc:9b:1c:60\",\"cid\":\"47514950895225\",\"charge_state\":\"2\",\"apps_count\":\"2,120\",\"package_name\":\"com.jifen.qukan\",\"ext\":\"{\\\"author_id\\\":\\\"2328110\\\",\\\"content_id\\\":\\\"1624220959\\\",\\\"member_id\\\":\\\"1453484970\\\"}\",\"platform\":\"android\",\"sensor_count\":\"11\",\"app_version\":\"3.10.48.000.0714.1521\",\"screen_size\":\"1080,1920,3.0\",\"brand\":\"HUAWEI\",\"sdk_version\":\"1.0.7.210128\",\"wifi_name\":\"123\",\"os_version\":\"23\",\"hardware\":\"hi3635\",\"adb\":\"1\",\"scene\":\"qtt_article_readtimerreport\",\"model\":\"HUAWEI GRA-TL00\"}";
        String str2 = "dubo";
        String str3 = "1629280231";
        args.add(vm.addLocalObject(new StringObject(vm, str)));
        args.add(vm.addLocalObject(new StringObject(vm, str2)));
        args.add(vm.addLocalObject(new StringObject(vm, str3)));
        Number number = module.callFunction(emulator, 0x8ff1, args.toArray());
        byte[] result = (byte[]) vm.getObject(number.intValue()).getValue();
        Inspector.inspect(result, "result");
    }

    @Override
    public FileResult<AndroidFileIO> resolve(Emulator<AndroidFileIO> emulator, String pathname, int oflags) {
        System.out.println("emulator = " + emulator + ", pathname = " + pathname + ", oflags = " + oflags);
        return null;
    }
}
