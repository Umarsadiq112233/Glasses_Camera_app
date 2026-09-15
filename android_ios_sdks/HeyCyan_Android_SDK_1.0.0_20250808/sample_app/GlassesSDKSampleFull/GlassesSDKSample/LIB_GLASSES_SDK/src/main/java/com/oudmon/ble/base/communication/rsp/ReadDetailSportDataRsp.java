package com.oudmon.ble.base.communication.rsp;
import com.oudmon.ble.base.communication.entity.BleStepDetails;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

import java.util.ArrayList;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.22 取得指定日期详细运动信息： (主要是从flash中读取数据)
 */

public class ReadDetailSportDataRsp extends BaseRspCmd {

    private ArrayList<BleStepDetails> bleStepDetailses = new ArrayList<>();
    private int index = 0;
    private boolean calorieNewProtocol=false;

    @Override
    public boolean acceptData(byte[] data) {
//         Log.i(TAG,  DataTransferUtils.getHexString(data));
        byte flag = data[0];
        if (index == 0 && (flag & 0xff) == 0xff) {
            bleStepDetailses.clear();
            return false;
        }
        if (index == 0 && (flag & 0xff) == 0xF0) {//报告数据大小
            if(data[2]==1){
                calorieNewProtocol=true;
            }
            index++;
            bleStepDetailses.clear();
        } else {//数据
            BleStepDetails stepDetail = new BleStepDetails();
            stepDetail.setYear(BLEDataFormatUtils.BCDToDecimal(data[1 - 1]) + 2000);
            stepDetail.setMonth(BLEDataFormatUtils.BCDToDecimal(data[2 - 1]));
            stepDetail.setDay(BLEDataFormatUtils.BCDToDecimal(data[3 - 1]));

            stepDetail.setTimeIndex(data[4 - 1]);
            int calorie= BLEDataFormatUtils.bytes2Int(new byte[] {data[8 - 1], data[7 - 1]});
            if(calorieNewProtocol){
                calorie=calorie*10;
            }
            stepDetail.setCalorie(calorie); // 得到单位为卡的数据
            stepDetail.setWalkSteps(BLEDataFormatUtils.bytes2Int(new byte[] {data[10 - 1], data[9 - 1]}));
            int distance=BLEDataFormatUtils.bytes2Int(new byte[] {data[12 - 1], data[11 - 1]});
            stepDetail.setDistance(distance);  // 单位为千米


            bleStepDetailses.add(stepDetail);
            index++;
            if (data[5 - 1] == data[6 - 1] - 1) {//最后一条
                return false;
            }
        }
        return true;
    }

    public ArrayList<BleStepDetails> getBleStepDetailses() {
        return bleStepDetailses;
    }
}
