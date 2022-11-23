package com.phil.bailong.lesson07;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.debugger.Debugger;
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
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.utils.Inspector;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Lesson07Activity extends AbstractJni implements IOResolver<AndroidFileIO> {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private Lesson07Activity() {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .setProcessName("com.bilibili.app")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);

        emulator.getSyscallHandler().addIOResolver(this);

        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/bailong/lesson07/110_62cdaa7ac506051d5d6a5a7662847822.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/bailong/lesson07/libmtguard.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Lesson07Activity lesson07Activity = new Lesson07Activity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");
        lesson07Activity.main111();
        lesson07Activity.main203();
    }

    private void main111() {
        List<Object> args = new ArrayList<>();
        args.add(vm.getJNIEnv());
        args.add(vm.addLocalObject(vm.resolveClass("com.bilibili.app").newObject(null)));
        args.add(111);
        args.add(vm.addLocalObject(new ArrayObject()));
        module.callFunction(emulator, 0x5a38d, args.toArray());
    }

    private void main203() {
        List<Object> args = new ArrayList<>();
        args.add(vm.getJNIEnv());
        args.add(vm.addLocalObject(vm.resolveClass("com.bilibili.app").newObject(null)));
        args.add(203);

        StringObject input2_1 = new StringObject(vm, "9b69f861-e054-4bc4-9daf-d36ae205ed3e");
        ByteArray input2_2 = new ByteArray(vm, "GET /aggroup/homepage/display __r0ysue".getBytes(StandardCharsets.UTF_8));
        DvmInteger input2_3 = DvmInteger.valueOf(vm, 2);
        vm.addLocalObject(input2_1);
        vm.addLocalObject(input2_2);
        vm.addLocalObject(input2_3);
        args.add(vm.addLocalObject(new ArrayObject(input2_1, input2_2, input2_3)));
        Number number = module.callFunction(emulator, 0x5a38d, args.toArray());
        StringObject result = (StringObject) ((DvmObject[]) ((ArrayObject) vm.getObject(number.intValue())).getValue())[0];
        System.out.println(result.getValue());
//        String result = vm.getObject(number.intValue()).getValue().toString();
//        System.out.println(result);
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "com/meituan/android/common/mtguard/NBridge->getPicName()Ljava/lang/String;": {
                return new StringObject(vm, "ms_com.sankuai.meituan");
            }
            case "com/meituan/android/common/mtguard/NBridge->getSecName()Ljava/lang/String;": {
                return new StringObject(vm, "ppd_com.sankuai.meituan.xbt");
            }
            case "com/meituan/android/common/mtguard/NBridge->getAppContext()Landroid/content/Context;": {
                return vm.resolveClass("android/content/Context").newObject(null);
            }
            case "com/meituan/android/common/mtguard/NBridge->getMtgVN()Ljava/lang/String;": {
                return new StringObject(vm, "4.4.7.3");
            }
            case "com/meituan/android/common/mtguard/NBridge->getDfpId()Ljava/lang/String;": {
                return new StringObject(vm, String.valueOf(vm.getEmulator().getPid()));
            }
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "android/content/Context->getPackageCodePath()Ljava/lang/String;": {
                return new StringObject(vm, "/data/app/com.sankuai.meituan-TEfTAIBttUmUzuVbwRK1DQ==/base.apk");
            }
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public int getIntField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        switch (signature) {
            case "android/content/pm/PackageInfo->versionCode:I": {
                return 1100090405;
            }
        }
        return super.getIntField(vm, dvmObject, signature);
    }

    @Override
    public DvmObject<?> newObjectV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "java/lang/Integer-><init>(I)V":
                int input = vaList.getIntArg(0);
//                return new DvmInteger(vm, input);
                return vm.resolveClass("java/lang/Integer").newObject(input);
        }
        return super.newObjectV(vm, dvmClass, signature, vaList);
    }

    @Override
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        if (signature.equals("android/content/pm/PackageManager->GET_SIGNATURES:I")) {
            return 0x40;
        }
        return super.getStaticIntField(vm, dvmClass, signature);
    }

    @Override
    public FileResult<AndroidFileIO> resolve(Emulator<AndroidFileIO> emulator, String pathname, int oflags) {
        if (pathname.equals("/data/app/com.sankuai.meituan-TEfTAIBttUmUzuVbwRK1DQ==/base.apk")) {
            return FileResult.<AndroidFileIO>success(new SimpleFileIO(oflags, new File("unidbg-android/src/test/java/com/phil/bailong/lesson07/110_62cdaa7ac506051d5d6a5a7662847822.apk"), pathname));
        }
        return null;
    }
}
