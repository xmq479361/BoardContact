package com.xmqiu.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;


public class AppActivity extends AppCompatActivity {
    final ActivityHandler mHandler;

    public AppActivity() {
        mHandler = new ActivityHandler(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                return AppActivity.this.queueIdle();
            }
        });
    }
    protected boolean queueIdle(){ return false; }

    public class ActivityHandler<T extends AppActivity> extends Handler {
        //第二步，将需要引用T的地方，改成弱引用。
        private WeakReference<T> atyInstance;
        private AtomicBoolean cancel = new AtomicBoolean(false);

        public ActivityHandler(T aty) {
            this.atyInstance = new WeakReference<T>(aty);
        }

        /**
         * 是否结束处理
         *
         * @return
         */
        public boolean isCancel() {
            return cancel.get();
        }

        /**
         * 是否结束处理
         */
        public void cancel() {
            synchronized (ActivityHandler.class) {
                cancel.compareAndSet(false, true);
                removeCallbacksAndMessages(null);
                atyInstance.clear();
            }
        }

        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            // 如果已经取消处理,则不调用
            if (isCancel()) {
                return false;
            }
            return super.sendMessageAtTime(msg, uptimeMillis);
        }

        @Override
        public void handleMessage(Message msg) {
            T aty = atyInstance == null ? null
                    : atyInstance.get();
            //如果T被释放回收了，则不处理这些消息
            if (atyInstance != null && (aty == null || aty.isFinishing())) {
                atyInstance.clear();
                return;
            }
            // 如果已经取消处理,则不调用
            if (isCancel()) {
                return;
            }
            super.handleMessage(msg);
        }
    }

    public void postAsync(Runnable runnable) {
        new Thread(runnable).start();
    }

    public void post(Runnable runnable) {
        runOnUiThread(runnable);
    }

    public void postDelayed(Runnable runnable, long delayedTime) {
        if (mHandler != null) {
            mHandler.postDelayed(runnable, delayedTime);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.cancel();
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
