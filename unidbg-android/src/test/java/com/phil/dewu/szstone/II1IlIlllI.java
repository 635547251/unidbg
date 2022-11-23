package com.phil.dewu.szstone;

import java.util.UUID;

public class II1IlIlllI {
    public static String ll1lIll1II11(String str, int i2) {
        StringBuilder sb = new StringBuilder(str);
        String str2 = i2 == 4 ? "auto" : i2 == 5 ? "sc" : i2 == 6 ? "gy" : "default";
        sb.append("bty");
        sb.append((char) 28);
        sb.append(str2);
        sb.append((char) 29);
//        String ll1lIll1II112 = Ill11llllIl11.ll1lIll1II11(III1IIll11lI.Ill11llllIl11);
        String ll1lIll1II112 = "";
        sb.append("bkv");
        sb.append((char) 28);
        sb.append(ll1lIll1II112);
        sb.append((char) 29);
        String replace = UUID.randomUUID().toString().replace("-", "");
        sb.append("iud");
        sb.append((char) 28);
        sb.append(replace);
        sb.append((char) 29);
        return sb.toString();
    }
}
