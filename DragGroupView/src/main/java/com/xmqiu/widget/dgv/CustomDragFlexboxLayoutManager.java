package com.xmqiu.widget.dgv;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.flexbox.CustomFlexboxLayoutManager;
import com.google.android.flexbox.FlexLine;

import java.util.List;

/**
 * 重写LayoutManager，实现支持固定行数收缩与展开--》动态子view高度
 * @date 2018/2/2.
 * @author xmqiu
 */
public class CustomDragFlexboxLayoutManager extends CustomFlexboxLayoutManager {
    private View mParent;
    private Callback callback;

    public CustomDragFlexboxLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onAttachedToWindow(RecyclerView recyclerView) {
        super.onAttachedToWindow(recyclerView);
        mParent = (View) recyclerView.getParent();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (state.didStructureChange()) {
            notifyResetShrink(state);
        }
    }

    void notifyResetShrink(final RecyclerView.State state) {
        if (callback != null) {
            mParent.post(new Runnable() {
                @Override
                public void run() {
                    List<FlexLine> flexLines = getFlexLinesInternal();
                    callback.onStructureChange(flexLines);
                }
            });
        }
    }

    public void setStructureChangeCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * 通知结构高度改变
     * @date 2018/2/2.
     * @author xmqiu
     */
    public interface Callback {
        /**
         * 通知结构高度改变
         * @param flexLines
         */
        void onStructureChange(List<FlexLine> flexLines);
    }
}
