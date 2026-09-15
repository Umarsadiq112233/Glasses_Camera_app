package com.oudmon.ble.base.communication.req;
import android.util.Log;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */
//TODO 需要关注OdmHandle中在连接后也下发了时间
public class SetTimeReq extends BaseReqCmd {

    private byte mLanguage = 0x00;

    private byte[] mData = new byte[7];

    private Map<String, Integer> mLocaleMap = new HashMap<>();

    /**
     * 建议调用带Offset的方法，如果缓存中没有时间偏差，offset为0
     */
    @Deprecated
    public SetTimeReq() {
        super(Constants.CMD_SET_DEVICE_TIME);
        initMap();
        setLanguage();
        Calendar calendar = Calendar.getInstance();
        mData[0] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.YEAR) % 2000);
        mData[1] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.MONTH) + 1);
        mData[2] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.DAY_OF_MONTH));
        mData[3] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.HOUR_OF_DAY));
        mData[4] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.MINUTE));
        mData[5] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.SECOND));
    }

    /**
     * 构造有时间偏差的时间戳，时间偏差为秒
     * @param offset 秒的偏差
     */
    public SetTimeReq(int offset) {
        super(Constants.CMD_SET_DEVICE_TIME);
        initMap();
        setLanguage();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, offset);
        mData[0] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.YEAR) % 2000);
        mData[1] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.MONTH) + 1);
        mData[2] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.DAY_OF_MONTH));
        mData[3] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.HOUR_OF_DAY));
        mData[4] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.MINUTE));
        mData[5] = BLEDataFormatUtils.decimalToBCD(calendar.get(Calendar.SECOND));
    }

    private void initMap() {
        mLocaleMap.put("zh_CN", 0);  //简体中文
        mLocaleMap.put("en", 1);     //英语
        mLocaleMap.put("zh_HK", 2);  //HK
        mLocaleMap.put("zh_TW", 2);  //TW
        mLocaleMap.put("el", 0x03);  //希腊
        mLocaleMap.put("fr", 0x04);  //法语
        mLocaleMap.put("de", 0x05);  //德语
        mLocaleMap.put("it", 0x06);  //意大利
        mLocaleMap.put("es", 0x07);  //西班牙
        mLocaleMap.put("nl", 0x08);  //荷兰
        mLocaleMap.put("pt", 0x09);  //葡萄牙
        mLocaleMap.put("ru", 0x0a);  //俄语
        mLocaleMap.put("tr", 0x0b);  //土耳其
        mLocaleMap.put("ja", 0x0c);  //日语
        mLocaleMap.put("ko", 0x0d);  //韩语
        mLocaleMap.put("pl", 0x0e);  //波兰
        mLocaleMap.put("ro", 0x0f);  //罗马尼亚
        mLocaleMap.put("ar", 0x10);  //阿拉伯
        mLocaleMap.put("th", 0x11);  //泰语
        mLocaleMap.put("vi", 0x12);  //越南语
        mLocaleMap.put("in", 0x13);  //印尼语
        mLocaleMap.put("hi", 0x14);  //印地文
        mLocaleMap.put("cs", 0x15);  //捷克
        mLocaleMap.put("sk", 0x16);  //斯洛伐克
        mLocaleMap.put("hu", 0x17);  //匈牙利
        mLocaleMap.put("iw", 0x18);  //希伯来
        mLocaleMap.put("hr", 0x19);  //克罗地亚
        mLocaleMap.put("sl", 0x1a);  //斯洛文尼
        mLocaleMap.put("ur", 0x23);  //乌耳都
    }

    @Override
    protected byte[] getSubData() {
        mData[6] = mLanguage;
        return mData;
    }

    public void setLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (language.startsWith("zh")) {
            language = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry().toUpperCase();
        }
        Integer value = mLocaleMap.get(language);
        int result = value == null ? 1 : value; //默认发英文
         Log.i(TAG, "SetTimeReq -> mLanguage: " + language + ", value: " + value + ", result: " + result);
        this.mLanguage = (byte) result;
    }

}
