package com.phil.dewu.smsdk;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidElfLoader;
import com.github.unidbg.linux.AndroidSyscallHandler;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


public class SmsDkActivity extends AbstractJni{
    private final AndroidEmulator androidEmulator;
    private final VM vm;
    private final Memory memory;
    private final Module module;

    public SmsDkActivity() {
        androidEmulator = AndroidEmulatorBuilder.for64Bit()
                .setProcessName("com.shizhuang.duapp")
                .setRootDir(new File("/tmp/rootfs/default"))
//                .addBackendFactory(new DynarmicFactory(true))
                .build();

        memory = androidEmulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));

        vm = androidEmulator.createDalvikVM(new File("unidbg-android/src/test/java/com/phil/dewu/com.shizhuang.duapp_4.88.0.apk"));
        vm.setVerbose(true);
        vm.setJni(this);

        // 注册 libandroid.so
        new AndroidModule(androidEmulator, vm).register(memory);

        DalvikModule dalvikModule = vm.loadLibrary(new File("unidbg-android/src/test/java/com/phil/dewu/smsdk/libsmsdk.so"), true);
        module = dalvikModule.getModule();

        vm.callJNI_OnLoad(androidEmulator, module);
    }

    // 设置debug日志
    static {
        Logger.getLogger(AndroidElfLoader.class).setLevel(Level.DEBUG);
        Logger.getLogger(AndroidSyscallHandler.class).setLevel(Level.DEBUG);
        Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SmsDkActivity smsDkActivity = new SmsDkActivity();
        System.out.println("load the vm " + (System.currentTimeMillis() - start) + "ms");
//        smsDkActivity.debugger();
        smsDkActivity.x2_0x55fa0();
    }


    private void debugger() {
        androidEmulator.attach().addBreakPoint(module, 0x27550);
    }


    // fingerprint
    private void x2_0x55fa0() {
//        androidEmulator.traceCode();
        DvmObject<?> dvmObject = vm.resolveClass("com.ishumei.dfp.SMSDK").newObject(null);
        String str = "wbdibgsipbeeyliz";
        String str2 = "{\"a1\":\"all\",\"a2\":\"20220321120051eaa393a86742c013d7087449005bd43601508b4a55840427\",\"a3\":\"none\",\"a4\":\"16\",\"a5\":\"myapp\",\"a6\":\"android\",\"a7\":\"2.9.8\",\"a8\":\"\",\"a30\":\"<unknown ssid>\",\"a34\":1804800,\"a37\":113,\"a39\":\"com.shizhuang.duapp\",\"a40\":1650530709254,\"a46\":{\"cpu_abi\":\"arm64-v8a\",\"serial\":\"HKL5DDUW\",\"fingerprint\":\"Lenovo\\/kunlun\\/kunlun:8.1.0\\/OPM1.171019.019\\/5.0.188_190222:user\\/release-keys\",\"model\":\"Lenovo L38041\",\"cpu_abi2\":\"\",\"brand\":\"Lenovo\",\"board\":\"sdm660\",\"manufacturer\":\"Lenovo\"},\"a86\":\"1100010\",\"a19\":\"02:00:00:00:00:00\",\"a9\":1652025350000,\"a10\":\"8.1.0\",\"a11\":\"\",\"a12\":29211,\"a13\":\"Lenovo L38041\",\"a14\":\"\",\"a15\":\"false\",\"a17\":[\"wlan0,192.168.4.78,40a10889dd46,fe80::ec1d:29dc:84cd:b39%wlan0\"],\"a18\":{\"gsm.network.type\":\"LTE,Unknown\",\"gsm.sim.state\":\"READY,ABSENT\",\"sys.usb.state\":\"adb\",\"ro.debuggable\":\"0\"},\"a20\":\"\",\"a24\":\"951bf4d02d44e36f\",\"a25\":\"\",\"a26\":\"02:00:00:00:00:00\",\"a27\":[],\"a28\":{},\"a29\":\"660_GEN_PACK-1.168271.1.173428.1\",\"a31\":\"192.168.4.78\",\"a33\":\"AArch64 Processor rev 2 (aarch64)\",\"a35\":\"\",\"a36\":\"1080,2016,480\",\"a38\":\"4.88.0\",\"a42\":\"com.shizhuang.duapp\",\"a43\":\"Lenovo\",\"a44\":\"wifi\",\"a45\":\"\",\"a47\":[\"1,BOSCH\",\"35,BOSCH\",\"2,AKM\",\"14,AKM\",\"4,BOSCH\",\"16,BOSCH\",\"8,Sensortek\",\"5,Sensortek\",\"1,BOSCH\",\"35,BOSCH\",\"2,AKM\",\"14,AKM\",\"4,BOSCH\",\"16,BOSCH\",\"8,Sensortek\",\"5,Sensortek\",\"9,QTI\",\"10,QTI\",\"11,QTI\",\"18,QTI\",\"19,QTI\",\"17,QTI\",\"15,QTI\",\"20,QTI\",\"3,QTI\",\"22,QTI\",\"27,QTI\",\"29,QTI\",\"30,QTI\",\"9,QTI\",\"10,QTI\",\"11,QTI\",\"18,QTI\",\"19,QTI\",\"15,QTI\",\"20,QTI\",\"3,QTI\",\"27,QTI\",\"33171006,QTI\",\"33171007,QTI\",\"33171009,QTI\",\"33171011,QTI\",\"33171012,QTI\",\"33171024,QTI\",\"65546,Motorola\",\"69633,ZUK\"],\"a48\":5994246144,\"a49\":\"\",\"a32\":8,\"a50\":{},\"a51\":[],\"a57\":-1227746293,\"a65\":50,\"a71\":114907406336,\"a74\":0,\"a75\":0,\"a88\":\"locateServiceName:android.os.BinderProxy|phoneServiceName:android.os.BinderProxy\",\"a98\":\"\",\"a108\":\"20220321120051eaa393a86742c013d7087449005bd43601508b4a55840427\",\"a109\":\"20220321120051eaa393a86742c013d7087449005bd43601508b4a55840427\",\"a110\":\"\",\"a97\":\"\",\"a16\":\"VBh6Z9UR64faMSRpaEEmiZeKzqPmkAEMSIto9rTOW2W8GY0LEIAd6\\/hxZjWzfQ7Ow+3NmmHVFGWLzOeL5lr4GAoZWHdI5mMMMb4YLLlR3x0nOT1jU\\/UC+5lFpFIn5k4vfmAT5N5LgAuPR8xY5W85OnAzWGaoIpzfS1eOHBaWXTKM2Zw1bB7zFWRDMjs5a60yz5RBgtb3uIBKyl6+p449KaEbBWqF3Z5Mom\\/SHYwatkiCyHwhRRjIid69xZjtLadjLV5lWWBMWO1GaOVCeycfAHlqrGMtf0HUkakS+V1UXGyqBtF14QDVDH2limiPNFRPpmHDwWfmqaeLcByBBkUesniTz1jv3\\/eFarbyn\\/UdGnpR4ezm7Os69ggK8cGxQgD2UOXgyNvTXc9VOkR+0Hz8hv38iXrAclAXcZX1ioHnmYha0LYzR\\/t2vtM8WwglCtiYxfxNJXPTSL1U1hkXGsmubafDK+v8ArVFU3sZlL9YIkLYsvaARvrSnB2YnqS0HCdshNUm5\\/GYxqkgqAFUT+wmEN3d3pRfwjgiQ3rhcf6gdGHM+sir1J\\/u+eB7S5JOu5bdQddBG3eGt1\\/RniTS8cfVyXrPvVdKqrjbioid5dV1cSbzNxQKAuEiCJ23lQgM1ue3zMg3TOM7+8wdjoacOT5ExSK4m00TLMx7oD5PHeLlBuk\\/KOnomveqe3zcK2E4gDiyg0vgOjMAQ6wjczSfWFuiLWiuZNrCdCBtYTa61n2vO71uLxD6BcMeJ3SVP4\\/OoHqchDZDSIG1QAZJELC9b4CbP51maAFTAqV3CH8w+M7rEzzwbr7HcGQiu51v03h7W8A8R0CqdJYw8Wnyuy2lYNPQXq79JP1nr\\/X\\/eM7kBWEbWxAdYLtejajXrlE78xWUVO3x6yZjI7a5UH\\/wPPpuIXY3ZG5Mpxv93xR787pvPfu3Ika6VCK8lHeZVza6TVVXcYkEWlABBBGKKlPcvqCi9bbSfCR2V4Z+okSX13mALdxQajQ9+5Wtl0OlQIMTQW2CLYlbCjy01FVNUmT57Bt0Wmk3Jd4Nm5lxE6KysoinrfNfgK7qxT0x1lg3R3vB1krJKsMQURgG+Y5yelX2rQj2Bu4iFcslqPMa1JCGLzTZ5A0VpMbBKkphU32jOf4tl7gwlvUtSagUoE3BeWUTNQk9IgDqYsu2xz4y1brkxmW\\/JoLyX+ey6UogCRf1kMOtcoxLrGlsxU3UvEAehd1IMB\\/AacRdPm7fnf\\/URU5JcmYdLP2Okf\\/h8V\\/balkUnEcqEoQIsM6a4kX+\\/Os5tH\\/3hX1JuEkW\\/RK\\/INLTuK+wvHcgl7MSiOSEgVGxuhnoS60eBIRPFI7v04x6Hr8uT8HtZmfW0TnChDyKaqLFzP0APPCEvNNPelnIMUpamv2El+Ll6TYzhCCBo3blQX6aCnkdccTZmTvojMAx17rmdC9ASTnmayKPUs1XnNloWybDeoi2nVcb5vkcvHUE6LYWascVER+lSaCFMIZLG48E2eNkC8VzxDE5GuFhLP67SbAc\\/fGzuGaW4Br+JcvUewfd8z0HUqkalxNu3Ws\\/gOLfv4TNBDHQkl5C33oCTNoJF7lJ1rVJfji31InpBuDp5iYN\\/BOoxwGijJCht8jTiVPEdmBxNDFHDv+FOI4sUqUn0IKnCqhkuFy5A+5uaJ\\/k6cwXIiis2KQp1kEYCuWLjPEakRSpB0F8I94b7jwzvDE92BCouL7zV4lJaddmt4dHFd4C7cdX6unvaUgNwNDXXCbY9atTG8qh69vdv8zuHU2D9C4jnU9pog2HbPPE9LszyGQsArpEOu34KpO3QHYuBhQNXpFJr5dJe\\/hh\\/9YAaGc3wxKWzU0ZAYIzHUIGMcrwATvtYKFHfdaKOddolfl2eL3qiuZLymzL6lGoulYzU6HBWV1NpG7o91Pv2HUxXkEgsTbr6f+LCjBBE5RdWLVXdSmSHC5TLn9B4BQiFNZ8+Xw7An+MS08HrflzjN7\\/WIBgWOqkDVy7W0OQntUYjJDeDS7fHnKZLYfLnfhpgOtz1XYJJ4+fTF3DaA5kKkZQIfZ\\/qRVUjwpQ9jGoO4Zc2K1GOlQIGUCbM1isO7ki6u1g28bsUxTOZZclZaeDY61GpZHp46SlF3fpm2aiHEblxj4UDjUSHwV9AYXqL+wppYFzDIaGRSGj3JDsLa22YLCkO8eVVlKsYa4wIjFM7T5UcgU32fDe14DR7xfHgtCdEWIezXp6dh2DxTYQFdkn4IP3gnrTVwVjsg\\/iq\\/0NNkGPN8kx16ZosS0gWBX\\/ipw3Wi4CUmiY8+DFRCGY2QyAFBWi\\/qUHYpvI4IAnpbgYn\\/PuHvsajrBkV2NkIoLaLb5lQDPTxHFpoZjPxwFljul2ftsNesGyDAKhUptk\\/8XWHOBfPXGPZ2+h0LR4LMKv2nVcWLNY02slGv\\/1qXg5vW5LmmkvpVgd0QYjit5tOHUMjko02SZGjBPtLKHVWyRpBgydRcUW5rZwBV\\/T0SBRM8oH9hkdpMfcz8vgP2U0Pta\\/shXDGihEnRStc2WWYGhoQlR9llbD\\/K9jIRALoZ0fDt16ngmQu5jB5EGPBwW7NAjSXV0pUrvAcCPDPolV3mFg0MnzNSpArcOZpkeOEurHudjBUvEIg3uC6qNijpFng2HQA0ZttjSsKO\\/MvBuvKoqVg0dsIeN3LNDs214ipquye1NIbeyIkI3rKZ9UKDb33ACzrqHux9MwYBzsLy2WK21sLPMSBntso2DalJwN\\/90nHmcgN9ZGjkM1JzxzZc7gPdrD3JUGq5r2hWeOixejFfh1gFGcZABan7onwH0c0YxVH88zrLXkX\\/3LIlFsf0hVQDfOY55ibOI7lKhCjzSR0Q83+hXd1XExhA8tE3uRI2z3fyfmhVj3RZ8ToXMZPEJjKZ+feodq8DuCEYKpCvcP+N2kQ8AwpC0omhLFhzilqSvviCn74hQz5N3XoSxt+xJtLwyKuT2LjC59TaOVrc0dM\\/Nmj3\\/40LxdLgeDP5JXXuEvNMsUHSXB24Y6uYaaNd83vqs2bE\\/cuVDtjkH7Tx5fgfQyFtzdQtAa8UbtjFIwCGSoBXhprIu9E40BzW2fdpWnjL8u5vpkHQGvaxXPi\\/hGi\\/8kfVzertB8xmoVhn6KGvHOhyPrrgMDzMIcNFHw4RBSsRuRNQQ6GiOzeGEhjHMyhclMPwK9wdWljTSxKpyjcQIEr\\/ZZ8Cr61t9aeuuksP6GDmwmfHz4jQizC023XsDo+WQ4RjSEPYjd6lPkLeAq91qAfgiLTmRuGwpiBiZ9T32MxA0ctvE8o50y9K3MWdrCppJgGaxEYN90OX1\\/AFU17x\\/\\/LEX23fJ0DZp\\/\\/U5a3eOCmVNgSW2UArMbl8cQ8h7qLuJkG9ZZz9Sa25hFq08p1K+qG8pj3uKIeMNcK1NTbZmHpjdOkum5qDMUrhYJVWYJkx9PlQk4P8\\/+wFvalKWS9vmKF\\/n3dGchLHn4OYJ4VbYB1kyc6DR5L+idyGOSjMPMQP\\/vOtx6NNE7XAp04Jt1U12y91hAMFK0\\/zHPasyA8JvJl4dGczSyY4JrmW0xaRsSXINZxdDAzGD4vmI5AYtThIKNCg6lgZHcsfh9JZArAyxr2lYMKO4N+aeqtryJ3yuYfwP4P+k9M8hXYoqISHttC0LRm98YVX1S46PT1GhxnHRe\\/yBZSoQXJwKzmezu6Ujqe4lzJhMQ7YhIvKVnxV5lfBnxtUj6kvdMn5H8JR3vYEHPje6Grx0OrKTblhVOEJ7G+MrL7HooejOMkf\\/XEQUZH5gWB3BfDYUAA3VrxL++tK0sABk0gWMgCqJnR0174ExvFJpi24\\/tpM+u2uuJIaNZAXEQpageje6q+m5wsd3ChFQkB9VqkmonkG\\/M9zvBg5WfhizyodMlEAkM0ejfMWYoHLv\\/oyKj51Gt\\/cg4PbXSvfpM5MGMOPC\\/buzufqQicF4Uu46JN7qJqAq5nw3WZl56PX2wwFoF\\/qs5ieyxbaOJQALltnW1jJIMqRVJIHwYVzhkhz8oOa2OesTLg5ThwRDdEzGjtc9SbDDzkNqgURfbcXBf\\/\\/DHyOkC23f0Ch7BnSFUw1q2AMjp51K21JpVeUXzWA5G5cwCUwhzGWSPxiUamZvRCKeA8aPe7QovWODuXCze+f1IjxwFCxoZVoHliD18c\\/LR93rrP0dhNNWjbDsXtsDRVH+QDAs6ONeZt2Dys1l5gYe9D8JkMX5ih09BCjV7EaOc4zBGGgvh5splEtkzlJqQ4Xl76\\/j6Rwfls9c3y2jaJW85d0NgTlZIsXJc2HwBuP+x9s8AI+xxCQ8Z9fABQsHSDpzhyVGGnVaSLVYpcwJ+FNX2i4ARSNiJg3TQ1uut3tD8Zc16JmxDbx2JhG\\/RG5AW0Z7\\/0k9QaZGnECYnkK\\/i4i3MEs9nfzvQYlFbPHsoFGg54Xmpu5tE1qHb1KSO2+zXM2BQ64erFEq7gjKdK7PikclA4D8OEJcowpCb4quyRl58rkmYaHGU6vdNulgGE6nCtQ3alrbVb6xYJFYg9EkpAkmDLzCPiGR9ieZp8WvLd4CmzjM1kesG7Udosqgnt6YSOZYGuXplSzC0Ja75wI2qzfqRZYHhoaOclKGWTCFgJqHsZZymPr+QIlduPZDssfC7hb9ubCGyG\\/7T+pQFcLAJ3CXKPTjoR0YTKA2tkKYjvRNaLMv\\/hMJNgS8\\/GHCV1Ch+5kdX\\/K2Z\\/wyVAzJdU3bfqvs+eMvr\\/ESbrbRShH2S4onJOz5M8uQ+cnV8KyEFNklVch7kC51WTy5X8iwn02V6XA0+nABYUoIsxIU2Bo2WGnX9I5vDL6OdpCWz8aiN7bYbF0QtHyN5J0xsw7hDOlO74WipVSxwblBhJB3DBuaH2H7IyLruKnEbfL9Xgr3dYqGv8qLayB7seRx5ietkk7yvrVskYhAhgEaMp6YOWaYZ5\\/Vo3M5jiUCrfljjPxojOAirYsNsa2ZRFyf1cSdyIpYb3eTQZFFfOu0UZpzLKYRqcBJVE4dcCuG\\/ykXEXwiZYeyB0UWgLJBpLh8gciK4iNJv9ArDF+12knn+hRtDYxkAaBCN1guvHXOfijPuuKl93H6Bv9u2y9b5wPtZ4XngaHdLsnUOtKQ99nAnQ8BFszn5AMc8JZkABLaURXBQp4hkGEEhjUNu7figIFXaDZ64jq6sHd1DtGB7KFxsDvi4TeScudCVhOBSeos+0ypzEo1JENtygeXxauN6nxLms7AGsOV8f5maZJ7sB0f2qsI0KtEBzKdaTUpKlv2vkHRK06QoTEPW4KccO2p79IhlzeF6Nj32i2Qmm6M1dkbBegh3giwDtXGj3f00pkFz+c7Vd9PqzDNjI7qQm37XJsuHD5KlOfUjdNvc2994JnXFeszs3PNJzgloYwG5xJoFGgRv9XgcOwtOPMWmvnzEyEwuHsTy8L9AUmV3OH5OKBx2z5cF9jWz+0At2Tfa1kj8LlgB3t3EYEbKyuxPexPx0uIlZkZGTUsUa7ZHvaawr2Gws7WNx\\/tziTO2XXpBAwwssiLnn+28DKtXXePTl8r1i1cVgZ1Ze7U6vLmMd6lyIbYEgA\\/zNjo+u5z0oieFz7wlLNT8qzts3xtooMT+njxbObC542f6TrQVcqcaNsFK66c=\",\"a111\":\"\",\"a41\":\"得物\",\"a52\":{\"magisk\":1},\"a53\":{},\"a54\":\"0000010\",\"a55\":\"3d73db6ce4863463a2cd78e538c2dd29\",\"a56\":\"CN=Jerry Ching, OU=R&D, O=Hupu, L=HongKou, ST=ShangHai, C=CN\",\"a58\":\"0\",\"a59\":\"0\",\"a60\":\"u0_a200\",\"a61\":[],\"a62\":\"\\/data\\/user\\/0\\/com.shizhuang.duapp\\/files\",\"a63\":[\"InputMethodInfo{com.iflytek.inputmethod.custom\\/.FlyIME, settings: com.iflytek.inputmethod.LauncherSettingsActivity}\",\"InputMethodInfo{com.android.inputmethod.latin\\/.LatinIME, settings: com.android.inputmethod.latin.settings.SettingsActivity}\"],\"a64\":{\"suc\":\"1\",\"enable\":\"0\",\"service\":[]},\"a66\":{},\"a67\":{},\"a69\":100298207232,\"a70\":101487833088,\"a72\":{\"temp\":300,\"vol\":4391,\"level\":100,\"scale\":100,\"status\":2},\"a73\":0,\"a68\":[],\"a76\":\"192.168.4.244:8888\",\"a77\":{},\"a78\":[],\"a79\":\"\",\"a80\":\"1652025349735-20259\",\"a81\":\"4\",\"a82\":\"9QJizgTomvaKLteHc7UzfixoL0IRM82GCQNJs75+PaHxCiM\\/rkRLObh1hj82dm7O\",\"a83\":\"0101100\",\"a84\":\"9vu8\\/JtsJ7I8GIKpgfqvO0ZifQuYmdxinHk9Jh76XiE=___\",\"a85\":[\"apps\"],\"a21\":\"4e3295d1416585d13ef8b15cf61f0179\"}";
        Pointer jniEnv = vm.getJNIEnv();
        List<Object> args = new ArrayList<>();
        args.add(jniEnv);
        args.add(vm.addLocalObject(dvmObject));
        args.add(vm.addLocalObject(new StringObject(vm, str)));
        args.add(vm.addLocalObject(new StringObject(vm, str2)));

        Number number = module.callFunction(androidEmulator, 0x55fa0, args.toArray());
        DvmObject<?> obj = vm.getObject(number.intValue());

        String value = (String) obj.getValue();
        System.out.println("result ==> " + value);
    }
}
