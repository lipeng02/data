package com.sansi.data.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sansi.data.dao.AlarmDataMapper;
import com.sansi.data.dao.AlarmEarlyMapper;
import com.sansi.data.entity.AlarmData;
import com.sansi.data.entity.AlarmEarly;
import com.sansi.data.entity.Location;
import com.sansi.data.entity.RequestInfo;
import com.sansi.data.service.ILocationService;
import com.sansi.data.service.JMSConsumer;
import com.sansi.data.service.MqttConsumer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class DataParser {
    //智慧通平台
    //测试接口
    private final static String TEST_URL = "http://39.108.190.47:5005/";
    //正式地址
    private final static String REAL_URL = "http://safe.zht2000.com:5008/";
    //获取token
    private final static String GET_TOKEN = "/api/services/app/Base/GetToken";
    //架桥机实时数据
    private final static String REALDATA_URL = "/api/services/app/BridgeM/Upload";
    //架桥机报警数据
    private final static String ALARMDATA_URL = "/api/services/app/BridgeM/Alarm";
    //架桥机报警阈值
    private final static String ALARMTHRESHOLD_URL = "/api/services/app/BridgeM/AlarmSetting";
    private final static String APP_ID = "hbsskj";
    private final static String APP_SECRET = "xcsdf09lo43";
    private final static String DEVICE1 = "JL01BMS01";

    //南京飞博
    private final static String serverUrl = "https://iot.zhinengjianshe.com/openapi";

    //存放状态数据的
    Map map = new LinkedHashMap();
    //存放报警数据
    Map map_1 = new LinkedHashMap();
    String topic = null;
    @Autowired
    private MqttConsumer mqttConsumer;
    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private AlarmDataMapper alarmDataMapper;
    @Autowired
    private AlarmEarlyMapper alarmEarlyMapper;
    @Autowired
    private ILocationService locationService;
    @Resource
    private RestTemplate restTemplate;

    //tcp的数据解析
    public void parseData(Object msg) {
        try {
            String[] hexArr = (String[]) msg;
            byte[] byteArr = (byte[]) msg;
            System.out.println("hexArr = " + hexArr);
            System.out.println("byteArr = " + byteArr);

            int[] intArr = DataFormat.hexArrToInt(hexArr);
            //16进制数组包含OD回车键,则是GPS数据
            if (Arrays.asList(hexArr).contains("0D")) {
                Location location = new Location();
                char[] chars = new char[intArr.length];
                for (int i = 0; i < intArr.length; i++) {
                    chars[i] = (char) intArr[i];
                }
                String[] splitArr = String.valueOf(chars).split("\r\n");
                for (String value : splitArr) {
                    String[] strArr = value.split(",");
                    switch (strArr[0]) {
                        case "$GPGGA":
                            break;
                        case "$GPRMC":
                            location.setLng(Double.parseDouble(strArr[5]) / 100);
                            location.setLat(Double.parseDouble(strArr[3]) / 100);
                            location.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                            break;
                        case "$GPFID":
                            location.setId(strArr[1]);
                            break;
                    }
                }
                if (location.getId() != null) {
                    locationService.saveOrUpdate(location);
                }
            } else {
                if (intArr.length > 13) {
                    topic = "" + (char) intArr[4] + (char) intArr[5] + (char) intArr[6] + (char) intArr[7]
                            + (char) intArr[8] + (char) intArr[9] + (char) intArr[10] + (char) intArr[11]
                            + (char) intArr[12] + (char) intArr[13];
                } else {
                    topic = "error";
                }
                if (hexArr.length == 200) {
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
                    log.info("设备号: {}  {}  tcp" ,topic ,hexArr.length);

                    //智慧通平台
                    if (topic.equals("t210913007")) {
                        String result = restTemplate.getForObject(REAL_URL + GET_TOKEN + "?appid=" + APP_ID + "&appsecret=" + APP_SECRET, String.class);
                        JSONObject jsonObject = JSONObject.parseObject(result);
                        String token = jsonObject.getJSONObject("result").getJSONObject("data").getString("accessToken");

                        //设备实时数据
                        JSONObject jsonRealtime = new JSONObject();
                        jsonRealtime.put("devId", DEVICE1);
                        jsonRealtime.put("dtime", DataFormat.dateFormat(new Date()));
                        jsonRealtime.put("main_1_pos", (double) (Integer.parseInt("" + hexArr[22] + hexArr[23], 16)) / 100);
                        jsonRealtime.put("leng_1_pos", (double) (Integer.parseInt("" + hexArr[34] + hexArr[35], 16)) / 100);
                        jsonRealtime.put("wid_1_pos", 0);
                        jsonRealtime.put("main_1_w", (double) (Integer.parseInt("" + hexArr[20] + hexArr[21], 16)) / 100);
                        jsonRealtime.put("main_2_pos", (double) (Integer.parseInt("" + hexArr[28] + hexArr[29], 16)) / 100);
                        jsonRealtime.put("leng_2_pos", (double) (Integer.parseInt("" + hexArr[36] + hexArr[37], 16)) / 100);
                        jsonRealtime.put("wid_2_pos", 0);
                        jsonRealtime.put("main_2_w", (double) (Integer.parseInt("" + hexArr[26] + hexArr[27], 16)) / 100);
                        jsonRealtime.put("wh_leng_pos", 0);
                        jsonRealtime.put("wh_wid_pos", (double) (Integer.parseInt("" + hexArr[32] + hexArr[33], 16)) / 100);
                        jsonRealtime.put("wind", 0);
                        jsonRealtime.put("windLevel", (int) (Integer.parseInt("" + hexArr[38] + hexArr[39], 16)) / 100);
                        jsonRealtime.put("main_1_speed", 0);
                        jsonRealtime.put("main_2_speed", 0);
                        jsonRealtime.put("level", (double) (Integer.parseInt("" + hexArr[40] + hexArr[41], 16)) / 100);
                        //垂直度除以的是10
                        jsonRealtime.put("vertical1", (double) (Integer.parseInt("" + hexArr[44] + hexArr[45], 16)) / 10);
                        jsonRealtime.put("vertical2", 0);
                        jsonRealtime.put("vertical_m_1", 0);
                        jsonRealtime.put("vertical_m_2", 0);
                        //垂直度除以的是10
                        jsonRealtime.put("vertical_b_1", (double) (Integer.parseInt("" + hexArr[46] + hexArr[47], 16)) / 10);
                        jsonRealtime.put("vertical_b_2", 0);
                        jsonRealtime.put("stress_f", 0);
                        jsonRealtime.put("stress_m", 0);
                        jsonRealtime.put("hookSpeed1", (double) (Integer.parseInt("" + hexArr[24] + hexArr[25], 16)) / 100);
                        jsonRealtime.put("verticalSpeed1", 0);
                        jsonRealtime.put("traverseSpeed1", 0);
                        jsonRealtime.put("hookSpeed2", (double) (Integer.parseInt("" + hexArr[28] + hexArr[29], 16)) / 100);
                        jsonRealtime.put("verticalSpeed2", 0);
                        jsonRealtime.put("traverseSpeed2", 0);
                        jsonRealtime.put("wholeTraverseSpeed", 0);
                        jsonRealtime.put("wholeVerticalSpeed", 0);
                        jsonRealtime.put("workingTime", 0);
                        jsonRealtime.put("allTime", (int) Long.parseLong("" + hexArr[156] + hexArr[157] + hexArr[158] + hexArr[159], 16) & 0xFFFFFFFF);
                        jsonRealtime.put("currentLoop", 0);
                        jsonRealtime.put("allLoop", (int) Long.parseLong("" + hexArr[160] + hexArr[161] + hexArr[162] + hexArr[163], 16) & 0xFFFFFFFF);
                        jsonRealtime.put("via", DataFormat.decToBin(intArr[149])[1]);
                        jsonRealtime.put("brakeStatus1", 0);
                        jsonRealtime.put("brakeStatus2", 0);
                        jsonRealtime.put("interlock", 0);
                        jsonRealtime.put("o_wholeAhead", 0);
                        jsonRealtime.put("o_wholeBack", 0);
                        jsonRealtime.put("o_wholeLeft", DataFormat.decToBin(intArr[148])[7]);
                        jsonRealtime.put("o_wholeRight", DataFormat.decToBin(intArr[148])[6]);
                        jsonRealtime.put("o_main_1_ahead", 0);
                        jsonRealtime.put("o_main_1_back", 0);
                        jsonRealtime.put("o_main_1_left", DataFormat.decToBin(intArr[149])[5]);
                        jsonRealtime.put("o_main_1_right", DataFormat.decToBin(intArr[149])[4]);
                        jsonRealtime.put("o_main_1_up", DataFormat.decToBin(intArr[148])[1]);
                        jsonRealtime.put("o_main_1_down", DataFormat.decToBin(intArr[148])[0]);
                        jsonRealtime.put("o_main_1_rotate", 0);
                        jsonRealtime.put("o_main_2_ahead", 0);
                        jsonRealtime.put("o_main_2_back", 0);
                        jsonRealtime.put("o_main_2_left", DataFormat.decToBin(intArr[149])[3]);
                        jsonRealtime.put("o_main_2_right", DataFormat.decToBin(intArr[149])[2]);
                        jsonRealtime.put("o_main_1_up", DataFormat.decToBin(intArr[149])[7]);
                        jsonRealtime.put("o_main_1_down", DataFormat.decToBin(intArr[149])[6]);
                        jsonRealtime.put("o_main_1_rotate", 0);
                        // 使用postForEntity请求接口
                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Authorization", "Bearer " + token);
                        HttpEntity<JSONObject> httpEntity = new HttpEntity<JSONObject>(jsonRealtime, headers);
                        ResponseEntity<String> response = restTemplate.postForEntity(REAL_URL + REALDATA_URL, httpEntity, String.class);
                    }

                } else if (hexArr.length == 62) {
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
                        alarmData.setA20_27(Timestamp.valueOf("20" + hexArr[20] + "-" + hexArr[21] + "-" + hexArr[22] + " " + hexArr[23] + ":" + hexArr[24] + ":" + hexArr[25]));
                        alarmData.setA28_35(Timestamp.valueOf("20" + hexArr[28] + "-" + hexArr[29] + "-" + hexArr[30] + " " + hexArr[31] + ":" + hexArr[32] + ":" + hexArr[33]));
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
                        log.info("设备号: {}  {}  tcp" ,topic ,hexArr.length);

                        AlarmEarly alarmEarly = new AlarmEarly();
                        alarmEarly.setEquipmentId(topic);
                        alarmEarly.setBjType(intArr[44] + intArr[45]);
                        alarmEarly.setBjInfo(null);
                        alarmEarlyMapper.insert(alarmEarly);
                    }

                    if (topic.equals("t210913007")) {
                        String result = restTemplate.getForObject(REAL_URL + GET_TOKEN + "?appid=" + APP_ID + "&appsecret=" + APP_SECRET, String.class);
                        JSONObject jsonObject = JSONObject.parseObject(result);
                        System.out.println("jsonObject = " + jsonObject);
                        String token = jsonObject.getJSONObject("result").getJSONObject("data").getString("accessToken");

                        //报警数据上传
                        JSONObject jsonAlarm = new JSONObject();
                        jsonAlarm.put("devId", DEVICE1);
                        jsonAlarm.put("type", 1);
                        jsonAlarm.put("startTime", "20" + hexArr[20] + "-" + hexArr[21] + "-" + hexArr[22] + " " + hexArr[23] + ":" + hexArr[24] + ":" + hexArr[25]);
                        jsonAlarm.put("endTime", "20" + hexArr[28] + "-" + hexArr[29] + "-" + hexArr[30] + " " + hexArr[31] + ":" + hexArr[32] + ":" + hexArr[33]);
                        jsonAlarm.put("reason", intArr[44] + intArr[45]);
                        jsonAlarm.put("data", 0);
                        // 使用postForEntity请求接口
                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Authorization", "Bearer " + token);
                        HttpEntity<JSONObject> httpEntity = new HttpEntity<JSONObject>(jsonAlarm, headers);
                        ResponseEntity<String> response = restTemplate.postForEntity(REAL_URL + ALARMDATA_URL, httpEntity, String.class);
                    }

                } else {
                    log.warn("字节数有误：{},长度:{}  tcp", topic, hexArr.length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //昆仑通泰的json解析
    public void parseData(JSONObject jsonObject) {
        try {

            if (jsonObject.getString("status") != null && jsonObject.getString("recover_time") != null) {// 报警数据

                //原来存redis的
                /*map_1.put("datetime", DataFormat.dateFormat(new Date()));
                //帧头
                map_1.put("A0_1", 0);
                map_1.put("A2_3", 0);
                map_1.put("A4_13", mqttConsumer.topicJ);
                map_1.put("A14_15", 0);
                map_1.put("A16_17", 0);
                map_1.put("A18_19", 0);

                //报警周期数据
                map_1.put("A20_27", jsonObject.getString("trigger_time"));
                map_1.put("A28_35", jsonObject.getString("recover_time"));
                map_1.put("A36_39", 0);
                map_1.put("A40_43", null);
                map_1.put("A44_45", 0);
                map_1.put("A46_57", new String(jsonObject.getString("message").getBytes("GBK"),"UTF-8"));

                //帧尾
                map_1.put("A58_59", null);
                map_1.put("A60_61", 0);

                redisTemplate.opsForValue().set("5_" + mqttConsumer.topicJ, map_1, 10, TimeUnit.SECONDS);
                System.out.println("设备号: " + mqttConsumer.topicJ + " " + DataFormat.dateFormat(new Date())+"  报警");*/
                AlarmData alarmData = new AlarmData();
                alarmData.setA0_1("");
                alarmData.setA2_3(0);
                alarmData.setA4_13(mqttConsumer.topicJ);
                alarmData.setA14_15(0);
                alarmData.setA16_17(0);
                alarmData.setA18_19(0);
                Timestamp start = Timestamp.valueOf(jsonObject.getString("trigger_time").replace('T', ' '));
                Timestamp end = Timestamp.valueOf(jsonObject.getString("recover_time").replace('T', ' '));
                alarmData.setA20_27(start);
                alarmData.setA28_35(end);
                alarmData.setA36_39((int) (end.getTime() - start.getTime()) / 1000);
                alarmData.setA40_43(0);
                alarmData.setA44_45(0);
                alarmData.setA46_57(jsonObject.getString("message"));
                alarmData.setA58_59(0);
                alarmData.setA60_61(0);
                alarmData.setBjid("0");
                alarmData.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                alarmData.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                int insert = alarmDataMapper.insert(alarmData);
                log.info("设备号: {}  {}" ,mqttConsumer.topicJ, jsonObject.getString("message"));

                AlarmEarly alarmEarly = new AlarmEarly();
                alarmEarly.setEquipmentId(mqttConsumer.topicJ);
                alarmEarly.setBjType(0);
                alarmEarly.setBjInfo(jsonObject.getString("message"));
                alarmEarlyMapper.insert(alarmEarly);
            } else {//实时数据
                JSONObject json = jsonObject.getJSONObject("RealTimeData");
                map.put("datetime", DataFormat.dateFormat(new Date()));
                //帧头
                map.put("A0_1", json.getIntValue("J1"));
                map.put("A2_3", json.getIntValue("J2"));
                map.put("A4_13", mqttConsumer.topicJ);
                map.put("A14_15", json.getIntValue("J4"));
                map.put("A16_17", json.getIntValue("J5"));
                map.put("A18_19", json.getIntValue("J6"));

                //状态数据
                map.put("A20_21", json.getDoubleValue("J7"));
                map.put("A22_23", json.getDoubleValue("J8"));
                map.put("A24_25", json.getDoubleValue("J9"));
                map.put("A26_27", json.getDoubleValue("J10"));
                map.put("A28_29", json.getDoubleValue("J11"));
                map.put("A30_31", json.getDoubleValue("J12"));
                map.put("A32_33", json.getDoubleValue("J13"));
                map.put("A34_35", json.getDoubleValue("J14"));
                map.put("A36_37", json.getDoubleValue("J15"));
                map.put("A38_39", json.getDoubleValue("J16"));
                map.put("A40_41", json.getDoubleValue("J17"));
                map.put("A42_43", json.getDoubleValue("J18"));
                map.put("A44_45", json.getDoubleValue("J19"));
                map.put("A46_47", json.getDoubleValue("J20"));
                map.put("A48_49", json.getDoubleValue("J21"));
                map.put("A50_51", json.getDoubleValue("J22"));
                map.put("A52_53", json.getDoubleValue("J23"));
                map.put("A54_55", json.getDoubleValue("J24"));
                map.put("A56_57", json.getDoubleValue("J25"));
                map.put("A58_59", json.getDoubleValue("J26"));
                map.put("A60_61", json.getDoubleValue("J27"));
                map.put("A62_63", json.getDoubleValue("J28"));
                map.put("A64_65", json.getDoubleValue("J29"));
                map.put("A66_67", json.getDoubleValue("J30"));
                map.put("A68_69", json.getDoubleValue("J31"));
                map.put("A70_71", json.getDoubleValue("J32"));
                map.put("A72_73", json.getDoubleValue("J33"));
                map.put("A74_75", json.getDoubleValue("J34"));
                map.put("A76_77", json.getDoubleValue("J35"));
                map.put("A78_79", json.getDoubleValue("J36"));
                map.put("A80_81", json.getDoubleValue("J37"));
                map.put("A82_83", json.getDoubleValue("J38"));
                map.put("A84_85", json.getDoubleValue("J39"));
                map.put("A86_87", json.getDoubleValue("J40"));
                map.put("A88_89", json.getDoubleValue("J41"));
                map.put("A90_91", json.getDoubleValue("J42"));
                map.put("A92_93", json.getDoubleValue("J43"));
                map.put("A94_95", json.getDoubleValue("J44"));
                map.put("A96_97", json.getDoubleValue("J45"));
                map.put("A98_99", json.getDoubleValue("J46"));
                map.put("A100_101", json.getDoubleValue("J47"));
                map.put("A102_103", json.getDoubleValue("J48"));
                map.put("A104_105", json.getDoubleValue("J49"));
                map.put("A106_107", json.getDoubleValue("J50"));
                map.put("A108_109", json.getDoubleValue("J51"));
                map.put("A110_111", json.getDoubleValue("J52"));
                map.put("A112_113", json.getDoubleValue("J53"));
                map.put("A114_115", json.getDoubleValue("J54"));
                map.put("A116_117", json.getDoubleValue("J55"));
                map.put("A118_119", json.getDoubleValue("J56"));
                map.put("A120_121", json.getDoubleValue("J57"));
                map.put("A122_123", json.getDoubleValue("J58"));
                map.put("A124_125", json.getDoubleValue("J59"));
                map.put("A126_127", json.getDoubleValue("J60"));
                map.put("A128_129", json.getDoubleValue("J61"));
                map.put("A130_131", json.getDoubleValue("J62"));
                map.put("A132_133", json.getDoubleValue("J63"));
                map.put("A134_135", json.getDoubleValue("J64"));
                map.put("A136_137", json.getDoubleValue("J65"));
                map.put("A138_139", json.getDoubleValue("J66"));
                map.put("A140_141", json.getDoubleValue("J67"));
                map.put("A142_143", json.getDoubleValue("J68"));
                map.put("A144_145", json.getDoubleValue("J69"));
                map.put("A146_147", json.getDoubleValue("J70"));

                //开关量
                map.put("A1480", json.getIntValue("J71"));
                map.put("A1481", json.getIntValue("J72"));
                map.put("A1482", json.getIntValue("J73"));
                map.put("A1483", json.getIntValue("J74"));
                map.put("A1484", json.getIntValue("J75"));
                map.put("A1485", json.getIntValue("J76"));
                map.put("A1486", json.getIntValue("J77"));
                map.put("A1487", json.getIntValue("J78"));
                map.put("A1490", json.getIntValue("J79"));
                map.put("A1491", json.getIntValue("J80"));
                map.put("A1492", json.getIntValue("J81"));
                map.put("A1493", json.getIntValue("J82"));
                map.put("A1494", json.getIntValue("J83"));
                map.put("A1495", json.getIntValue("J84"));
                map.put("A1496", json.getIntValue("J85"));
                map.put("A1497", json.getIntValue("J86"));
                map.put("A1500", json.getIntValue("J87"));
                map.put("A1501", json.getIntValue("J88"));
                map.put("A1502", json.getIntValue("J89"));
                map.put("A1503", json.getIntValue("J90"));
                map.put("A1504", json.getIntValue("J91"));
                map.put("A1505", json.getIntValue("J92"));
                map.put("A1506", json.getIntValue("J93"));
                map.put("A1507", json.getIntValue("J94"));
                map.put("A1510", json.getIntValue("J95"));
                map.put("A1511", json.getIntValue("J96"));
                map.put("A1512", json.getIntValue("J97"));
                map.put("A1513", json.getIntValue("J98"));
                map.put("A1514", json.getIntValue("J99"));
                map.put("A1515", json.getIntValue("J100"));
                map.put("A1516", json.getIntValue("J101"));
                map.put("A1517", json.getIntValue("J102"));
                map.put("A1520", json.getIntValue("J103"));
                map.put("A1521", json.getIntValue("J104"));
                map.put("A1522", json.getIntValue("J105"));
                map.put("A1523", json.getIntValue("J106"));
                map.put("A1524", json.getIntValue("J107"));
                map.put("A1525", json.getIntValue("J108"));
                map.put("A1526", json.getIntValue("J109"));
                map.put("A1527", json.getIntValue("J110"));
                map.put("A1530", json.getIntValue("J111"));
                map.put("A1531", json.getIntValue("J112"));
                map.put("A1532", json.getIntValue("J113"));
                map.put("A1533", json.getIntValue("J114"));
                map.put("A1534", json.getIntValue("J115"));
                map.put("A1535", json.getIntValue("J116"));
                map.put("A1536", json.getIntValue("J117"));
                map.put("A1537", json.getIntValue("J118"));
                map.put("A1540", json.getIntValue("J119"));
                map.put("A1541", json.getIntValue("J120"));
                map.put("A1542", json.getIntValue("J121"));
                map.put("A1543", json.getIntValue("J122"));
                map.put("A1544", json.getIntValue("J123"));
                map.put("A1545", json.getIntValue("J124"));
                map.put("A1546", json.getIntValue("J125"));
                map.put("A1547", json.getIntValue("J126"));
                map.put("A1550", json.getIntValue("J127"));
                map.put("A1551", json.getIntValue("J128"));
                map.put("A1552", json.getIntValue("J129"));
                map.put("A1553", json.getIntValue("J130"));
                map.put("A1554", json.getIntValue("J131"));
                map.put("A1555", json.getIntValue("J132"));
                map.put("A1556", json.getIntValue("J133"));
                map.put("A1557", json.getIntValue("J134"));

                // 累计工作时间,累计工作次数
                map.put("A156_159", json.getIntValue("J135"));
                map.put("A160_163", json.getIntValue("J136"));
                // GPS经纬度
                map.put("A164_167", json.getIntValue("J137"));
                map.put("A168_171", json.getIntValue("J138"));

                map.put("A172_175", json.getIntValue("J139"));
                map.put("A176_179", json.getIntValue("J140"));
                map.put("A180_183", json.getIntValue("J141"));
                map.put("A184_187", json.getIntValue("J142"));
                map.put("A188_191", json.getIntValue("J143"));
                map.put("A192_195", json.getIntValue("J144"));
                map.put("A196_197", json.getIntValue("J145"));

                //帧尾  原CRC16的值,不设置为0.0会导致点击App定位闪退
                map.put("A198_199", ((double) 0.0));

                redisTemplate.opsForValue().set(mqttConsumer.topicJ, map);
                log.info("设备号: {}" ,mqttConsumer.topicJ );

                //数据对接到南京飞博
                if (mqttConsumer.topicJ.equals("j210205001")) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("Clientid", mqttConsumer.topicJ);
                    //他大车==我们小车   他小车==我们天车
                    jsonObj.put("Aaheight", json.getDoubleValue("J8"));
                    jsonObj.put("Aaspeed", json.getDoubleValue("J10"));
                    jsonObj.put("Aaweight", json.getDoubleValue("J7"));
                    jsonObj.put("Atravel", json.getDoubleValue("J14"));
                    jsonObj.put("Baheight", "0");
                    jsonObj.put("Baspeed", "0");
                    jsonObj.put("Baweight", "0");
                    jsonObj.put("Btravel", "0");
                    jsonObj.put("Ss104_105", json.getDoubleValue("J13"));
                    jsonObj.put("X142", json.getDoubleValue("J16"));
                    jsonObj.put("Bj1360", json.getIntValue("J110"));
                    jsonObj.put("Bj1361", json.getIntValue("J98") | json.getIntValue("J99"));
                    jsonObj.put("Bj1362", json.getIntValue("J97") | json.getIntValue("J103"));
                    jsonObj.put("Bj1363", json.getIntValue("J107"));
                    jsonObj.put("Bj1364", json.getIntValue("J108"));
                    jsonObj.put("Bj1370", "0");
                    jsonObj.put("Bj1371", "0");
                    jsonObj.put("Bj1372", "0");
                    jsonObj.put("Bj1373", "0");
                    jsonObj.put("Bj1374", "0");
                    jsonObj.put("Bj1380", json.getIntValue("J117"));
                    jsonObj.put("Bj1381", json.getIntValue("J118"));
                    jsonObj.put("Bj1382", json.getIntValue("J126"));
                    jsonObj.put("Bj1383", json.getIntValue("J127"));
                    jsonObj.put("Bj1384", "0");
                    jsonObj.put("Bj1385", "0");
                    jsonObj.put("Bj1386", json.getIntValue("J128"));
                    jsonObj.put("Bj1387", json.getIntValue("J123"));
                    jsonObj.put("Kg1322",json.getIntValue("J115")|json.getIntValue("J116"));
                    jsonObj.put("Kg1323",json.getIntValue("J98") | json.getIntValue("J99"));
                    jsonObj.put("Kg1312","0");
                    jsonObj.put("Kg1313","0");
                    jsonObj.put("Kg1330",json.getIntValue("J126"));
                    jsonObj.put("Kg1331",json.getIntValue("J127"));
                    jsonObj.put("Kg1332",json.getIntValue("J133"));
                    jsonObj.put("Kg1333",json.getIntValue("J124")|json.getIntValue("J125"));
                    jsonObj.put("Kg1334",json.getIntValue("J123"));
                    jsonObj.put("AlarmX",json.getIntValue("J129"));
                    jsonObj.put("AlarmY",json.getIntValue("J130"));
                    jsonObj.put("AlarmV01",json.getIntValue("J131"));
                    jsonObj.put("AlarmV02",json.getIntValue("J132"));
                    jsonObj.put("WorkTime",json.getDoubleValue("J135"));
                    jsonObj.put("WorkCycle",json.getDoubleValue("J136"));
                    jsonObj.put("LevelX",json.getDoubleValue("J17"));
                    jsonObj.put("LevelY",json.getDoubleValue("J18"));
                    jsonObj.put("Vertical01",json.getDoubleValue("J19"));
                    jsonObj.put("Vertical02",json.getDoubleValue("J20"));
                    jsonObj.put("times",DataFormat.dateFormat(new Date()));
                    jsonObj.put("type","3");

                    String urlData = URLEncoder.encode(jsonObj.toJSONString(), "utf-8").replace("+", "");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    RequestInfo requestInfo= new RequestInfo();
                    requestInfo.setAppid("5HGSVWBXCT");
                    requestInfo.setFormat("json");
                    requestInfo.setMethod("upload");
                    requestInfo.setNonce(UUID.randomUUID().toString().replaceAll("-", ""));
                    requestInfo.setTimestamp(sdf.format(new Date()));
                    requestInfo.setVersion("1.0");
                    requestInfo.setData(urlData);
                    requestInfo.setAppSecret("2f5a3d162ab7458c92aa97111d132260");

                    //获取签名
                    String sign = SignUtil.buildSign(requestInfo);

                    // 封装参数，千万不要替换为Map与HashMap，否则参数无法传递
                    MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
                    paramMap.add("appid", "5HGSVWBXCT");
                    paramMap.add("format", "json");
                    paramMap.add("method", "upload");
                    paramMap.add("nonce", requestInfo.getNonce());
                    paramMap.add("timestamp", sdf.format(new Date()));
                    paramMap.add("version", "1.0");
                    paramMap.add("data", urlData);
                    paramMap.add("sign", sign);

                    restTemplate.postForObject(serverUrl, paramMap, String.class);
                    log.info("设备号_: {}  " ,mqttConsumer.topicJ );
                }
            }
        } catch (Exception e) {
            log.warn("昆仑数据有误：{}", mqttConsumer.topicJ);
            e.printStackTrace();
        }
    }
}
