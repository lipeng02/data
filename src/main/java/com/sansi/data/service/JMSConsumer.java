package com.sansi.data.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sansi.data.config.JMSConfig;
import com.sansi.data.dao.AlarmDataMapper;
import com.sansi.data.dao.AlarmEarlyMapper;
import com.sansi.data.dao.PeriodWeightMapper;
import com.sansi.data.entity.AlarmData;
import com.sansi.data.entity.AlarmEarly;
import com.sansi.data.entity.PeriodWeight;
import com.sansi.data.utils.AESUtils;
import com.sansi.data.utils.DataFormat;
import com.sansi.data.utils.DataParser;
import com.sansi.data.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Intro
 * @Author LeePong
 * @Date 2020/4/24 16:20
 */
@Slf4j
@Component
public class JMSConsumer {
    //兰联科技
    private final static String REALTIME_URL = "http://jtjx.lanlianyun.cn/jqjsbl/excutejqjsbl";
    private final static String ALARM_URL = "http://jtjx.lanlianyun.cn/sbyc/excuteSbyc";
    private final static String HEART_URL = "http://jtjx.lanlianyun.cn/sbxt/excuteSbxt";
    private final static String SBKID = "000000007af5ebc8017b0459665929b3";

    //简道云 20210703003 架桥机上传接口
    private final static String jiaqiaoji = "https://api.jiandaoyun.com/api/v3/app/6108a947cbc591000884aa19/entry/6108a94f69558c00072f8b55/data_create";
    //简道云 20210703003 塔机上传接口
    private final static String taji = "https://api.jiandaoyun.com/api/v3/app/6108a947cbc591000884aa19/entry/612355336191730008ea9bdf/data_create";

    //存放状态数据的
    Map map = new LinkedHashMap();
    //存放报警数据
    Map map_1 = new LinkedHashMap();
    @Autowired
    private DataParser dataParser;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private AlarmDataMapper alarmDataMapper;
    @Autowired
    private AlarmEarlyMapper alarmEarlyMapper;
    @Autowired
    private PeriodWeightMapper periodWeightMapper;

    //消息队列监听器
    @JmsListener(destination = JMSConfig.QUEUE, containerFactory = "jmsListenerContainerQueue")
    public void onQueueMessage(String msg) {
        log.info("接收到queue消息：{}", msg);
    }

