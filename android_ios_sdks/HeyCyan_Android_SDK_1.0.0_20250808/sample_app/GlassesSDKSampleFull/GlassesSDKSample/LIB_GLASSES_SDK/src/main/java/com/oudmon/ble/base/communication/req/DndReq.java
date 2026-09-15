package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.entity.StartEndTimeEntity;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.6勿扰模式命令
 */

public class DndReq extends MixtureReq{

    private DndReq() {
        super(Constants.CMD_MUTE);
        subData = new byte[]{0x01};
    }

    private DndReq(boolean isEnable, StartEndTimeEntity dndEntity) {
        super(Constants.CMD_MUTE);
        subData=new byte[]{0x02, (byte) (isEnable ? 0x01 : 0x02),
                (byte) (dndEntity.getStartHour() & 0xff),
                (byte) (dndEntity.getStartMinute() & 0xff),
                (byte) (dndEntity.getEndHour() & 0xff),
                (byte) (dndEntity.getEndMinute() & 0xff)};
    }
    public static DndReq getReadInstance(){
        return new DndReq();
    }

    public static DndReq getWriteInstance(boolean isEnable,StartEndTimeEntity dndEntity) {
        return new DndReq(isEnable, dndEntity);
    }

}
