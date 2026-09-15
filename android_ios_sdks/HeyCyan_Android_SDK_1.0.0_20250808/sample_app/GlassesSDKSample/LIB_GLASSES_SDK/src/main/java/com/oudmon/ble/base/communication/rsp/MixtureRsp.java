package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 注意，MixtureRsp针对的是能读能写的数据操作，
 * 比如各个开关等。
 * 如果只是读取数据，如读取血压、心率、电量等数据，
 * 最好是继承BaseRspCmd
 */

public abstract class MixtureRsp extends BaseRspCmd {

    /**
     * 读操作
     */
    public static final byte ACTION_READ = 0x01;
    /**
     * 写操作
     */
    public static final byte ACTION_WRITE = 0x02;
    /**
     * 删除操作
     */
    public static final byte ACTION_DELETE = 0x03;


    /**
     * 响应的类型 读响应和写响应
     */
    private byte action;

    @Override
    public boolean acceptData(byte[] data) {
        action = data[0];
        if (action == ACTION_READ) {
            readSubData(data);
        }
        return false;
    }

    /**
     * 只有在读取信息的时候才会调用该方法，从而改变内部属性值
     * 如果是在写信息的时候，我们目前是没有调用该方法，而自定义的属性仍然是原始值
     * @param subData data
     */
    protected abstract void readSubData(byte[] subData);

    public byte getAction() {
        return action;
    }
}
