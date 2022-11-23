package com.phil.dewu;

import com.github.unidbg.linux.android.SystemPropertyProvider;

public class MySystemPropertyProvider implements SystemPropertyProvider {
    @Override
    public String getProperty(String key) {
        switch (key) {
            // ndid
            case "ro.serialno":
                return "";
            // ndid2
            case "ro.boot.serialno":
                return "";
            // nbra
            case "ro.product.brand":
                return "Lenovo";
            // nmf
            case "ro.product.manufacturer":
                return "Lenovo";
            // ndme
            case "ro.product.model":
                return "Lenovo L38041";
            // nabi
            case "ro.product.cpu.abi":
                return "arm64-v8a";
            // nabil
            case "ro.product.cpu.abilist":
                return "arm64-v8a,armeabi-v7a,armeabi";
            // ndgst
            case "ro.boot.vbmeta.digest":
                return "";
            // nhn
            case "net.hostname":
                return "";
            // root检查
            case "ro.kernel.qemu":
                return null;
            // 内存泄露检查
            case "libc.debug.malloc":
            case "libc.debug.malloc.program":
                return null;
            // 模拟器检测
            case "init.svc.vbox86-setup":
            case "init.svc.qemu-props":
            case "init.svc.windroyed":
            case "init.svc.ttVM_x86-setup":
            case "init.svc.droid4x":
            case "init.svc.noxd":
                return null;
            case "ro.hardware":
                return "qcom";
        }
        throw new UnsupportedOperationException(key);
    }
}
