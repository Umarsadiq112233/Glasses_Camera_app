package com.oudmon.ble.base.communication.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtil {

	/**
	 * int to byte[] 支持 1或者 4 个字节 小断模式
	 * 
	 * @param i
	 * @param len
	 * @return
	 */
	public static byte[] intToByte(int i, int len) {
		byte[] abyte = null;
		if (len == 1) {
			abyte = new byte[len];
			abyte[0] = (byte) (0xff & i);
		} else {
			abyte = new byte[len];
			abyte[0] = (byte) (0xff & i);
			abyte[1] = (byte) ((0xff00 & i) >> 8);
			abyte[2] = (byte) ((0xff0000 & i) >> 16);
			abyte[3] = (byte) ((0xff000000 & i) >> 24);
		}
		return abyte;
	}

	// 将byte[] 数组转换成float[]数组
	public static float[] bytesToFloats(byte[] bytes,int len,boolean isBe) {
		if(bytes==null){
			return null;
		}
		float[] floats = new float[len/4];
		// 大端序
		if (isBe) {
			ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asFloatBuffer().get(floats);
		} else {
			ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floats);
		}

		return floats;
	}

	// 将byte[] 数组转换成short[]数组
	public static short[] bytesToShorts(byte[] bytes,int len,boolean isBe) {
		if(bytes==null){
			return null;
		}
		short[] shorts = new short[len/2];
		// 大端序
		if (isBe) {
			ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
		} else {
			ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
		}

		return shorts;
	}

	/**
	 * Byte转Bit
	 */
	public static String byteToBit(byte b) {
		return "" +(byte)((b >> 7) & 0x1) +
				(byte)((b >> 6) & 0x1) +
				(byte)((b >> 5) & 0x1) +
				(byte)((b >> 4) & 0x1) +
				(byte)((b >> 3) & 0x1) +
				(byte)((b >> 2) & 0x1) +
				(byte)((b >> 1) & 0x1) +
				(byte)((b >> 0) & 0x1);
	}

	/**
	 * byte to int 带符号位
	 * @param bytes
	 * @return
	 */
	public static int bytes2IntIncludeSignBit(byte[] bytes){
		int addr = 0;
		if (bytes.length == 1) {
			addr = bytes[0];
		} else if (bytes.length == 4) {
			addr = bytes[3] << 24;
			addr |= (bytes[2] << 24) >>> 8;
			addr |= (bytes[1] << 24) >>> 16;
			addr |= (bytes[0] << 24) >>> 24;
		} else if (bytes.length == 2) {
			addr = bytes[1] << 8 ;
			addr |= (bytes[0] << 24) >>> 24;
		} else if (bytes.length == 3) {
			addr = bytes[2] << 16;
			addr |= (bytes[1] << 24) >>> 16;
			addr |= (bytes[0] << 24) >>> 24;
		}
		return addr;
	}

	//int转byte
	public static byte int2byte(int integer){
		return (byte) (0xff & integer);
	}


	/**
	 * byte 成[byte ]
	 * @param b
	 * @return
	 */
	  public static byte[] getBooleanArray(byte b) {  
	        byte[] array = new byte[8];  
	        for (int i = 7; i >= 0; i--) {  
	            array[i] = (byte)(b & 1);  
	            b = (byte) (b >> 1);  
	        }  
	        return array;  
	    } 

	/**
	 * byte to int
	 * @param bytes
	 * @return
	 */
	public static int bytesToInt(byte[] bytes) {
		int addr = 0;
		if (bytes.length == 1) {
			addr = bytes[0] & 0xFF;
		} else if (bytes.length == 4) {
			addr = bytes[0] & 0xFF;
			addr |= ((bytes[1] << 8) & 0xFF00);
			addr |= ((bytes[2] << 16) & 0xFF0000);
			addr |= ((bytes[3] << 24) & 0xFF000000);
		} else if (bytes.length == 2) {
			addr = bytes[0] & 0xFF;
			addr |= ((bytes[1] << 8) & 0xFF00);
		} else if (bytes.length == 3) {
			addr = bytes[0] & 0xFF;
			addr |= ((bytes[1] << 8) & 0xFF00);
			addr |= ((bytes[2] << 16) & 0xFF0000);
		}
		return addr;
	}

	public static int byteToInt(byte bytes){
		return  bytes & 0xFF;

	}

	/**
	 * 合并两数组
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] concat(byte[] a, byte[] b) {
		if (a == null) {
			return b;
		} else if (b == null) {
			return a;
		} else {
			byte[] c = new byte[a.length + b.length];
			System.arraycopy(a, 0, c, 0, a.length);
			System.arraycopy(b, 0, c, a.length, b.length);
			return c;
		}
	}
	
	/**
	 * byte to int
	 * @param bytes
	 * @return
	 */
	public static int bytesToIntForVersion(byte[] bytes) {
		int addr = 0;
		if (bytes.length == 1) {
			addr = bytes[0] & 0xFF;
		} else if (bytes.length == 4) {
			addr = bytes[3] & 0xFF;
			addr |= ((bytes[2] << 8) & 0xFF00);
			addr |= ((bytes[1] << 16) & 0xFF0000);
			addr |= ((bytes[0] << 24) & 0xFF000000);
		} else if (bytes.length == 2) {
			addr = bytes[0] & 0xFF;
			addr |= ((bytes[1] << 8) & 0xFF00);
		} else if (bytes.length == 3) {
			addr = bytes[0] & 0xFF;
			addr |= ((bytes[1] << 8) & 0xFF00);
			addr |= ((bytes[2] << 16) & 0xFF0000);
		}
		return addr;
	}

	/**
	 * int to byte[] 支持 1或者 4 个字节 大端模式
	 * 
	 * @param i
	 * @param len
	 * @return
	 */
	public static byte[] intToByteBig(int i, int len) {
		byte[] abyte = null;
		if (len == 1) {
			abyte = new byte[len];
			abyte[0] = (byte) (0xff & i);
		} else {
			abyte = new byte[len];
			abyte[0] = (byte) ((i >>> 24) & 0xff);
			abyte[1] = (byte) ((i >>> 16) & 0xff);
			abyte[2] = (byte) ((i >>> 8) & 0xff);
			abyte[3] = (byte) (i & 0xff);
		}
		return abyte;
	}

	public static int bytesToIntBig(byte[] bytes) {
		int addr = 0;
		if (bytes.length == 1) {
			addr = bytes[0] & 0xFF;
		} else {
			addr = bytes[0] & 0xFF;
			addr = (addr << 8) | (bytes[1] & 0xff);
			addr = (addr << 8) | (bytes[2] & 0xff);
			addr = (addr << 8) | (bytes[3] & 0xff);
		}
		return addr;
	}
	
	public static  String bytesToString(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder(bytes.length);
		for (byte byteChar : bytes)
			stringBuilder.append(String.format("%02X", byteChar));
		return stringBuilder.toString();
	}

	public static  String bytesToStringFormat(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder(bytes.length);
		for (byte byteChar : bytes)
			stringBuilder.append(String.format("%01X", byteChar));
		return stringBuilder.toString();
	}

	/**
	 * 取低位
	 * 
	 * @param i
	 * @return
	 */
	public static int loword(int i) {
		return i & 0xFFFF;
	}

	/**
	 * 取高位
	 * 
	 * @param i
	 * @return
	 */
	public static int hiword(int i) {
		return i >>> 8;
	}
	/**
	 * 二进制string 转16进制
	 * @param bString
	 * @return
	 */
	public static String binaryString2hexString(String bString)
	{
		if (bString == null || bString.equals("") || bString.length() % 8 != 0)
			return null;
		StringBuffer tmp = new StringBuffer();
		int iTmp = 0;
		for (int i = 0; i < bString.length(); i += 4)
		{
			iTmp = 0;
			for (int j = 0; j < 4; j++)
			{
				iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
			}
			tmp.append(Integer.toHexString(iTmp));
		}
		return tmp.toString();
	}

	/**
	 * 字节数组转为16进制的字符串
	 * @param copyOfRange
	 * @return
	 */
	public static String byteArrayToString(byte[] copyOfRange) {
		StringBuilder sb = new StringBuilder();
		for (byte b : copyOfRange) {
			String i = Integer.toHexString(b & 0xff);
			i = i.length() == 1 ? "0" + i : i ;
			sb.append(i);
		}
		return sb.toString();
	}


	/**
	 * 字节数组转为16进制的字符串
	 * @param copyOfRange
	 * @return
	 */
	public static String byteArraySubToString(byte[] copyOfRange) {
		StringBuilder sb = new StringBuilder();
		for (byte b : copyOfRange) {
			String i = Integer.toHexString(b & 0xff);
			i = i.length() == 1 ? "0" + i : i ;
			sb.append(i);
			if(sb.toString().length()>=200){
				break;
			}
		}
		return sb.toString();
	}



	/**
	 * byte 转byte []数组
	 * @param b
	 * @return
	 */
	public static byte[] byteToBitArray(int b){
		byte [] array=new byte[8];
		array[0]=(byte)((b >> 7) & 0x1);
		array[1]=(byte)((b >> 6) & 0x1);
		array[2]=(byte)((b >> 5) & 0x1);
		array[3]=(byte)((b >> 4) & 0x1);
		array[4]=(byte)((b >> 3) & 0x1);
		array[5]=(byte)((b >> 2) & 0x1);
		array[6]=(byte)((b >> 1) & 0x1);
		array[7]=(byte)((b >> 0) & 0x1);
		return array;
	}


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToString1(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder stringBuilder = new StringBuilder(bytes.length);
        String format;
        format = "%02X ";
        for (byte byteChar : bytes)
            stringBuilder.append(String.format(format, byteChar));
        return stringBuilder.toString();
    }

	//字符串转字节数组
	public static byte[] hexToBytes(String hexStrings) {
		if (hexStrings == null || hexStrings.equals("")) {
			return null;
		}
		String hexString = hexStrings.toLowerCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] bytes = new byte[length];
		String hexDigits = "0123456789abcdef";
		for (int i = 0; i < length; i++) {
			int pos = i * 2; // 两个字符对应一个byte
			int h = hexDigits.indexOf(hexChars[pos]) << 4;
			int l = hexDigits.indexOf(hexChars[pos + 1]);
			if (h == -1 || l == -1) { // 非16进制字符
				return null;
			}
			bytes[i] = (byte) (h | l);
		}
		return bytes;
	}

	public static String byteAsciiToChar(int... ascii){
		String str = "";
    	for (int i : ascii) {
			char ch = (char)i;
			str += ch;
		}
		return str;
	}

	public static String unicodeByteToStr(byte[] bBuf){
//        return new String(bBuf, StandardCharsets.UTF_16LE); // 这种不会处理字符串结束符 \0
		StringBuffer result = new StringBuffer();
		Character ch = 0;
		for(int i = 0; i < bBuf.length; i += 2){
			if(bBuf[i+1] == 0){
				ch = (char)bBuf[i];
			}
			else {
				ch = (char)((bBuf[i] << 8)&0xFF00 | bBuf[i+1]);
			}

			if(ch == 0){ // 字符串结束符 \0
				break;
			}
			result.append(ch);
		}
		return result.toString();
	}
}
