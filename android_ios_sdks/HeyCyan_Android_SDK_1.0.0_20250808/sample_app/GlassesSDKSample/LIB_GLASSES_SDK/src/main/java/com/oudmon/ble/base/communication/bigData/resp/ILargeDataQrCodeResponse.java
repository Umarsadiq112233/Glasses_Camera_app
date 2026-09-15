package com.oudmon.ble.base.communication.bigData.resp;
import com.oudmon.ble.base.communication.bigData.bean.ECardEntity;

public interface ILargeDataQrCodeResponse {
    void qrCode(ECardEntity resp);
}
