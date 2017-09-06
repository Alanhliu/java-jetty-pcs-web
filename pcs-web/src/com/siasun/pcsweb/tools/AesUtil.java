package com.siasun.pcsweb.tools;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AesUtil {
	
	public final String VIPARA = "0102030405060708";

	// 加密
	public String Encrypt(String sSrc, String sKey) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(sKey.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec iv = new IvParameterSpec(VIPARA.getBytes());
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(sSrc.getBytes("UTF-8"));

		return Base64.encodeBase64String(encrypted);
	}

	// 解密
	public String Decrypt(String sSrc, String sKey) throws Exception {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(sKey.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec iv = new IvParameterSpec(VIPARA.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] encrypted1 = Base64.decodeBase64(sSrc);
			try {
				byte[] original = cipher.doFinal(encrypted1);
				String originalString = new String(original, "UTF-8");
				return originalString;
			} catch (Exception e) {
				System.out.println(e.toString());
				return null;
			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return null;
		}
	}
}