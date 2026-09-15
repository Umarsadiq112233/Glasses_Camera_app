package com.oudmon.ble.base.communication.rsp;
import android.util.Log;

import com.oudmon.ble.base.communication.entity.BpDataEntity;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.util.Arrays;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.13 定时血压数据获取
 */

public class BpDataRsp extends BaseRspCmd {

    private int valueIndex = 0;
    private BpDataEntity bpDataEntity;

    @Override
    public boolean acceptData(byte[] data) {
        Log.i(TAG, "acceptData.. data: " + DataTransferUtils.getHexString(data));
        int pocketIndex = data[0];
        if (pocketIndex == 0x00) {
            valueIndex = 0;
            bpDataEntity = new BpDataEntity(data[1] + 2000, data[2], data[3], data[4]);
            int timeDelay = data[4];
            for (int i = 0; i < 6; i++) {
                byte cur = data[i + 5];
                for (int j = 0; j < 8; j++) {//查找1的位置
                    byte mask = (byte) (cur >>> j);
                    if ((0x01 & mask) == 1) {//此处有数据
                        bpDataEntity.addBpIndex((i * 8 + j) * timeDelay);
                    }
                }
            }
        } else if (pocketIndex == 0x01) {
            if (bpDataEntity == null) {
                return true;
            }
            bpDataEntity.addRealValue(valueIndex * 13, Arrays.copyOfRange(data, 1, data.length));
            valueIndex += 1;
            Log.i(TAG, "acceptData: size=" + bpDataEntity.getBpValues().size() + " cur offset=" + valueIndex * 13);
            if (valueIndex * 13 >= bpDataEntity.getBpValues().size()) {//没有后续数据了
                Log.i(TAG, "acceptData: 成功");
                return false;
            }
        } else if ((pocketIndex & 0xFF) == 0xFF) {
            return false;//不需要数据了
        }
        return true;
    }

    public BpDataEntity getBpDataEntity() {
        return bpDataEntity;
    }
}