    //发布订阅监听器
    @JmsListener(destination = JMSConfig.TOPIC, containerFactory = "jmsListenerContainerTopic")
    //MQTT协议数据解析
    public void onTopicMessage(Map hashMap) {
        String topic = (String) hashMap.get("topic");
        byte[] payload = (byte[]) hashMap.get("payload");
        String[] hexArr = DataFormat.byteArrToHexArr(payload);
//        String[] hexArr = DataFormat.strToHexArr("247300C874323130373032303031000100010000000000000000000000000000000000000000000001F9009000000000000000007F057D36780478E582EA7F33784075317F4C7DEE791B76BE850B850D67B7808C7FF9000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008002780000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        int[] intArr = DataFormat.hexArrToInt(hexArr);
        try {

//        //CRC16校验
//        String[] copy = Arrays.copyOf(hexArr, 196);
//        byte[] dd = Crc16Util.getData(copy);
//        String s = Crc16Util.byteTo16String(dd).toUpperCase();
//
//        //截取最后四位16进制数据
//        StringBuilder sb = new StringBuilder();
//        String a = s.substring(s.length() - 4, s.length()).trim();
//        String b = s.substring(s.length() - 6, s.length() - 4).trim();
//        StringBuilder result = sb.append(b).append(a);
//
//        String hexOgn = (hexArr[198] + hexArr[199]).toUpperCase();
//        System.out.println("CRC16校验值:   " + result + "      " + hexOgn);

            if (hexArr.length == 200 /*&& result.equals(hexOgn)*/) {
                //数据对接到兰联科技
                if (topic.equals("t210717002")) {
                    //设备实时数据
                    JSONObject jsonRealtime = new JSONObject();
                    //工作循环时间s
                    jsonRealtime.put("gzxhsj", (int) Long.parseLong("" + hexArr[156] + hexArr[157] + hexArr[158] + hexArr[159], 16) & 0xFFFFFFFF);
                    //工作循环次数
                    jsonRealtime.put("gzxhcs", (int) Long.parseLong("" + hexArr[160] + hexArr[161] + hexArr[162] + hexArr[163], 16) & 0xFFFFFFFF);
                    //前小车水平度
                    jsonRealtime.put("qxcspd", (double) (Integer.parseInt("" + hexArr[40] + hexArr[41], 16)) / 100);
                    //过孔距离 m
                    jsonRealtime.put("gkjl", 0);
                    //支腿X轴倾角
                    jsonRealtime.put("ztxqj", (double) (Integer.parseInt("" + hexArr[44] + hexArr[45], 16)) / 100);
                    //支腿Y轴倾角
                    jsonRealtime.put("ztyqj", (double) (Integer.parseInt("" + hexArr[46] + hexArr[47], 16)) / 100);
                    //总重量
                    jsonRealtime.put("zzl", ((double) (Integer.parseInt("" + hexArr[20] + hexArr[21], 16)) / 100) + ((double) (Integer.parseInt("" + hexArr[26] + hexArr[27], 16)) / 100));
                    //额定重量
                    jsonRealtime.put("edzl", 160);
                    //前小车高度
                    jsonRealtime.put("qxcgd", (double) (Integer.parseInt("" + hexArr[22] + hexArr[23], 16)) / 100);
                    //前小车横移距离
                    jsonRealtime.put("qxchyjl", 0);
                    //前小车纵移距离
                    jsonRealtime.put("qxczyjl", (double) (Integer.parseInt("" + hexArr[34] + hexArr[35], 16)) / 100);
                    //前小车重量
                    jsonRealtime.put("qxczl", (double) (Integer.parseInt("" + hexArr[20] + hexArr[21], 16)) / 100);
                    //风速
                    jsonRealtime.put("fs", (double) (Integer.parseInt("" + hexArr[38] + hexArr[39], 16)) / 100);
                    //后小车高度
                    jsonRealtime.put("hxcgd", (double) (Integer.parseInt("" + hexArr[28] + hexArr[29], 16)) / 100);
                    //后小车横移距离
                    jsonRealtime.put("hxchyjl", 0);
                    //后小车纵移距离
                    jsonRealtime.put("hxczljl", (double) (Integer.parseInt("" + hexArr[36] + hexArr[37], 16)) / 100);
                    //后小车重量
                    jsonRealtime.put("hxczl", (double) (Integer.parseInt("" + hexArr[26] + hexArr[27], 16)) / 100);
                    String info_realtime = AESUtils.encryptjm(jsonRealtime.toJSONString(), SBKID);
                    MultiValueMap<String, String> postParams = new LinkedMultiValueMap<String, String>();
                    postParams.add("sbkId", SBKID);
                    postParams.add("info", info_realtime);
                    String resultRealtime = restTemplate.postForObject(REALTIME_URL, postParams, String.class);

                    //设备异常数据
                    JSONObject jsonAlarm = new JSONObject();
                    //水平角度报警(0:正常1:报警)
                    jsonAlarm.put("sbjdbj", DataFormat.decToBin(intArr[152])[4]);
                    //前车超载预警(0:正常1:预警)
                    jsonAlarm.put("qcczyj", 0);
                    //前车超载报警(0:正常1:报警)
                    jsonAlarm.put("qcczbj", DataFormat.decToBin(intArr[151])[1]);
                    //后车超载预警(0:正常1:预警)
                    jsonAlarm.put("hcczyj", 0);
                    //后车超载报警(0:正常1:预警)
                    jsonAlarm.put("hcczbj", DataFormat.decToBin(intArr[152])[7]);
                    //风速报警(0:正常1:预警)
                    jsonAlarm.put("fsbj", DataFormat.decToBin(intArr[152])[5]);
                    //倾角报警(0:正常1:预警)
                    jsonAlarm.put("qjbj", DataFormat.decToBin(intArr[152])[2]);
                    String infoAlarm = AESUtils.encryptjm(jsonAlarm.toJSONString(), SBKID);
                    MultiValueMap<String, String> postParamsAlarm = new LinkedMultiValueMap<String, String>();
                    postParamsAlarm.add("sbkId", SBKID);
                    postParamsAlarm.add("info", infoAlarm);
                    String resultAlarm = restTemplate.postForObject(ALARM_URL, postParamsAlarm, String.class);

                    //设备心跳数据
                    JSONObject jsonHeart = new JSONObject();
                    //设备备案号
                    jsonHeart.put("registNo", topic);
                    //黑匣子序列号
                    jsonHeart.put("serialNo", "0");
                    //上传时间
                    jsonHeart.put("sendTime", DataFormat.dateFormat(new Date()));
                    String infoHeart = AESUtils.encryptjm(jsonHeart.toJSONString(), SBKID);
                    MultiValueMap<String, String> postParamsHeart = new LinkedMultiValueMap<String, String>();
                    postParamsHeart.add("sbkId", SBKID);
                    postParamsHeart.add("info", infoAlarm);
                    String resultHeart = restTemplate.postForObject(HEART_URL, postParamsHeart, String.class);

                } else {  //数据存进redis
                    map.put("datetime", DataFormat.dateFormat(new Date()));
                    //帧头
                    map.put("A0_1", "" + (char) intArr[0] + (char) intArr[1]);
                    map.put("A2_3", intArr[2] + intArr[3]);
                    map.put("A4_13", topic);
                    map.put("A14_15", (double) (Integer.parseInt("" + hexArr[14] + hexArr[15], 16)) / 100);
                    map.put("A16_17", (double) (Integer.parseInt("" + hexArr[16] + hexArr[17], 16)) / 100);
                    map.put("A18_19", (double) (Integer.parseInt("" + hexArr[18] + hexArr[19], 16)) / 100);

                    //状态数据
                    map.put("A20_21", (double) (Integer.parseInt("" + hexArr[20] + hexArr[21], 16)) / 100);
                    map.put("A22_23", (double) (Integer.parseInt("" + hexArr[22] + hexArr[23], 16)) / 100);
                    map.put("A24_25", (double) (Integer.parseInt("" + hexArr[24] + hexArr[25], 16)) / 100);
                    map.put("A26_27", (double) (Integer.parseInt("" + hexArr[26] + hexArr[27], 16)) / 100);
                    map.put("A28_29", (double) (Integer.parseInt("" + hexArr[28] + hexArr[29], 16)) / 100);
                    map.put("A30_31", (double) (Integer.parseInt("" + hexArr[30] + hexArr[31], 16)) / 100);
                    map.put("A32_33", (double) (Integer.parseInt("" + hexArr[32] + hexArr[33], 16)) / 100);
                    map.put("A34_35", (double) (Integer.parseInt("" + hexArr[34] + hexArr[35], 16)) / 100);
                    map.put("A36_37", (double) (Integer.parseInt("" + hexArr[36] + hexArr[37], 16)) / 100);
                    map.put("A38_39", (double) (Integer.parseInt("" + hexArr[38] + hexArr[39], 16)) / 100);
                    map.put("A40_41", (double) (Integer.parseInt("" + hexArr[40] + hexArr[41], 16)) / 100);
                    map.put("A42_43", (double) (Integer.parseInt("" + hexArr[42] + hexArr[43], 16)) / 100);
                    map.put("A44_45", (double) (Integer.parseInt("" + hexArr[44] + hexArr[45], 16)) / 100);
                    map.put("A46_47", (double) (Integer.parseInt("" + hexArr[46] + hexArr[47], 16)) / 100);
                    map.put("A48_49", (double) (Integer.parseInt("" + hexArr[48] + hexArr[49], 16)) / 100);
                    map.put("A50_51", (double) (Integer.parseInt("" + hexArr[50] + hexArr[51], 16)) / 100);
                    map.put("A52_53", (double) (Integer.parseInt("" + hexArr[52] + hexArr[53], 16)) / 100);
                    map.put("A54_55", (double) (Integer.parseInt("" + hexArr[54] + hexArr[55], 16)) / 100);
                    map.put("A56_57", (double) (Integer.parseInt("" + hexArr[56] + hexArr[57], 16)) / 100);
                    map.put("A58_59", (double) (Integer.parseInt("" + hexArr[58] + hexArr[59], 16)) / 100);
                    map.put("A60_61", (double) (Integer.parseInt("" + hexArr[60] + hexArr[61], 16)) / 100);
                    map.put("A62_63", (double) (Integer.parseInt("" + hexArr[62] + hexArr[63], 16)) / 100);
                    map.put("A64_65", (double) (Integer.parseInt("" + hexArr[64] + hexArr[65], 16)) / 100);
                    map.put("A66_67", (double) (Integer.parseInt("" + hexArr[66] + hexArr[67], 16)) / 100);
                    map.put("A68_69", (double) (Integer.parseInt("" + hexArr[68] + hexArr[69], 16)) / 100);
                    map.put("A70_71", (double) (Integer.parseInt("" + hexArr[70] + hexArr[71], 16)) / 100);
                    map.put("A72_73", (double) (Integer.parseInt("" + hexArr[72] + hexArr[73], 16)) / 100);
                    map.put("A74_75", (double) (Integer.parseInt("" + hexArr[74] + hexArr[75], 16)) / 100);
                    map.put("A76_77", (double) (Integer.parseInt("" + hexArr[76] + hexArr[77], 16)) / 100);
                    map.put("A78_79", (double) (Integer.parseInt("" + hexArr[78] + hexArr[79], 16)) / 100);
                    map.put("A80_81", (double) (Integer.parseInt("" + hexArr[80] + hexArr[81], 16)) / 100);
                    map.put("A82_83", (double) (Integer.parseInt("" + hexArr[82] + hexArr[83], 16)) / 100);
                    map.put("A84_85", (double) (Integer.parseInt("" + hexArr[84] + hexArr[85], 16)) / 100);
                    map.put("A86_87", (double) (Integer.parseInt("" + hexArr[86] + hexArr[87], 16)) / 100);
                    map.put("A88_89", (double) (Integer.parseInt("" + hexArr[88] + hexArr[89], 16)) / 100);
                    map.put("A90_91", (double) (Integer.parseInt("" + hexArr[90] + hexArr[91], 16)) / 100);
                    map.put("A92_93", (double) (Integer.parseInt("" + hexArr[92] + hexArr[93], 16)) / 100);
                    map.put("A94_95", (double) (Integer.parseInt("" + hexArr[94] + hexArr[95], 16)) / 100);
                    map.put("A96_97", (double) (Integer.parseInt("" + hexArr[96] + hexArr[97], 16)) / 100);
                    map.put("A98_99", (double) (Integer.parseInt("" + hexArr[98] + hexArr[99], 16)) / 100);
                    map.put("A100_101", (double) (Integer.parseInt("" + hexArr[100] + hexArr[101], 16)) / 100);
                    map.put("A102_103", (double) (Integer.parseInt("" + hexArr[102] + hexArr[103], 16)) / 100);
                    map.put("A104_105", (double) (Integer.parseInt("" + hexArr[104] + hexArr[105], 16)) / 100);
                    map.put("A106_107", (double) (Integer.parseInt("" + hexArr[106] + hexArr[107], 16)) / 100);
                    map.put("A108_109", (double) (Integer.parseInt("" + hexArr[108] + hexArr[109], 16)) / 100);
                    map.put("A110_111", (double) (Integer.parseInt("" + hexArr[110] + hexArr[111], 16)) / 100);
                    map.put("A112_113", (double) (Integer.parseInt("" + hexArr[112] + hexArr[113], 16)) / 100);
                    map.put("A114_115", (double) (Integer.parseInt("" + hexArr[114] + hexArr[115], 16)) / 100);
                    map.put("A116_117", (double) (Integer.parseInt("" + hexArr[116] + hexArr[117], 16)) / 100);
                    map.put("A118_119", (double) (Integer.parseInt("" + hexArr[118] + hexArr[119], 16)) / 100);
                    map.put("A120_121", (double) (Integer.parseInt("" + hexArr[120] + hexArr[121], 16)) / 100);
                    map.put("A122_123", (double) (Integer.parseInt("" + hexArr[122] + hexArr[123], 16)) / 100);
                    map.put("A124_125", (double) (Integer.parseInt("" + hexArr[124] + hexArr[125], 16)) / 100);
                    map.put("A126_127", (double) (Integer.parseInt("" + hexArr[126] + hexArr[127], 16)) / 100);
                    map.put("A128_129", (double) (Integer.parseInt("" + hexArr[128] + hexArr[129], 16)) / 100);
                    map.put("A130_131", (double) (Integer.parseInt("" + hexArr[130] + hexArr[131], 16)) / 100);
                    map.put("A132_133", (double) (Integer.parseInt("" + hexArr[132] + hexArr[133], 16)) / 100);
                    map.put("A134_135", (double) (Integer.parseInt("" + hexArr[134] + hexArr[135], 16)) / 100);
                    map.put("A136_137", (double) (Integer.parseInt("" + hexArr[136] + hexArr[137], 16)) / 100);
                    map.put("A138_139", (double) (Integer.parseInt("" + hexArr[138] + hexArr[139], 16)) / 100);
                    map.put("A140_141", (double) (Integer.parseInt("" + hexArr[140] + hexArr[141], 16)) / 100);
                    map.put("A142_143", (double) (Integer.parseInt("" + hexArr[142] + hexArr[143], 16)) / 100);
                    map.put("A144_145", (double) (Integer.parseInt("" + hexArr[144] + hexArr[145], 16)) / 100);
                    map.put("A146_147", (double) (Integer.parseInt("" + hexArr[146] + hexArr[147], 16)) / 100);
                    //开关量
                    String[] A148 = DataFormat.decToBin(intArr[148]);
                    map.put("A1480", A148[7]);
                    map.put("A1481", A148[6]);
                    map.put("A1482", A148[5]);
                    map.put("A1483", A148[4]);
                    map.put("A1484", A148[3]);
                    map.put("A1485", A148[2]);
                    map.put("A1486", A148[1]);
                    map.put("A1487", A148[0]);

                    String[] A149 = DataFormat.decToBin(intArr[149]);
                    map.put("A1490", A149[7]);
                    map.put("A1491", A149[6]);
                    map.put("A1492", A149[5]);
                    map.put("A1493", A149[4]);
                    map.put("A1494", A149[3]);
                    map.put("A1495", A149[2]);
                    map.put("A1496", A149[1]);
                    map.put("A1497", A149[0]);

                    String[] A150 = DataFormat.decToBin(intArr[150]);
                    map.put("A1500", A150[7]);
                    map.put("A1501", A150[6]);
                    map.put("A1502", A150[5]);
                    map.put("A1503", A150[4]);
                    map.put("A1504", A150[3]);
                    map.put("A1505", A150[2]);
                    map.put("A1506", A150[1]);
                    map.put("A1507", A150[0]);

                    String[] A151 = DataFormat.decToBin(intArr[151]);
                    map.put("A1510", A151[7]);
                    map.put("A1511", A151[6]);
                    map.put("A1512", A151[5]);
                    map.put("A1513", A151[4]);
                    map.put("A1514", A151[3]);
                    map.put("A1515", A151[2]);
                    map.put("A1516", A151[1]);
                    map.put("A1517", A151[0]);

                    String[] A152 = DataFormat.decToBin(intArr[152]);
                    map.put("A1520", A152[7]);
                    map.put("A1521", A152[6]);
                    map.put("A1522", A152[5]);
                    map.put("A1523", A152[4]);
                    map.put("A1524", A152[3]);
                    map.put("A1525", A152[2]);
                    map.put("A1526", A152[1]);
                    map.put("A1527", A152[0]);

                    String[] A153 = DataFormat.decToBin(intArr[153]);
                    map.put("A1530", A153[7]);
                    map.put("A1531", A153[6]);
                    map.put("A1532", A153[5]);
                    map.put("A1533", A153[4]);
                    map.put("A1534", A153[3]);
                    map.put("A1535", A153[2]);
                    map.put("A1536", A153[1]);
                    map.put("A1537", A153[0]);

                    String[] A154 = DataFormat.decToBin(intArr[154]);
                    map.put("A1540", A154[7]);
                    map.put("A1541", A154[6]);
                    map.put("A1542", A154[5]);
                    map.put("A1543", A154[4]);
                    map.put("A1544", A154[3]);
                    map.put("A1545", A154[2]);
                    map.put("A1546", A154[1]);
                    map.put("A1547", A154[0]);

                    String[] A155 = DataFormat.decToBin(intArr[155]);
                    map.put("A1550", A155[7]);
                    map.put("A1551", A155[6]);
                    map.put("A1552", A155[5]);
                    map.put("A1553", A155[4]);
                    map.put("A1554", A155[3]);
                    map.put("A1555", A155[2]);
                    map.put("A1556", A155[1]);
                    map.put("A1557", A155[0]);

                    //累计工作时间
                    map.put("A156_159", (int) Long.parseLong("" + hexArr[156] + hexArr[157] + hexArr[158] + hexArr[159], 16) & 0xFFFFFFFF);
                    //累计工作次数
                    map.put("A160_163", (int) Long.parseLong("" + hexArr[160] + hexArr[161] + hexArr[162] + hexArr[163], 16) & 0xFFFFFFFF);
                    map.put("A164_167", (double) Long.parseLong("" + hexArr[164] + hexArr[165] + hexArr[166] + hexArr[167], 16) / 1000000);
                    map.put("A168_171", (double) Long.parseLong("" + hexArr[168] + hexArr[169] + hexArr[170] + hexArr[171], 16) / 1000000);
                    //因接受dtu的数据有误,解析出来数值过大,现采用写死数据(甘肃路桥)
//                map.put("A164_167", (double) Long.parseLong("065AD638", 16) / 1000000);
//                map.put("A168_171", (double) Long.parseLong("022B5BE0", 16) / 1000000);
                    map.put("A172_175", (double) (Long.parseLong("" + hexArr[172] + hexArr[173] + hexArr[174] + hexArr[175], 16)) / 100);
                    map.put("A176_179", (double) (Long.parseLong("" + hexArr[176] + hexArr[177] + hexArr[178] + hexArr[179], 16)) / 100);
                    map.put("A180_183", (double) (Long.parseLong("" + hexArr[180] + hexArr[181] + hexArr[182] + hexArr[183], 16)) / 100);
                    map.put("A184_187", (double) (Long.parseLong("" + hexArr[184] + hexArr[185] + hexArr[186] + hexArr[187], 16)) / 100);
                    map.put("A188_191", (double) (Long.parseLong("" + hexArr[188] + hexArr[189] + hexArr[190] + hexArr[191], 16)) / 100);
                    map.put("A192_195", (double) (Long.parseLong("" + hexArr[192] + hexArr[193] + hexArr[194] + hexArr[195], 16)) / 100);
                    map.put("A196_197", (double) (Integer.parseInt("" + hexArr[196] + hexArr[197], 16)) / 100);

                    //帧尾  原CRC16的值,不设置为0.0会导致点击App定位闪退
                    map.put("A198_199", ((double) 0.0));

                    redisTemplate.opsForValue().set(topic, map);
                    log.info("设备号: {}  {}",topic ,hexArr.length);

                    //先存到redis,再发送到简道云 12-24 应客户要求取消简道云平台的上传
                    /*if (topic.equals("t210703003")) {
                        //架桥机设备实时数据
                        JSONObject jsonRealtime = new JSONObject();
                        JSONObject json1 = new JSONObject();
                        JSONObject json2 = new JSONObject();
                        JSONObject json3 = new JSONObject();
                        JSONObject json4 = new JSONObject();
                        JSONObject json5 = new JSONObject();
                        JSONObject json6 = new JSONObject();
                        JSONObject json7 = new JSONObject();
                        JSONObject json8 = new JSONObject();
                        JSONObject json9 = new JSONObject();
                        JSONObject json10 = new JSONObject();
                        JSONObject json11 = new JSONObject();
                        JSONObject json12 = new JSONObject();
                        JSONObject json13 = new JSONObject();
                        JSONObject json14 = new JSONObject();
                        JSONObject json15 = new JSONObject();
                        JSONObject json16 = new JSONObject();
                        JSONObject json17 = new JSONObject();

                        json1.put("value", (double) (Integer.parseInt("" + hexArr[20] + hexArr[21], 16)) / 100);
                        json2.put("value", (double) (Integer.parseInt("" + hexArr[22] + hexArr[23], 16)) / 100);
                        json3.put("value", (double) (Integer.parseInt("" + hexArr[24] + hexArr[25], 16)) / 100);
                        json4.put("value", (double) (Integer.parseInt("" + hexArr[26] + hexArr[27], 16)) / 100);
                        json5.put("value", (double) (Integer.parseInt("" + hexArr[28] + hexArr[29], 16)) / 100);
                        json6.put("value", (double) (Integer.parseInt("" + hexArr[30] + hexArr[31], 16)) / 100);
                        json7.put("value", (double) (Integer.parseInt("" + hexArr[32] + hexArr[33], 16)) / 100);
                        json8.put("value", (double) (Integer.parseInt("" + hexArr[34] + hexArr[35], 16)) / 100);
                        json9.put("value", (double) (Integer.parseInt("" + hexArr[36] + hexArr[37], 16)) / 100);
                        json10.put("value", (int) Integer.parseInt("" + hexArr[38] + hexArr[39], 16) / 100);
                        json11.put("value", (double) (Integer.parseInt("" + hexArr[40] + hexArr[41], 16)) / 100);
                        json12.put("value", (double) (Integer.parseInt("" + hexArr[42] + hexArr[43], 16)) / 100);
                        json13.put("value", (double) (Integer.parseInt("" + hexArr[44] + hexArr[45], 16)) / 100);
                        json14.put("value", (double) (Integer.parseInt("" + hexArr[46] + hexArr[47], 16)) / 100);
                        json15.put("value", (int) (Long.parseLong("" + hexArr[156] + hexArr[157] + hexArr[158] + hexArr[159], 16) / 3600) & 0xFFFFFFFF);
                        json16.put("value", (int) Long.parseLong("" + hexArr[160] + hexArr[161] + hexArr[162] + hexArr[163], 16) & 0xFFFFFFFF);
                        json17.put("value", DataFormat.dateFormat(new Date()));
                        jsonRealtime.put("_widget_1627978240445", json1);
                        jsonRealtime.put("_widget_1627978240639", json2);
                        jsonRealtime.put("_widget_1627978240659", json3);
                        jsonRealtime.put("_widget_1627978240679", json4);
                        jsonRealtime.put("_widget_1627978240699", json5);
                        jsonRealtime.put("_widget_1627978240773", json6);
                        jsonRealtime.put("_widget_1627978240793", json7);
                        jsonRealtime.put("_widget_1627978240831", json8);
                        jsonRealtime.put("_widget_1627978240851", json9);
                        jsonRealtime.put("_widget_1627978240871", json10);
                        jsonRealtime.put("_widget_1627978240999", json11);
                        jsonRealtime.put("_widget_1627978241019", json12);
                        jsonRealtime.put("_widget_1627978241057", json13);
                        jsonRealtime.put("_widget_1627978241095", json14);
                        jsonRealtime.put("_widget_1627978241133", json15);
                        jsonRealtime.put("_widget_1627978241155", json16);
                        jsonRealtime.put("_widget_1627978241194", json17);

                        JSONObject jsonUpload = new JSONObject();
                        jsonUpload.put("data", jsonRealtime);

                        // 使用postForEntity请求接口
                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Authorization", "Bearer yBij5jvzy4i21UBfvFBrbcmbk6GP6Iz3");
                        HttpEntity<JSONObject> httpEntity = new HttpEntity<JSONObject>(jsonUpload, headers);
                        ResponseEntity<String> response = restTemplate.postForEntity(jiaqiaoji, httpEntity, String.class);

                    }
                    if (topic.equals("t210629001")) {
                        //塔吊设备实时数据
                        JSONObject jsonRealtime = new JSONObject();
                        JSONObject json1 = new JSONObject();
                        JSONObject json2 = new JSONObject();
                        JSONObject json3 = new JSONObject();
                        JSONObject json4 = new JSONObject();
                        JSONObject json5 = new JSONObject();
                        JSONObject json6 = new JSONObject();
                        JSONObject json7 = new JSONObject();
                        JSONObject json8 = new JSONObject();
                        JSONObject json9 = new JSONObject();
                        JSONObject json10 = new JSONObject();
                        JSONObject json11 = new JSONObject();
                        JSONObject json12 = new JSONObject();
                        JSONObject json13 = new JSONObject();
                        JSONObject json14 = new JSONObject();
                        JSONObject json15 = new JSONObject();
                        JSONObject json16 = new JSONObject();
                        JSONObject json17 = new JSONObject();
                        JSONObject json18 = new JSONObject();
                        JSONObject json19 = new JSONObject();
                        JSONObject json20 = new JSONObject();
                        JSONObject json21 = new JSONObject();
                        JSONObject json22 = new JSONObject();
                        JSONObject json23 = new JSONObject();
                        JSONObject json24 = new JSONObject();

                        json1.put("value", (double) (Integer.parseInt("" + hexArr[20] + hexArr[21], 16)) / 100);
                        json2.put("value", (double) (Integer.parseInt("" + hexArr[22] + hexArr[23], 16)) / 100);
                        json3.put("value", (double) (Integer.parseInt("" + hexArr[24] + hexArr[25], 16)) / 100);
                        json4.put("value", (double) (Integer.parseInt("" + hexArr[26] + hexArr[27], 16)) / 100);
                        json5.put("value", (double) (Integer.parseInt("" + hexArr[28] + hexArr[29], 16)) / 100);
                        json6.put("value", (double) (Integer.parseInt("" + hexArr[30] + hexArr[31], 16)) / 100);
                        json7.put("value", (double) (Integer.parseInt("" + hexArr[32] + hexArr[33], 16)) / 100);
                        json8.put("value", (double) (Integer.parseInt("" + hexArr[34] + hexArr[35], 16)) / 100);
                        json9.put("value", (double) (Integer.parseInt("" + hexArr[36] + hexArr[37], 16)) / 100);
                        json10.put("value", (int) Integer.parseInt("" + hexArr[38] + hexArr[39], 16) / 100);
                        json11.put("value", (double) (Integer.parseInt("" + hexArr[40] + hexArr[41], 16)) / 100);
                        json12.put("value", (double) (Integer.parseInt("" + hexArr[42] + hexArr[43], 16)) / 100);
                        json13.put("value", (double) (Integer.parseInt("" + hexArr[44] + hexArr[45], 16)) / 100);
                        json14.put("value", (double) (Integer.parseInt("" + hexArr[46] + hexArr[47], 16)) / 100);
                        json15.put("value", (double) (Integer.parseInt("" + hexArr[48] + hexArr[49], 16)) / 100);
                        json16.put("value", (double) (Integer.parseInt("" + hexArr[50] + hexArr[51], 16)) / 100);
                        json17.put("value", (double) (Integer.parseInt("" + hexArr[52] + hexArr[53], 16)) / 100);
                        json18.put("value", (double) (Integer.parseInt("" + hexArr[54] + hexArr[55], 16)) / 100);
                        json19.put("value", (double) (Integer.parseInt("" + hexArr[56] + hexArr[57], 16)) / 100);
                        json20.put("value", (double) (Integer.parseInt("" + hexArr[58] + hexArr[59], 16)) / 100);
                        json21.put("value", (double) (Integer.parseInt("" + hexArr[60] + hexArr[61], 16)) / 100);
                        json22.put("value", (int) (Long.parseLong("" + hexArr[156] + hexArr[157] + hexArr[158] + hexArr[159], 16) / 3600) & 0xFFFFFFFF);
                        json23.put("value", (int) Long.parseLong("" + hexArr[160] + hexArr[161] + hexArr[162] + hexArr[163], 16) & 0xFFFFFFFF);
                        json24.put("value", (int) Long.parseLong("" + hexArr[172] + hexArr[173] + hexArr[174] + hexArr[175], 16) & 0xFFFFFFFF);
                        jsonRealtime.put("_widget_1629705523263", json1);
                        jsonRealtime.put("_widget_1629705523282", json2);
                        jsonRealtime.put("_widget_1629705523302", json3);
                        jsonRealtime.put("_widget_1629705523322", json4);
                        jsonRealtime.put("_widget_1629705523342", json5);
                        jsonRealtime.put("_widget_1629705523362", json6);
                        jsonRealtime.put("_widget_1629705523382", json7);
                        jsonRealtime.put("_widget_1629705523402", json8);
                        jsonRealtime.put("_widget_1629705523422", json9);
                        jsonRealtime.put("_widget_1629705523442", json10);
                        jsonRealtime.put("_widget_1629705523462", json11);
                        jsonRealtime.put("_widget_1629705523482", json12);
                        jsonRealtime.put("_widget_1629705523502", json13);
                        jsonRealtime.put("_widget_1629705523522", json14);
                        jsonRealtime.put("_widget_1629705523542", json15);
                        jsonRealtime.put("_widget_1629705523562", json16);
                        jsonRealtime.put("_widget_1629705523582", json17);
                        jsonRealtime.put("_widget_1629705523602", json18);
                        jsonRealtime.put("_widget_1629705523622", json19);
                        jsonRealtime.put("_widget_1629705523642", json20);
                        jsonRealtime.put("_widget_1629705523662", json21);
                        jsonRealtime.put("_widget_1629705523682", json22);
                        jsonRealtime.put("_widget_1629705523702", json23);
                        jsonRealtime.put("_widget_1629705523722", json24);

                        JSONObject jsonUpload = new JSONObject();
                        jsonUpload.put("data", jsonRealtime);

                        // 使用postForEntity请求接口
                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Authorization", "Bearer yBij5jvzy4i21UBfvFBrbcmbk6GP6Iz3");
                        HttpEntity<JSONObject> httpEntity = new HttpEntity<JSONObject>(jsonUpload, headers);
                        ResponseEntity<String> response = restTemplate.postForEntity(taji, httpEntity, String.class);

                    }*/
                }
            } else if (hexArr.length == 62) {

                //数据存进Redis
                List<String> topicList = new ArrayList<>();
                topicList.add("t210509001");
                topicList.add("t210519001");
                topicList.add("t210519002");
                topicList.add("t210519003");
                topicList.add("t210519004");
                topicList.add("t211213001");

                if (topicList.contains(topic)) {
                    map_1.put("datetime", DataFormat.dateFormat(new Date()));
                    //帧头
                    map_1.put("A0_1", "" + (char) intArr[0] + (char) intArr[1]);
                    map_1.put("A2_3", intArr[2] + intArr[3]);
                    map_1.put("A4_13", topic);
                    map_1.put("A14_15", (double) (Integer.parseInt("" + hexArr[14] + hexArr[15], 16)) / 100);
                    map_1.put("A16_17", (double) (Integer.parseInt("" + hexArr[16] + hexArr[17], 16)) / 100);
                    map_1.put("A18_19", (double) (Integer.parseInt("" + hexArr[18] + hexArr[19], 16)) / 100);

                    //报警周期数据
                    map_1.put("A20_27", "20" + hexArr[20] + "-" + hexArr[21] + "-" + hexArr[22] + " " + hexArr[23] + ":" + hexArr[24] + ":" + hexArr[25]);
                    map_1.put("A28_35", "20" + hexArr[28] + "-" + hexArr[29] + "-" + hexArr[30] + " " + hexArr[31] + ":" + hexArr[32] + ":" + hexArr[33]);
                    map_1.put("A36_39", Integer.parseInt("" + hexArr[36] + hexArr[37] + hexArr[38] + hexArr[39], 16));
                    map_1.put("A40_43", null);
                    map_1.put("A44_45", intArr[44] + intArr[45]);
                    //map_1.put("A46_57", "" + (char) intArr[46] + (char) intArr[47] + (char) intArr[48] + (char) intArr[49] + (char) intArr[50] + (char) intArr[51] + (char) intArr[52] + (char) intArr[53] + (char) intArr[54] + (char) intArr[55] + (char) intArr[56] + (char) intArr[57]);
                    map_1.put("A46_57", "");

                    //帧尾
                    map_1.put("A58_59", null);
                    map_1.put("A60_61", intArr[60] + intArr[61]);

                    if (("2000-00-00 00:00:00").equals(map_1.get("A28_35"))) {
                        if (topic.equals("t210509001"))
                            redisTemplate.opsForValue().set("5_" + topic, map_1, 10, TimeUnit.SECONDS);
                        else
                            redisTemplate.opsForValue().set("5_" + topic, map_1, 2, TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set("5_" + topic, map_1, 10, TimeUnit.SECONDS);
                    }
                    log.info("设备号: {}  {}",topic ,hexArr.length);
                }


                //报警数据存进MySQL
                //李希文塔机时间不发送的话,月份就会为00,当月份为00时,数据不做处理
                if (!hexArr[22].equals("00") && !hexArr[29].equals("00")) {
                    AlarmData alarmData = new AlarmData();
                    alarmData.setA0_1("" + (char) intArr[0] + (char) intArr[1]);
                    alarmData.setA2_3(intArr[2] + intArr[3]);
                    alarmData.setA4_13(topic);
                    alarmData.setA14_15((int) (double) (Integer.parseInt("" + hexArr[14] + hexArr[15], 16)) / 100);
                    alarmData.setA16_17((int) (double) (Integer.parseInt("" + hexArr[16] + hexArr[17], 16)) / 100);
                    alarmData.setA18_19((int) (double) (Integer.parseInt("" + hexArr[18] + hexArr[19], 16)) / 100);
                    //字符串转时间戳, 当字符串为无效日期时, 会报错(如 2000-00-00)
                    alarmData.setA20_27(Timestamp.valueOf("20" + "22" + "-" + hexArr[21] + "-" + hexArr[22] + " " + hexArr[23] + ":" + hexArr[24] + ":" + hexArr[25]));
                    alarmData.setA28_35(Timestamp.valueOf("20" + "22" + "-" + hexArr[29] + "-" + hexArr[30] + " " + hexArr[31] + ":" + hexArr[32] + ":" + hexArr[33]));
                    alarmData.setA36_39(Integer.parseInt("" + hexArr[36] + hexArr[37] + hexArr[38] + hexArr[39], 16));
                    alarmData.setA40_43((double) Integer.parseInt("" + hexArr[40] + hexArr[41] + hexArr[42] + hexArr[43], 16));
                    alarmData.setA44_45(intArr[44] + intArr[45]);
                    alarmData.setA46_57("");
                    alarmData.setA58_59(0);
                    alarmData.setA60_61(intArr[60] + intArr[61]);
                    alarmData.setBjid("0");
                    alarmData.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    alarmData.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                    int insert = alarmDataMapper.insert(alarmData);
                    log.info("设备号: {}  {}",topic ,hexArr.length);

                    AlarmEarly alarmEarly = new AlarmEarly();
                    alarmEarly.setEquipmentId(topic);
                    alarmEarly.setBjType(intArr[44] + intArr[45]);
                    alarmEarly.setBjInfo(null);
                    alarmEarlyMapper.insert(alarmEarly);
                }
            } else if (hexArr.length == 48) {
                //门座式吊重数据存进MySQL
                PeriodWeight periodWeight = new PeriodWeight();
                periodWeight.setTopic(topic);
                periodWeight.setStartTime(hexArr[21].equals("00") ? null : Timestamp.valueOf("20" + intArr[20] + "-" + intArr[21] + "-" + intArr[22] + " " + intArr[23] + ":" + intArr[24] + ":" + intArr[25]));
                periodWeight.setEndTime(hexArr[29].equals("00") ? null : Timestamp.valueOf("20" + intArr[28] + "-" + intArr[29] + "-" + intArr[30] + " " + intArr[31] + ":" + intArr[32] + ":" + intArr[33]));
                periodWeight.setWeight((float) Long.parseLong("" + hexArr[40] + hexArr[41] + hexArr[42] + hexArr[43], 16)/100);
                periodWeight.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                periodWeightMapper.insert(periodWeight);
            } else {
                log.warn("数据字节数有误：{}  长度:{}", topic, hexArr.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
