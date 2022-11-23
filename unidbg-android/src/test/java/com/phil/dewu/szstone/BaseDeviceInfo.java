//package com.phil.dewu;
//
//import java.lang.reflect.Field;
//
//public class BaseDeviceInfo {
//    private String awt;                                                 // 0
//    private String bapt = "/data/user/0/com.shizhuang.duapp/files";     // file.getAbsolutePath()   文件绝对路径
//    private String bav = "4.88.0";
//    private String bavn = "457";
//    private String bcn;                                                 // dewu
//    private String bdn = "得物";
//    private String bssm;                                                // 20220321120051eaa393a86742c013d7087449005bd43601508b4a55840427   shumeiid
//    private String dabs;
//    private String dadir;                                               // /data/app/com.shizhuang.duapp-Q0KlpKpSgribn3DkboV_zg==/base.apk  context.getPackageCodePath()    包路径
//    private String dapc;
//    private String dappl;
//    private String dappm = null;
//    private String dat;                                                 // 时间戳
//    // 电池信息
//    // level,voltage,temperature,status,plugged
//    // 电量,电压,电池温度,充电状态(1: 未知 2: 充电中 3: 放电中 4: 未充电 5: 电池满) ,充电设备(1: 充电器 2: USB 4: 无线充电)
//    // 100,4357,320,2,1,1651595002705
//    private String dbs;
//    private String dbt;                                                 // String.valueOf(System.currentTimeMillis() - SystemClock.elapsedRealtime())   当前时间-系统启动时间
//    private String dbuid;
//    private String dcpu;                                                // OPM1.171019.019  android.os.SystemProperties."ro.system.build.id"
//    private String dcpuf;
//    private String dcpui;
//    private String ddi;
//    private String dfp;
//    private String dfua;
//    private String dgi;
//    private String dgyro;
//    private String dhki;
//    private String did;
//    private String dlt;
//    private String dlua;
//    private String dmaci;
//    private String dmact;
//    private String dmcc;
//    private String dmpc;
//    private String dmpn;
//    private String dms;
//    private String dmua;
//    private String dn;
//    private String dni;
//    private String dnw;
//    private String dpid;
//    private String ds;
//    private String dsb;
//    private String dsc;
//    private String dsdsn;
//    private String dsenc;
//    private String dsim;
//    private String dss;
//    private String dtu;
//    private String dust;
//    private String dvo;
//    private String dwi;
//    private String dwm;
//    private String dwn;
//    private String iccid;
//    private String idid;
//    private String imsi;
//    private String isdi;
//    private String isds;
//    private String ldh;
//    private String ldiv;
//    private String lish;
//    private String lism;
//    private String lisou;
//    private String lisr;
//    private String lisvrt;
//    private String sk;
//    private String sksour;
//    private String tcst;
//    private String tct;
//    private String terr;
//    private String tlnat;
//    private String tlnet;
//    private String tnt;
//    private final String bcv = "1.2.5.220323";
//    private String dmac = null;
//    private String iaid = null;
//    private final String tot = ll1lIll1II11.Ill11llllIl11;
//    private final String do1 = Build.VERSION.RELEASE;
//    private final String do2 = String.valueOf(Build.VERSION.SDK_INT);
//    private String dbra;                                                    // Build.BRAND  手机品牌
//    private final String dmf = Build.MANUFACTURER;
//    private final String dme = Build.MODEL;
//    private final String dhw = Build.HARDWARE;
//    private String ifaid = null;
//    private final String src = "android";
//
//    public BaseDeviceInfo(String packageCodePath, String brand, String buildId) {
//        this.dadir = packageCodePath;
//        this.dat = String.valueOf(System.currentTimeMillis());
//        this.dbra = brand;
//        this.dbt = String.valueOf(System.currentTimeMillis());
//        // 电池信息
//        // level,voltage,temperature,status,plugged
//        // 电量,电压,电池温度,充电状态(1: 未知 2: 充电中 3: 放电中 4: 未充电 5: 电池满) ,充电设备(1: 充电器 2: USB 4: 无线充电)
//        this.dbs = String.format("%s,%s,%s,%s,%s,%s", 100, 4337, 320, 3, 0, System.currentTimeMillis());
//        this.dbuid = buildId;
//
//    }
//
//    /* JADX INFO: Access modifiers changed from: private */
//    /* renamed from: a */
//    public /* synthetic */ void b(Context context, CountDownLatch countDownLatch) {
//        this.dcpui = l1l11II111I1.ll1lIll1II11();
//        this.dcpu = l1l11II111I1.Ill11llllIl11;
//        this.dcpuf = l1l11II111I1.Il11IIl1lll;
//        this.lism = l1lIIl11l111l.ll1lIll1II11(context).booleanValue() ? "1" : "0";
//    }
//
//    private String getStringValue() throws IllegalAccessException {
//        StringBuilder sb = new StringBuilder();
//        for (Class<?> cls = getClass(); cls != null; cls = cls.getSuperclass()) {
//            if ("java.lang.object".equalsIgnoreCase(cls.getName())) {
//                break;
//            }
//            Field[] declaredFields = cls.getDeclaredFields();
//            if (declaredFields.length > 0) {
//                for (Field field : declaredFields) {
//                    field.setAccessible(true);
//                    Object obj = field.get(this);
//                    if (obj != null) {
//                        String replace = obj.toString().replace((char) 28, ' ').replace((char) 29, ' ');
//                        if ("do1".equals(field.getName())) {
//                            sb.append("do");
//                            sb.append((char) 28);
//                        } else {
//                            sb.append(field.getName());
//                            sb.append((char) 28);
//                        }
//                        sb.append(replace);
//                        sb.append((char) 29);
//                    }
//                }
//            }
//        }
//        return sb.toString();
//    }
//
//    public String buildBaseDevice(final Context context, String shumeiId) throws IllegalAccessException {
//        SensorManager sensorManager;
//        Sensor defaultSensor;
//        String str = "0";
//        str = "1";
//        String str2 = "";
//        final CountDownLatch countDownLatch = new CountDownLatch(3);
//        long currentTimeMillis = System.currentTimeMillis();
//        GyrData gyrData = I1I1lIllIllII.ll1lIll1II11;
//        I1I1lIllIllII.Il11IIl1lll.set(0);
//        Context context2 = SZStone.sContext;
//        if (!(context2 == null || (sensorManager = (SensorManager) context2.getSystemService("sensor")) == null || (defaultSensor = sensorManager.getDefaultSensor(4)) == null)) {
//            sensorManager.registerListener(I1I1lIllIllII.I11I11lll1I1, defaultSensor, 0);
//        }
//
//        this.dtu = III1IIll11lI.Il1l1IIIII;
////        this.bcn = III1IIll11lI.l111111l1III;
//        this.bcn = "dewu";
////        this.bssm = III1IIll11lI.III1IIll11lI;
//        this.bssm = shumeiId;
//        ThreadPoolExecutor threadPoolExecutor = I1I111l1llI.I11I11lll1I1;
//        if (threadPoolExecutor != null) {
//            threadPoolExecutor.execute(new Runnable() { // from class: k.c.e.m.a
//                @Override // java.lang.Runnable
//                public final void run() {
//                    BaseDeviceInfo.this.b(context, countDownLatch);
//                }
//            });
//        }
//        ThreadPoolExecutor threadPoolExecutor2 = I1I111l1llI.I11I11lll1I1;
//        if (threadPoolExecutor2 != null) {
//            threadPoolExecutor2.execute(new Runnable() { // from class: k.c.e.m.c
//                @Override // java.lang.Runnable
//                public final void run() {
//                    BaseDeviceInfo.this.d(context, countDownLatch);
//                }
//            });
//        }
//        ThreadPoolExecutor threadPoolExecutor3 = I1I111l1llI.I11I11lll1I1;
//        if (threadPoolExecutor3 != null) {
//            threadPoolExecutor3.execute(new Runnable() { // from class: k.c.e.m.b
//                @Override // java.lang.Runnable
//                public final void run() {
//                    BaseDeviceInfo.this.f(context, countDownLatch);
//                }
//            });
//        }
//        if (countDownLatch.await(2200L, TimeUnit.MILLISECONDS)) {
//        }
//        this.awt = str;
//        this.dgyro = I1I1lIllIllII.ll1lIll1II11.toString();
//        I1I1lIllIllII.ll1lIll1II11();
//        this.tct = Ill11llllIl11.ll1lIll1II11();
//        this.tcst = String.valueOf(System.currentTimeMillis() - currentTimeMillis);
//        str2 = getStringValue();
//        III1IIll11lI.II1IlIlllI = str2;
//        return str2;
//    }
//}
