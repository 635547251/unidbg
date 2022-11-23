package com.phil.dewu.szstone;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.HookStatus;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.hook.HookContext;
import com.github.unidbg.hook.ReplaceCallback;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.linux.ARM32SyscallHandler;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.AndroidSyscallHandler;
import com.github.unidbg.linux.android.*;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.jni.ProxyDvmObject;
import com.github.unidbg.linux.file.RandomFileIO;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.SvcMemory;
import com.github.unidbg.unix.UnixSyscallHandler;
import com.phil.dewu.MyARMSyscallHandler;
import com.phil.dewu.MySystemPropertyProvider;
import com.phil.dewu.utils;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


public class SzstoneActivity extends AbstractJni implements IOResolver<AndroidFileIO> {
    private final AndroidEmulator androidEmulator;
    private final VM vm;
    private final Memory memory;
    private final Module module;

    public SzstoneActivity() {
        AndroidEmulatorBuilder builder = new AndroidEmulatorBuilder(true) {
            @Override
            public AndroidEmulator build() {
                return new AndroidARM64Emulator(processName, rootDir, backendFactories) {
                    @Override
                    protected UnixSyscallHandler<AndroidFileIO> createSyscallHandler(SvcMemory svcMemory) {
                        // 实现popen
                        return new MyARMSyscallHandler(svcMemory);
                    }
                };
            }
        };

        androidEmulator = builder.setProcessName("com.shizhuang.duapp")
                .setRootDir(new File("/tmp/rootfs/default"))
//                .addBackendFactory(new DynarmicFactory(true))
                .build();

        memory = androidEmulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));

        // 添加IOResolver
        androidEmulator.getSyscallHandler().addIOResolver(this);

        // _system_property_get
        SystemPropertyHook systemPropertyHook = new SystemPropertyHook(androidEmulator);
        systemPropertyHook.setPropertyProvider(new MySystemPropertyProvider());
        memory.addHookListener(systemPropertyHook);

        vm = androidEmulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/dewu/com.shizhuang.duapp_4.88.0.apk"));
        vm.setVerbose(true);
        vm.setJni(this);

        DalvikModule dalvikModule = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/dewu/szstone/libszstone.so"), true);
        module = dalvikModule.getModule();

        vm.callJNI_OnLoad(androidEmulator, module);
    }

    // 设置debug日志
    static {
//        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
//        Logger.getLogger(AndroidSyscallHandler.class).setLevel(Level.DEBUG);
//        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SzstoneActivity szstoneActivity = new SzstoneActivity();
        System.out.println("load the vm " + (System.currentTimeMillis() - start) + "ms");
//        szstoneActivity.patch();
//        szstoneActivity.hookSignature();
        szstoneActivity.hook_0x2C41C();
        szstoneActivity.hookArc4random();
//        szstoneActivity.debugger();
        szstoneActivity.lf_0x33e6c();
    }


    private void patch() {
        // android/app/ActivityThread->currentActivityThread()Landroid/app/ActivityThread;
        // mov w0, #1
        // patch if (sub_A6A4(v5))
        androidEmulator.getBackend().mem_write(module.base + 0x34398, new byte[]{0x20, 0x00, (byte) 0x80, 0x52});

        // com.github.unidbg.linux.file.TcpSocket
        // nop
        // sub_2CE30(v40)
        androidEmulator.getBackend().mem_write(module.base + 0x33DC4, new byte[]{0x1F, 0x20, 0x03, (byte) 0xD5});
    }

    private void hookSignature() {
        Dobby dobby = Dobby.getInstance(androidEmulator);
        dobby.replace(module.base + 0x1D13C, new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
                System.out.println("x8 => " + context.getPointerArg(8).getString(0));
                // x8 signature
                context.getPointerArg(8).write(utils.hexToByteArray("3330383230323433333038323031616361303033303230313032303230343466623964356138333030643036303932613836343838366637306430313031303530353030333036353331306233303039303630333535303430363133303234333465333131313330306630363033353530343038313330383533363836313665363734383631363933313130333030653036303335353034303731333037343836663665363734623666373533313064333030623036303335353034306131333034343837353730373533313063333030613036303335353034306230633033353232363434333131343330313230363033353530343033313330623461363537323732373932303433363836393665363733303230313730643331333233303335333233313330333533343332333033303561313830663332333133313332333033343332333733303335333433323330333035613330363533313062333030393036303335353034303631333032343334653331313133303066303630333535303430383133303835333638363136653637343836313639333131303330306530363033353530343037313330373438366636653637346236663735333130643330306230363033353530343061313330343438373537303735333130633330306130363033353530343062306330333532323634343331313433303132303630333535303430333133306234613635373237323739323034333638363936653637333038313966333030643036303932613836343838366637306430313031303130353030303338313864303033303831383930323831383130303835333662653133623734366338643138363365666233616337633137373162353165663763346435316434393437363536323662366361613761366139326566616433613266323336353735353766316135346266623861623137636135353065333336383934386266373330636162376130356362396464353438336134336338343434623163376337656362386637356361393866663537323164636363663038346461316164666463353961343164663564303765326337333865393535366263306462633535633932663732346636353739643963306134356636306437366665666633393537643433643764303930366535626464393964663330323033303130303031333030643036303932613836343838366637306430313031303530353030303338313831303036643161323737653062306565386566346561313839396233333065613933373562306131633236356266663830323366343365363966323262336230383830323435666639313933353266373963326332363639356636303638666631333961396136653564336337383762383262323031336364633936343138633330633030623638633832353163313033336437323934313461316535653461356130613736663131373561383164316435616263623362326139323135343731646566366164303533636332303363653661353339396339626136616632396333383338343763306437626435326461386164303635316665636337383339393964"));
                System.out.println("x8 => " + context.getPointerArg(8).getString(0));
                HookStatus.RET(emulator, originFunction);
                return super.onCall(emulator, context, originFunction);
            }
        });
    }


    // hook 0x2C41C返回 fopen(/proc/stat, r)
    private void hook_0x2C41C() {
        Dobby dobby = Dobby.getInstance(androidEmulator);
        dobby.replace(module.base + 0x2C41C, new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
                return HookStatus.LR(emulator, 0);
            }
        });
    }

    // 替换返回值
    private void hookArc4random() {
        Dobby dobby = Dobby.getInstance(androidEmulator);
        int arc4randomAddress = (int) module.findSymbolByName("arc4random").getAddress();
        dobby.replace(arc4randomAddress, new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {
                return HookStatus.LR(emulator, (int) ((Math.random() * 9 + 1) * 100000000));
            }
        });
    }


    private void debugger() {
        androidEmulator.attach().addBreakPoint(module, 0x33FE4);
        androidEmulator.attach().addBreakPoint(module, 0x34648);
        androidEmulator.attach().addBreakPoint(module, 0x3464C);

        // debugger popen
        int popenAddress = (int) module.findSymbolByName("popen").getAddress();
        androidEmulator.attach().addBreakPoint(popenAddress, new BreakPointCallback() {
            @Override
            public boolean onHit(Emulator<?> emulator, long address) {
                RegisterContext registerContext = emulator.getContext();
                String cmdline = registerContext.getPointerArg(0).getString(0);
                System.out.println("[popen] cmdline = " + cmdline);
                return true;
            }
        });
    }


    private void lf_0x33e6c() {
//        androidEmulator.traceCode();
        DvmObject<?> dvmObject = vm.resolveClass("com.shizhuang.stone.main.SzSdk").newObject(null);
        Pointer jniEnv = vm.getJNIEnv();
        StringObject str = new StringObject(vm, "awt0bapt/data/user/0/com.shizhuang.duapp/filesbav4.88.0bavn457bcndewubcv1.2.1.220209bdn得物bssm20220501163233cd4223bd5ff1312d3bdb4249868eaf35016322761c78e135dabsdadir/data/app/com.shizhuang.duapp-jDeJY5p16SEiXC-bA_mVoA==/base.apkdapcdappmdat1651315875859dbraLenovodbs100,4344,250,2,1,1651412308713dbt1650530693719dbuidOPM1.171019.019dcpuAArch64 Processor rev 4 (aarch64) ,8,1804800dcpuf0dcpui8,Hardware\\t: Qualcomm Technologies, Inc SDM636ddiOPM1.171019.019 dev-keysdfpLenovo/kunlun/kunlun:8.1.0/OPM1.171019.019/5.0.188_190222:user/release-keysdfuadgidgstdgyro0.0,0.0,0.0dhkidhwqcomdidcom.shizhuang.duappdlt2147483647dluadmac40:A1:08:89:01:1AdmccdmeLenovo L38041dmfLenovodmpcdmpndms5994246144,3553398784,5994987520dmua1651315875859dnQCOM-BTDdni0dnw%7BChinaNet-V339%7D%2C%7B401%7D%2C%7BNETGEAR%7D%2C%7BHUAWEI-EH7YPR%7D%2C%7B%21Ultimate%7D%2C%7B%40Ultimate%7D%2C%7BCMCC-GN5D%7D%2C%7BHUAWEI-Bing%7D%2C%7BCMCC-202%7D%2C%7B501-1%7D%2Cdo8.1.0do227dpid7821drcv0x79775662ddb300c9bdaa2e2c5fb49fef51058954000000000000000000000000ds24488810,114908450816dsc6.0dsdsndsenc46dsim0dss1080,2160,3.0dtumyappduiddust3dvo4,0,0,0,0dwiwlan0dwmb0:7f:b9:99:58:1ddwn401iaid858ff2b55155dc79iccidididHKL4iZQNifaid858ff2b55155dc79imsiisdildh0ldiv0lish1lism0lisr0lisvrt0sk9HBURPsA1zAXqmXaFXihvWOfukypOnLLlpTtHu1mkS4eq4Z9MziGnyPupwPFUFYXwedwpOEfxl92KWwD2dRfF67ppZ23sksour2srcandroidtcst409tct1651412309086terrtntWIFItot1651412308676btydefaultbkviudce9c0b0682b949db87d6dc48ea5372bd");
        List<Object> args = new ArrayList<>();
        args.add(jniEnv);
        args.add(vm.addLocalObject(dvmObject));
        args.add(vm.addLocalObject(str));
        args.add(1);

        Number number = module.callFunction(androidEmulator, 0x33e6c, args.toArray());
        DvmObject<?> obj = vm.getObject(number.intValue());
        byte[] value = (byte[]) obj.getValue();
        System.out.println("result ==> " + new String(Base64.getEncoder().encode(value), StandardCharsets.UTF_8));
    }

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        if (signature.equals("android/app/ActivityThread->currentActivityThread()Landroid/app/ActivityThread;")) {
            return vm.resolveClass("android/app/ActivityThread").newObject(null);
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature) {
            case "android/app/ActivityThread->getApplication()Landroid/app/Application;":
                return vm.resolveClass("android/app/Application").newObject(null);
            case "android/app/Application->getPackageManager()Landroid/content/pm/PackageManager;":
                return vm.resolveClass("android/content/pm/PackageManager").newObject(null);
            case "android/app/Application->getPackageName()Ljava/lang/String;":
                return new StringObject(vm, "com.shizhuang.duapp");
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public FileResult<AndroidFileIO> resolve(Emulator<AndroidFileIO> emulator, String pathname, int oflags) {
        File file;
        switch (pathname) {
            case "/dev/__properties__":
                file = new File("unidbg-android/src/main/resources/android/sdk23/dev/__properties__");
                return FileResult.success(new SimpleFileIO(oflags, file, pathname));
            case "/proc/stat":
                file = new File("unidbg-android/src/main/resources/android/sdk23/proc/stat");
                return FileResult.success(new SimpleFileIO(oflags, file, pathname));
            case "/proc/sys/kernel/random/boot_id":
//                file = new File("unidbg-android/src/test/java/com/phil/dewu/files/boot_id.txt");
//                return FileResult.success(new SimpleFileIO(oflags, file, pathname));
                return null;
            case "/proc/cpuinfo":
                file = new File("unidbg-android/src/test/java/com/phil/dewu/files/cpuinfo.txt");
                return FileResult.success(new SimpleFileIO(oflags, file, pathname));
            case "/proc/version":
//                file = new File("unidbg-android/src/test/java/com/phil/dewu/files/version.txt");
//                return FileResult.success(new SimpleFileIO(oflags, file, pathname));
                return null;
            case "/dev/urandom":
                return FileResult.success(new RandomFileIO(emulator, pathname));

        }
        System.out.println("[resolve] pathname = " + pathname);
        return null;
    }
}
