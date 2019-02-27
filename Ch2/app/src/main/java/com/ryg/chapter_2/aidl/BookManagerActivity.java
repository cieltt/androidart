package com.ryg.chapter_2.aidl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ryg.chapter_2.R;

import java.util.List;

public class BookManagerActivity extends Activity {

    private static final String TAG = "BookManagerActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private IBookManager mRemoteBookManager;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_NEW_BOOK_ARRIVED:
                Log.d(TAG, "receive new book :" + msg.obj);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binder died. tname:" + Thread.currentThread().getName());
            if (mRemoteBookManager == null)
                return;
            mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManager = null;
            // TODO:这里重新绑定远程Service
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            IBookManager bookManager = IBookManager.Stub.asInterface(service);
            mRemoteBookManager = bookManager;
            try {
                mRemoteBookManager.asBinder().linkToDeath(mDeathRecipient, 0);
                List<Book> list = bookManager.getBookList();
                Log.i(TAG, "query book list, list type:"
                        + list.getClass().getCanonicalName());
                Log.i(TAG, "query book list:" + list.toString());
                Book newBook = new Book(3, "Android进阶");
                bookManager.addBook(newBook);
                Log.i(TAG, "add book:" + newBook);
                List<Book> newList = bookManager.getBookList();
                Log.i(TAG, "query book list:" + newList.toString());
                Log.i(TAG, "registerListener asBinder client: " + mOnNewBookArrivedListener.asBinder());
                Log.d(TAG, "registerListener client,listener" + mOnNewBookArrivedListener.getClass().getCanonicalName() + " listener: "+mOnNewBookArrivedListener);
                bookManager.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mRemoteBookManager = null;
            Log.d(TAG, "onServiceDisconnected. tname:" + Thread.currentThread().getName());
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook)
                    .sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manager);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onButton1Click(View view) {
        Toast.makeText(this, "click button1", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (mRemoteBookManager != null) {
                    try {
                        List<Book> newList = mRemoteBookManager.getBookList();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (mRemoteBookManager != null
                && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                Log.i(TAG, "unregister listener:" + mOnNewBookArrivedListener);
                mRemoteBookManager
                        .unregisterListener(mOnNewBookArrivedListener);
                /**
                 * 参考
                 * @Override public void registerListener(com.ryg.chapter_2.aidl.IOnNewBookArrivedListener listener) throws android.os.RemoteException
                 * {
                 * android.os.Parcel _data = android.os.Parcel.obtain();
                 * android.os.Parcel _reply = android.os.Parcel.obtain();
                 * try {
                 * _data.writeInterfaceToken(DESCRIPTOR);
                 * _data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
                 * mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
                 * _reply.readException();
                 * }
                 * 其中就而在IOnNewBookArrivedListener接口而言，客户端（activity）是Stub，managerService是proxy
                 * 而BookManager接口而言，客户端（activity）是proxy，managerService是Stub
                 * 使用RemoteCallBackList成功的原因是使用了同一个mOnNewBookArrivedListener，而在IOnNewBookArrivedListener.Stub.Proxy
                 * 中因为使用了相同的mOnNewBookArrivedListener，所以写到Parcel data 中的listener.asBinder()是同一个IOnNewBookArrivedListener.Stub，
                 * 虽然manager那一端从onTransact中register和unregister生成了两个IOnNewBookArrivedListener.Stub.Proxy,但是其内部的mRemote是同一个，
                 * 也是因为客户端（activity）BookManager proxy写入的是一样的IOnNewBookArrivedListener.Stub：
                 * case TRANSACTION_registerListener:
                 * {
                 * data.enforceInterface(descriptor);
                 * com.ryg.chapter_2.aidl.IOnNewBookArrivedListener _arg0;
                 * _arg0 = com.ryg.chapter_2.aidl.IOnNewBookArrivedListener.Stub.asInterface(data.readStrongBinder());
                 * this.registerListener(_arg0);
                 * reply.writeNoException();
                 * return true;
                 * }
                 * case TRANSACTION_unregisterListener:
                 * {
                 * data.enforceInterface(descriptor);
                 * com.ryg.chapter_2.aidl.IOnNewBookArrivedListener _arg0;
                 * _arg0 = com.ryg.chapter_2.aidl.IOnNewBookArrivedListener.Stub.asInterface(data.readStrongBinder());
                 * this.unregisterListener(_arg0);
                 * reply.writeNoException();
                 * return true;
                 * }
                 *
                 *
                 */


            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }

}
