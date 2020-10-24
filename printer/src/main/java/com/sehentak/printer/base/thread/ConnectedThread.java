package com.sehentak.printer.base.thread;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.sehentak.printer.base.func.BluetoothService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectedThread extends Thread {
    private String TAG = this.getClass().getSimpleName();
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothService mService;

    public ConnectedThread(BluetoothSocket socket, BluetoothService service) {
        Log.d(TAG, "create ConnectedThread");
        mService = service;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                byte[] buffer = new byte[256];
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                if(bytes>0) {
                    // Send the obtained bytes to the UI Activity
                    mService.mHandler.obtainMessage(2, bytes, -1, buffer)
                            .sendToTarget();
                } else {
//                    Log.e(TAG, "disconnected");
                    mService.connectionLost();

                    //add by chongqing jinou
                    if(mService.mState != mService.STATE_NONE) {
//                        Log.e(TAG, "disconnected");
                        // Start the service over to restart listening mode
                        mService.start();
                    }
                    break;
                }
            } catch (IOException e) {
//                Log.e(TAG, "disconnected", e);
                mService.connectionLost();

                //add by chongqing jinou
                if(mService.mState != mService.STATE_NONE) {
                    // Start the service over to restart listening mode
                    mService.start();
                }
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     * @param buffer  The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);
            mmOutStream.flush();//清空缓存
               /* if (buffer.length > 3000) //
                {
                  byte[] readata = new byte[1];
                  SPPReadTimeout(readata, 1, 5000);
                }*/
            Log.i("BTPWRITE", new String(buffer,"GBK"));
            // Share the sent message back to the UI Activity
            mService.mHandler.obtainMessage(3, -1, -1, buffer)
                    .sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    /*
    //
    private boolean SPPReadTimeout(byte[] Data, int DataLen, int Timeout){
      for (int i = 0; i < Timeout / 5; i++)
      {
        try
        {
          if (mmInStream.available() >= DataLen)
          {
            try
            {
                mmInStream.read(Data, 0, DataLen);
              return true;
            }
            catch (IOException e)
            {
              ErrorMessage = "读取蓝牙数据失败";
              return false;
            }
          }
        }
        catch (IOException e)
        {
          ErrorMessage = "读取蓝牙数据失败";
          return false;
        }
        try
        {
          Thread.sleep(5L);
        }
        catch (InterruptedException e)
        {
          ErrorMessage = "读取蓝牙数据失败";
          return false;
        }
      }
      ErrorMessage = "蓝牙读数据超时";
      return false;
    }
    */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}
