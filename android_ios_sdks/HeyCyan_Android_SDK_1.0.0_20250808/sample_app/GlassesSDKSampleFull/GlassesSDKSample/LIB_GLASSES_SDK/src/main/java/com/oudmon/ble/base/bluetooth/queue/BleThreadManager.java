package com.oudmon.ble.base.bluetooth.queue;
import com.oudmon.ble.base.bluetooth.BleOperateManager;

import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author gs
 * @CreateDate: /6/23 15:55
 * <p>
 * "佛主保佑,
 * 永无bug"
 */
public class BleThreadManager {
    private static BleThreadManager instance;

    private BlockingDeque<BleDataBean> queue=new LinkedBlockingDeque<>(20);
    private BleConsumer bleConsumer;

    private BleThreadManager() {
        if(bleConsumer==null){
            bleConsumer=new BleConsumer("bleConsumer-"+new Random().nextInt(),queue);
            bleConsumer.start();
        }
    }

    public static BleThreadManager getInstance(){
        if(instance==null){
            synchronized (BleThreadManager.class){
                if(instance==null){
                    instance=new BleThreadManager();
                }
                return instance;
            }
        }
        return instance;
    }

    public void addData(BleDataBean bean) {
        try {
            if(!BleOperateManager.getInstance().isConnected()){
                return;
            }
            queue.putLast(bean);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void clean(){
        queue.clear();
    }

}
