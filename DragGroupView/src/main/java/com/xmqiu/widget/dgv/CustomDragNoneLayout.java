package com.xmqiu.widget.dgv;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


/**
 * @date 2018/2/2.
 * @author xmqiu
 * 不支持拖拽的默认空布局
 */
public class CustomDragNoneLayout extends LinearLayout implements IDragNone {
    public CustomDragNoneLayout(Context context) {
        super(context);
    }

    public CustomDragNoneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDragNoneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
