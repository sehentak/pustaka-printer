package com.sehentak.printer.base.thread;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.sehentak.printer.BuildConfig;
import com.sehentak.printer.base.func.BluetoothService;

import java.io.IOException;

/**
 * This thread runs while listening for incoming connections. It behaves
 * like a server-side client. It runs until a connection is accepted
 * (or until cancelled).
 */
public class AcceptThread extends Thread {
    private String TAG = this.getClass().getSimpleName();

    // The local server socket
    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothService mService;

    public AcceptThread(BluetoothService service) {
        mService = service;
        BluetoothServerSocket tmp = null;

        // Create a new listening server socket
        try {
            tmp = mService.mAdapter.listenUsingRfcommWithServiceRecord(mService.NAME, mService.MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "listen() failed", e);
        }
        mmServerSocket = tmp;
    }

    @Override
    public void run() {
        if (BuildConfig.DEBUG) Log.d(TAG, "BEGIN mAcceptThread" + this);
        setName("AcceptThread");
        BluetoothSocket socket;

        // Listen to the server socket if we're not connected
        while (mService.mState != mService.STATE_CONNECTED) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();
            } catch (Exception e) {
//                    Log.e(TAG, "accept() failed", e);
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                synchronized (mService) {
                    if (mService.mState == mService.STATE_LISTEN) {

                    } else if (mService.mState == mService.STATE_CONNECTING) {
                        // Situation normal. Start the connected thread.
                        mService.connected(socket, socket.getRemoteDevice());
                    } else if (mService.mState == mService.STATE_NONE) {
                    } else if (mService.mState == mService.STATE_CONNECTED) {
                        // Either not ready or already connected. Terminate new socket.
                        try { socket.close(); }
                        catch (IOException e) {
                            Log.e(TAG, "Could not close unwanted socket", e);
                        }
                    } else throw new IllegalStateException("Unexpected value: " + mService.mState);
                }
            }
        }
        if (BuildConfig.DEBUG) Log.i(TAG, "END mAcceptThread");
    }

    public void cancel() {
        if (BuildConfig.DEBUG) Log.d(TAG, "cancel " + this);
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of server failed", e);
        }
    }
}
