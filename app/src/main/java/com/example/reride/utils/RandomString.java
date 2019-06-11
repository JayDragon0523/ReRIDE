package com.example.reride.utils;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RandomString {

	// 用当前时间+N位随机数
	public final static String generateId() {
		// 获得当前时间
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss" );
		// 转换为字符串
		String formatDate = sdf.format(new Date());
		// 随机生成文件编号
		int random = new Random().nextInt(10000);
		return MD5( new StringBuffer().append(formatDate).append(random).toString() ).substring(8, 24);  // 16位MD5就是32位MD5的第8~24位之间
	}
	
	// 返回32位字符
	private final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
}
