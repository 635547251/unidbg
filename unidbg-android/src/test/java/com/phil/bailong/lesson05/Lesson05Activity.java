package com.phil.bailong.lesson05;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Lesson05Activity extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private Lesson05Activity() throws FileNotFoundException {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .setProcessName("com.bilibili.app")
//                .addBackendFactory(new DynarmicFactory(true))
                .build();
        Memory memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        memory.setLibraryResolver(resolver);
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/bailong/lesson05/轻小说.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/bailong/lesson05/libsfdata.so"), true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);

        // 保存的path
//        String traceFile = "unidbg-android/src/test/java/com/phil/bailong/lesson05/qxstrace.txt";
//        PrintStream traceStream = new PrintStream(new FileOutputStream(traceFile), true);
//        emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);
    }

    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        Lesson05Activity lesson05Activity = new Lesson05Activity();
        System.out.println("load offset=" + (System.currentTimeMillis() - start) + "ms");
        lesson05Activity.getSFsecurity();
    }

    public void getSFsecurity() {
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        Object custom = null;
        DvmObject<?> context = vm.resolveClass("android/content/Context").newObject(custom);// context
        list.add(vm.addLocalObject(context));
        list.add(vm.addLocalObject(new StringObject(vm, "F1517503-9779-32B7-9C78-F5EF501102BC")));

        Number number = module.callFunction(emulator, 0xA944 + 1, list.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println("result = " + result);
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        if (signature.equals("java/util/UUID->randomUUID()Ljava/util/UUID;")) {
            return vm.resolveClass("java/util/UUID").newObject(UUID.randomUUID());
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        if (signature.equals("java/util/UUID->toString()Ljava/lang/String;")) {
            UUID uuid = (UUID)dvmObject.getValue();
            return new StringObject(vm, uuid.toString());
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }
}
