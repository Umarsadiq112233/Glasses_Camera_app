package com.oudmon.ble.base.bluetooth.queue;

import static com.oudmon.ble.base.bluetooth.QCDataParser.TAG;

import android.util.Log;

import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.ble.base.request.WriteRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

/**
 * @author gs
 * @CreateDate: /6/23 15:52
 * <p>
 * "佛主保佑,
 * 永无bug"
 */
public class BleConsumer extends Thread {
    private static final UUID SERIAL_PORT_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    BlockingQueue<BleDataBean> blockingQueue;

    public BleConsumer(String name, BlockingQueue<BleDataBean> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }


    @Override
    public void run() {
        try {
            while (true) {
                BleDataBean bean = blockingQueue.take();
                int index=0;
                if(bean.getSleepTime()>0){
                    Thread.sleep(bean.getSleepTime());
                }
                int mPackageLength=bean.getSubLength();
                while (index * mPackageLength < bean.getData().length) {
                    BleOperateManager.getInstance().execute(getWriteRequest(Arrays.copyOfRange(bean.getData(), index * mPackageLength, index * mPackageLength + Math.min(mPackageLength,  bean.getData().length- index * mPackageLength))));
                    index++;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将要传送的Data转换成一个可以发送的包
     *
     * @param cmdId 指令ID
     * @param data  要发送的数据data
     * @return 转换后的包
     */
    private byte[] addHeader(int cmdId, byte[] data) {
        byte[] pocket = new byte[(data == null ? 0 : data.length) + 6];
        pocket[0] = (byte) 0xbc;
        pocket[1] = (byte) cmdId;
        if (data != null && data.length > 0) {
            System.arraycopy(DataTransferUtils.shortToBytes((short) data.length), 0, pocket, 2, 2);     //pocket[2], pocket[3]为数据长度信息
            System.arraycopy(DataTransferUtils.shortToBytes((short) CRC16.calcCrc16(data)), 0, pocket, 4, 2);   //pocket[4], pocket[5]为CRC校验
            System.arraycopy(data, 0, pocket, 6, data.length);  //pocket[6]以后为数据data
        } else {
            pocket[4] = (byte) 0xff;
            pocket[5] = (byte) 0xff;
        }
        return pocket;
    }

    public WriteRequest getWriteRequest(byte[] data) {
         Log.i(TAG, "getWriteRequest: data=" + DataTransferUtils.getHexString(data));
        WriteRequest noRspInstance = WriteRequest.getNoRspInstance(SERIAL_PORT_SERVICE, SERIAL_PORT_CHARACTER_WRITE);
       noRspInstance.setValue(data);
        return noRspInstance;
    }
}
