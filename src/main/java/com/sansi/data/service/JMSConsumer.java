package com.sansi.data.service;

import com.sansi.data.config.JMSConfig;
import com.sansi.data.utils.Crc16Util;
import com.sansi.data.utils.DataFormat;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Intro
 * @Author LeePong
 * @Date 2020/4/24 16:20
 */
@Component
public class JMSConsumer {
    @Resource
    private RedisTemplate redisTemplate;

    //存放状态数据的
    Map map = new LinkedHashMap();
    //存放报警数据
    Map map_1 = new LinkedHashMap();

    private final static Logger logger = LoggerFactory.getLogger(JMSConsumer.class);

    //消息队列监听器
    @JmsListener(destination = JMSConfig.QUEUE, containerFactory = "jmsListenerContainerQueue")
    public void onQueueMessage(String msg) {
        logger.info("接收到queue消息：{}", msg);
    }

    //发布订阅监听器
    @JmsListener(destination = JMSConfig.TOPIC, containerFactory = "jmsListenerContainerTopic")
    public void onTopicMessage(byte[] payload) {
        try {
            String[] hexArr = DataFormat.byteArrToHexArr(payload);
            int[] intArr = DataFormat.HexArrToInt(hexArr);

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
                //帧头
                map.put("A0_1", "" + (char) intArr[0] + (char) intArr[1]);
                map.put("A2_3", intArr[2] + intArr[3]);
                map.put("A4_13", "" + (char) intArr[4] + (char) intArr[5] + (char) intArr[6] + (char) intArr[7] + (char) intArr[8] + (char) intArr[9] + (char) intArr[10] + (char) intArr[11] + (char) intArr[12] + (char) intArr[13]);
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
                String[] A148 = DataFormat.DecToBin(intArr[148]);
                map.put("A1480", A148[7]);
                map.put("A1481", A148[6]);
                map.put("A1482", A148[5]);
                map.put("A1483", A148[4]);
                map.put("A1484", A148[3]);
                map.put("A1485", A148[2]);
                map.put("A1486", A148[1]);
                map.put("A1487", A148[0]);

                String[] A149 = DataFormat.DecToBin(intArr[149]);
                map.put("A1490", A149[7]);
                map.put("A1491", A149[6]);
                map.put("A1492", A149[5]);
                map.put("A1493", A149[4]);
                map.put("A1494", A149[3]);
                map.put("A1495", A149[2]);
                map.put("A1496", A149[1]);
                map.put("A1497", A149[0]);

                String[] A150 = DataFormat.DecToBin(intArr[150]);
                map.put("A1500", A150[7]);
                map.put("A1501", A150[6]);
                map.put("A1502", A150[5]);
                map.put("A1503", A150[4]);
                map.put("A1504", A150[3]);
                map.put("A1505", A150[2]);
                map.put("A1506", A150[1]);
                map.put("A1507", A150[0]);

                String[] A151 = DataFormat.DecToBin(intArr[151]);
                map.put("A1510", A151[7]);
                map.put("A1511", A151[6]);
                map.put("A1512", A151[5]);
                map.put("A1513", A151[4]);
                map.put("A1514", A151[3]);
                map.put("A1515", A151[2]);
                map.put("A1516", A151[1]);
                map.put("A1517", A151[0]);

                String[] A152 = DataFormat.DecToBin(intArr[152]);
                map.put("A1520", A152[7]);
                map.put("A1521", A152[6]);
                map.put("A1522", A152[5]);
                map.put("A1523", A152[4]);
                map.put("A1524", A152[3]);
                map.put("A1525", A152[2]);
                map.put("A1526", A152[1]);
                map.put("A1527", A152[0]);

                String[] A153 = DataFormat.DecToBin(intArr[153]);
                map.put("A1530", A153[7]);
                map.put("A1531", A153[6]);
                map.put("A1532", A153[5]);
                map.put("A1533", A153[4]);
                map.put("A1534", A153[3]);
                map.put("A1535", A153[2]);
                map.put("A1536", A153[1]);
                map.put("A1537", A153[0]);

                String[] A154 = DataFormat.DecToBin(intArr[154]);
                map.put("A1540", A154[7]);
                map.put("A1541", A154[6]);
                map.put("A1542", A154[5]);
                map.put("A1543", A154[4]);
                map.put("A1544", A154[3]);
                map.put("A1545", A154[2]);
                map.put("A1546", A154[1]);
                map.put("A1547", A154[0]);

                String[] A155 = DataFormat.DecToBin(intArr[155]);
                map.put("A1550", A155[7]);
                map.put("A1551", A155[6]);
                map.put("A1552", A155[5]);
                map.put("A1553", A155[4]);
                map.put("A1554", A155[3]);
                map.put("A1555", A155[2]);
                map.put("A1556", A155[1]);
                map.put("A1557", A155[0]);

                //其它数据
                map.put("A156_159", Integer.parseInt("" + hexArr[156] + hexArr[157] + hexArr[158] + hexArr[159], 16));
                map.put("A160_163", Integer.parseInt("" + hexArr[160] + hexArr[161] + hexArr[162] + hexArr[163], 16));
                map.put("A164_167", (double) Integer.parseInt("" + hexArr[164] + hexArr[165] + hexArr[166] + hexArr[167], 16) / 1000000);
                map.put("A168_171", (double) Integer.parseInt("" + hexArr[168] + hexArr[169] + hexArr[170] + hexArr[171], 16) / 1000000);
                map.put("A172_175", (double) (Integer.parseInt("" + hexArr[172] + hexArr[173] + hexArr[174] + hexArr[175], 16)) / 100);
                map.put("A176_179", (double) (Integer.parseInt("" + hexArr[176] + hexArr[177] + hexArr[178] + hexArr[179], 16)) / 100);
                map.put("A180_183", (double) (Integer.parseInt("" + hexArr[180] + hexArr[181] + hexArr[182] + hexArr[183], 16)) / 100);
                map.put("A184_187", (double) (Integer.parseInt("" + hexArr[184] + hexArr[185] + hexArr[186] + hexArr[187], 16)) / 100);
                map.put("A188_191", (double) (Integer.parseInt("" + hexArr[188] + hexArr[189] + hexArr[190] + hexArr[191], 16)) / 100);
                map.put("A192_195", (double) (Integer.parseInt("" + hexArr[192] + hexArr[193] + hexArr[194] + hexArr[195], 16)) / 100);
                map.put("A196_197", (double) (Integer.parseInt("" + hexArr[196] + hexArr[197], 16)) / 100);

                //帧尾
                map.put("A198_199", ("" + hexArr[198] + hexArr[199]).toUpperCase());
                map.put("datetime", DataFormat.DateFormat(new Date()));

                redisTemplate.opsForValue().set(map.get("A4_13"), map);
                System.out.println("设备号: " + map.get("A4_13") + " " + DataFormat.DateFormat(new Date()) + "  " + hexArr.length);
            } else if (hexArr.length == 62) {
                //帧头
                map_1.put("A0_1", "" + (char) intArr[0] + (char) intArr[1]);
                map_1.put("A2_3", intArr[2] + intArr[3]);
                map_1.put("A4_13", "" + (char) intArr[4] + (char) intArr[5] + (char) intArr[6] + (char) intArr[7] + (char) intArr[8] + (char) intArr[9] + (char) intArr[10] + (char) intArr[11] + (char) intArr[12] + (char) intArr[13]);
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
                map_1.put("datetime", DataFormat.DateFormat(new Date()));

                if (("2000-00-00 00:00:00").equals(map_1.get("A28_35"))) {
                    redisTemplate.opsForValue().set("5_" + map_1.get("A4_13"), map_1, 2, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set("5_" + map_1.get("A4_13"), map_1, 10, TimeUnit.SECONDS);
                }
                System.out.println("设备号: " + map_1.get("A4_13") + " " + DataFormat.DateFormat(new Date()) + "  " + hexArr.length);
            } else {
//                logger.info("数据格式有误：{}", DataFormat.asHexFormatted(payload));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
