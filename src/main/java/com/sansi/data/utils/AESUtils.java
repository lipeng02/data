package com.sansi.data.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.shiro.codec.Base64;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

 /**
 *  AES 是一种可逆加密算法，对用户的敏感信息加密处理 对原始数据进行AES加密后，再进行Base64编码转化；
 * 一般是配合RSA进行动态加解密
 * @version 
 * @since JDK 1.7
 */
public class AESUtils {

    /*
     * 加密用的Key 可以用26个字母和数字组成 此处使用AES-128-CBC加密模式，key需要为16位。
     * key是aes的一个加密源，也就是通过该key加密后，必须通过该key就进行解密；对数据不是特别敏感的，直接用静态的key即可；
     * 有些场景下数据异常敏感，那么这个key就要动态生成，结合RSA进行加密，，就能做到每次请求，key都不一样，
     * 安全程度大大加强；
     * 当然key可以为32，但是要下载两个jdk的jar包，好像是美国那边没有发出来这个jar，具体的key百度下
     */
	//private static String sectStr ="ca8db5d78a174ddcbe9e5de08d71e9d5"; //SysUtil.findPropertiesKey("smzSect");
    //private static String sKey = sectStr;
    
    //private static String ivParameter = sectStr.substring(11, 27);//偏移量必须是16位字符串
    private static AESUtils instance = null;

    private AESUtils() {

    }
    /**
     * 
     * @Title: getInstance
     * @Description:初始化
     * @return AESUtils
     */
    public static AESUtils getInstance() {
        if (instance == null)
            instance = new AESUtils();
        return instance;
    }
    
    /**
     * 
     * @Title: Encrypt
     * @Description: 对数据进行加密
     * @param encData
     * @param secretKey
     * @param vector
     * @return
     * @throws Exception String
     */
    public static String encryptjm(String encData ,String secretKey) throws Exception {

        if(secretKey == null) {
            return null;
        }
        String ivParameter = secretKey.substring(11, 27);
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        byte[] raw = secretKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        try{
        	cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        }catch(Exception ex){
        	return null;
        }
        byte[] encrypted = cipher.doFinal(encData.getBytes("utf-8"));
        return new BASE64Encoder().encode(encrypted);// 此处使用BASE64做转码。
    }
    
    public static String Encrypt(String sSrc, String sKey) throws Exception {  
        if (sKey == null) {  
            System.out.print("Key为空null");  
            return null;  
        }  
        byte[] raw = sKey.getBytes("utf-8");  
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");  
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");//"算法/模式/补码方式"  
        String cKey=sKey.substring(0, 16);
        IvParameterSpec iv = new IvParameterSpec(cKey.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度  
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec,iv);  
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());  
        return new Base64().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。  
    }  
    
    /*
     * 加密
     * 1.构造密钥生成器
     * 2.根据ecnodeRules规则初始化密钥生成器
     * 3.产生密钥
     * 4.创建和初始化密码器
     * 5.内容加密
     * 6.返回字符串
     */
      public static String AESEncode(String encodeRules,String content){
          try {
              //1.构造密钥生成器，指定为AES算法,不区分大小写
              KeyGenerator keygen=KeyGenerator.getInstance("AES");
              //2.根据ecnodeRules规则初始化密钥生成器
              //生成一个128位的随机源,根据传入的字节数组
              keygen.init(128, new SecureRandom(encodeRules.getBytes()));
                //3.产生原始对称密钥
              SecretKey original_key=keygen.generateKey();
                //4.获得原始对称密钥的字节数组
              byte [] raw=original_key.getEncoded();
              //5.根据字节数组生成AES密钥
              SecretKey key=new SecretKeySpec(raw, "AES");
                //6.根据指定算法AES自成密码器
              Cipher cipher=Cipher.getInstance("AES");
                //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
              cipher.init(Cipher.ENCRYPT_MODE, key);
              //8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
              byte [] byte_encode=content.getBytes("utf-8");
              //9.根据密码器的初始化方式--加密：将数据加密
              byte [] byte_AES=cipher.doFinal(byte_encode);
            //10.将加密后的数据转换为字符串
              //这里用Base64Encoder中会找不到包
              //解决办法：
              //在项目的Build path中先移除JRE System Library，再添加库JRE System Library，重新编译后就一切正常了。
              String AES_encode=new String(new BASE64Encoder().encode(byte_AES));
            //11.将字符串返回
              return AES_encode;
          } catch (NoSuchAlgorithmException e) {
              e.printStackTrace();
          } catch (NoSuchPaddingException e) {
              e.printStackTrace();
          } catch (InvalidKeyException e) {
              e.printStackTrace();
          } catch (IllegalBlockSizeException e) {
              e.printStackTrace();
          } catch (BadPaddingException e) {
              e.printStackTrace();
          } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
          }
          
          //如果有错就返加nulll
          return null;         
      }


    /*// 加密
    public String encrypt(String sSrc) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
        return new BASE64Encoder().encode(encrypted);// 此处使用BASE64做转码。  
    }

    // 解密
    public static String decrypt(String sSrc){
        try {
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "utf-8");
            return originalString;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }*/

    /**解密
     * @param sSrc 解密字符串
     * @param key key
     * @param ivs ivs
     * @return
     * @throws Exception
     */
    public static String decryptjm(String sSrc,String key){
        try {
        	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        	String ivParameter = key.substring(11, 27);
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "utf-8");
            return originalString;
        } catch (Exception ex) {
        	ex.printStackTrace();
            return null;
        }
    }

    public static String encodeBytes(byte[] bytes) {
        StringBuffer strBuf = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ((int) 'a')));
            strBuf.append((char) (((bytes[i]) & 0xF) + ((int) 'a')));
        }

        return strBuf.toString();
    }

   
    /**
     * getRandomString:获取随机字符串
     * @author zhaoshouyun
     * @param length
     * @return
     * @since JDK 1.7
     */
    @SuppressWarnings("unused")
    private static String getRandomString(int length) { //length表示生成字符串的长度  
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";     
        Random random = new Random();     
        StringBuffer sb = new StringBuffer();     
        for (int i = 0; i < length; i++) {     
            int number = random.nextInt(base.length());     
            sb.append(base.charAt(number));     
        }     
        return sb.toString();     
     } 
    
    /**
     * 产生随机密钥(这里产生密钥必须是16位)
     */
    public static String generateKey() {
        String key = UUID.randomUUID().toString();
        key = key.replace("-", "").substring(0, 16);// 替换掉-号
        return key;
    }
    

	public static void main(String[] args) throws Exception {
		String enkey = "402882f67394d8cc017394eca7750014";
		//String enkey = SysUtil.md5(msg);
		String cSrc = "{\"yxzt\":\"0\",\"sgzt\":\"2\",\"jd\":\"108.31947\",\"wd\":\"30.71676\",\"qj\":\"1.3\",\"zdzz\":\"8000\",\"bl\":\"2\",\"zdfd\":\"55\",\"dbc\":\"55\",\"phbc\":\"12\",\"lj\":\"800\",\"zz\":\"1230\",\"fs\":\"3.5\",\"fd\":\"15.6\",\"gd\":\"26.8\",\"jjd\":\"156.9\"}";
		String enStr = AESUtils.encryptjm(cSrc, enkey);
		System.err.println("加密后的字串是：" + enStr);
		    
		String deStr = AESUtils.decryptjm(enStr, enkey);
		System.err.println("解密后的字串是：" + deStr);
		
    }

}
