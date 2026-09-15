package com.oudmon.ble.base.communication;
import android.util.Log;
import org.jvcompress.lzo.MiniLZO;
import org.jvcompress.util.MInt;
import java.util.Arrays;

/**
 * Created by Jxr35 on 2018/6/26
 */

public class CompressUtils {

    private static final String TAG = "Jxr35";

    public static byte[] compress(byte[] input) {
        try {
            byte[] output = new byte[input.length + input.length / 16 + 64 + 3];
            int[] dict = new int[64 * 1024];
            MInt mInt = new MInt();
            MiniLZO.lzo1x_1_compress(input, input.length, output, mInt, dict);
            byte[] compress = Arrays.copyOfRange(output, 0, mInt.v);

//             Log.i(TAG,"compress.. inputLength: " + input.length + ", input: " + DataTransferUtils.getHexString(input));
//             Log.i(TAG,"compress.. outputLength: " + output.length + ", output: " + DataTransferUtils.getHexString(output));
//             Log.i(TAG,"compress.. compressLength: " + compress.length + ", compress: " + DataTransferUtils.getHexString(compress));
//             Log.i(TAG,"compress.. mInt: " + mInt.v);
            return compress;

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "compress.. FileNotFoundException");
        }
        return new byte[0];
    }

}
