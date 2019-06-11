package com.example.reride.utils;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RandomString {

	// �õ�ǰʱ��+Nλ�����
	public final static String generateId() {
		// ��õ�ǰʱ��
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss" );
		// ת��Ϊ�ַ���
		String formatDate = sdf.format(new Date());
		// ��������ļ����
		int random = new Random().nextInt(10000);
		return MD5( new StringBuffer().append(formatDate).append(random).toString() ).substring(8, 24);  // 16λMD5����32λMD5�ĵ�8~24λ֮��
	}
	
	// ����32λ�ַ�
	private final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] btInput = s.getBytes();
            // ���MD5ժҪ�㷨�� MessageDigest ����
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // ʹ��ָ�����ֽڸ���ժҪ
            mdInst.update(btInput);
            // �������
            byte[] md = mdInst.digest();
            // ������ת����ʮ�����Ƶ��ַ�����ʽ
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
