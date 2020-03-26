package com.xmqiu.widget.dgv;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexLine;
import com.xmqiu.utils.view.ViewHelper;
import com.xmqiu.utils.view.ViewsUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.xmqiu.widget.dgv.BoardView.TYPE_AMOUNT;
import static com.xmqiu.widget.dgv.BoardView.TYPE_INPUT;

/**
 * 船帆. <br/>
 * 这里通过两个管理RecyclerView来实现，状态的切换。即可编辑模式和展示收缩模式
 * @author xmqiu
 * @date 2018/2/2.
 */
public class SailLayout extends LinearLayout {
    final String TAG = "SailLayout";
    private int mCanvasViewId, mPlugViewId;
    private int mTouchSlop;
    private View mPlugView;
    /** 编辑模式下的RecyclerView */
    private DragItemRecyclerView mPlugRecyclerView;
    /** 展示下的RecyclerView, 支持展示收缩模式 */
    public DragItemRecyclerView mCanvasView;
    private SailListener sailListener;
    private ContactInputCallback inputCallback;
    private boolean isInitData = true;
    /**
     * 摘要显示的最大行数
     */
    final int MAX_LINE = 2;

    public SailLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SailLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SailLayout);
        mPlugViewId = a.getResourceId(R.styleable.SailLayout_plugId, -1);
        mCanvasViewId = a.getResourceId(R.styleable.SailLayout_canvasId, -1);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取高度测量模式
        int modeVertical = MeasureSpec.getMode(heightMeasureSpec);
        //判断宽度模式以演示
        switch (modeVertical) {
            //当本控件在xml中宽度上写入 wrap_content时为此模式 这时返回的width一般为0
            case MeasureSpec.AT_MOST:
                int maxHeight = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    if (view.getVisibility() == View.GONE) {
                        continue;
                    }
                    maxHeight = Math.max(view.getMeasuredHeight() + view.getPaddingTop() + view.getPaddingBottom(), maxHeight);
                }
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, modeVertical);
                break;
            case MeasureSpec.EXACTLY:
                //当本控件在xml中宽度上写入实际值时为此模式 如20dp match_parent(与父布局一样的数值) 这里返回的width也就是xml中写明的数值
            case MeasureSpec.UNSPECIFIED:
                //只有在ListView或类似的控件中会出现 表示不关心大小 这时返回的width一般为0
            default:
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate(); // id_sail_canvas
        setOrientation(LinearLayout.HORIZONTAL);
        mCanvasView = ViewHelper.findById(this, mCanvasViewId);
        mPlugView = ViewHelper.findById(this, mPlugViewId);
        if (mPlugView instanceof DragItemRecyclerView) {
            mPlugRecyclerView = (DragItemRecyclerView) mPlugView;
            mPlugRecyclerView.setHorizontalScrollBarEnabled(false);
            mPlugRecyclerView.setVerticalScrollBarEnabled(false);
            mPlugRecyclerView.setHasFixedSize(false);
            mPlugRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mPlugRecyclerView.addOnItemTouchListener(checkTouchListener);
        }
        mCanvasView.setHorizontalScrollBarEnabled(false);
        mCanvasView.setVerticalScrollBarEnabled(false);
        mCanvasView.setHasFixedSize(false);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
//        defaultItemAnimator.setMoveDuration(50);
        defaultItemAnimator.setAddDuration(120);
        mCanvasView.setItemAnimator(defaultItemAnimator);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setFocusableInTouchMode(false);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "RecyclerView onClick isSail: " + isSail());
                if (!isSail()) {
                    if (sailListener != null) {
                        // && sailListener.setSail(true)
                        sailListener.onSelect(SailLayout.this);
                    }
                    setSail(true);
                    rquestInput(true);
                }
            }
        });
    }

    public void rquestInput(boolean withSoftInput) {
        View lastItemView = mCanvasView.findViewById(R.id.id_board_drag_item_input);
        if (lastItemView != null && !lastItemView.isFocused()) {
            lastItemView.requestFocus();
            if (withSoftInput) ViewsUtil.showSoftInputMethod(lastItemView);
        }
    }

    public void clearInputFocus() {
        View lastItemView = mCanvasView.findViewById(R.id.id_board_drag_item_input);
        if (lastItemView != null && lastItemView.isFocused()) {
            ViewsUtil.hideSoftInputMethod(lastItemView);
            lastItemView.clearFocus();
        }
    }

    public boolean isSail() {
        if (mCanvasView != null && mPlugRecyclerView != null) {
            return (mCanvasView.getVisibility() == View.VISIBLE && mPlugRecyclerView.getVisibility() == View.GONE);
        }
        return false;
    }

    /**
     * 扬帆
     *
     * @param isSailOn
     */
    public void setSail(final boolean isSailOn) {
        if (mCanvasView != null && mPlugRecyclerView != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (isSailOn) {
                        if (mCanvasView.getVisibility() != View.VISIBLE) {
                            mCanvasView.setVisibility(View.VISIBLE);
                        }
                        if (mPlugRecyclerView.getVisibility() != View.GONE) {
                            mPlugRecyclerView.setVisibility(View.GONE);
                        }
                    } else {
                        EditText inputText = ViewHelper.findById(mCanvasView, R.id.id_board_drag_item_input);
                        if (inputCallback != null && inputText != null && !TextUtils.isEmpty(inputText.getText())) {
                            inputCallback.onEditorAction(inputText, EditorInfo.IME_ACTION_NEXT, null);
                        }
                        mCanvasView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mPlugRecyclerView.getVisibility() != View.VISIBLE) {
                                    mPlugRecyclerView.setVisibility(View.VISIBLE);
                                }
                                if (mCanvasView.getVisibility() != View.GONE) {
                                    mCanvasView.setVisibility(View.GONE);
                                }
                            }
                        }, 100);
                    }
                }
            });
        }
    }

    public void onDestory() {
        inputCallback = null;
        inputKeyListener = null;
        inputEditorActionListener = null;
        inputTextWatch = null;
    }

    public DragItemRecyclerView attachDragRecyclerView(final DragItemAdapter adapter, final boolean isNeedSetSail) {
        if (!adapter.isLastItemInputOrAmount())
            adapter.addTextItem(TYPE_INPUT, "");
        mCanvasView.setAdapter(adapter);
        final CustomDragFlexboxLayoutManager layoutManager = new CustomDragFlexboxLayoutManager(getContext());
        layoutManager.setStructureChangeCallback(new CustomDragFlexboxLayoutManager.Callback() {
            @Override
            public void onStructureChange(List<FlexLine> flexLines) {
                checkAddEventListener();
                List list = adapter.getItemList();
                final int totalSize = list.size(), lineNum = flexLines.size();
                int showCount = totalSize - 1;
                if (lineNum > MAX_LINE) {
                    showCount = checkToGetShowCountSummary(flexLines, lineNum, 0) - 1;
                }
                DragItemAdapter copy = adapter.copyTo(totalSize, list, showCount);
                if (lineNum > MAX_LINE) { // 超过x行，则展示为缩略，摘要显示
                    copy.addTextItem(TYPE_AMOUNT, String.format("等%d人...", totalSize - 1));
                }
                if (!isInitData) {
                    mPlugRecyclerView.setAdapter(copy);
                } else {
                    isInitData = false;
                    mPlugRecyclerView.setLayoutManager(new CustomDragFlexboxLayoutManager(getContext()));
                    mPlugRecyclerView.setAdapter(copy);
                    mPlugRecyclerView.post(new Runnable() {
                        public void run() {
                            setSail(false);
                        }
                    });
                }
            }
        });
        mCanvasView.setLayoutManager(layoutManager);
        isInitData = true;
        return mCanvasView;
    }

    /**
     * 检查获取需要显示的摘要数量
     *
     * @param flexLines
     * @param lineNum
     * @param showCount
     * @return
     */
    private int checkToGetShowCountSummary(List<FlexLine> flexLines, int lineNum, int showCount) {
        int maxSizeInLine = 0, lastLineMainSize = 0;
        for (int i = 0; i < lineNum; i++) { // 添加指定行数数据
            FlexLine flexLine = flexLines.get(i);
            maxSizeInLine = Math.max(flexLine.getMainSize(), maxSizeInLine);
            if (MAX_LINE > i) {
                showCount += flexLine.getItemCountNotGone();
                if (i == MAX_LINE - 1)
                    lastLineMainSize = flexLine.getMainSize();
            }
        }
        Log.i(TAG, "checkToGetShowCountSummary: " + maxSizeInLine + " = " + lastLineMainSize + " = " + ViewHelper.dip2px(getContext(), 50));
        // 如果该大小
        if (maxSizeInLine - lastLineMainSize >= ViewHelper.dip2px(getContext(), 65))
            showCount += 1;
        return showCount;
    }

    private void checkAddEventListener() {
        View childView = mCanvasView.findViewById(R.id.id_board_drag_item_input);
        if (childView != null && childView instanceof EditText && childView.getOnFocusChangeListener() == null) {
            EditText inputView = ((EditText) childView);
            if (inputCallback != null)
                inputCallback.onFindInputEdit(this, inputView);
            inputView.setOnFocusChangeListener(new OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (inputCallback != null)
                        inputCallback.onFocusChanged(mCanvasView, v, hasFocus);
                }
            });
            inputView.setOnKeyListener(inputKeyListener);
            inputView.addTextChangedListener(inputTextWatch = new SailTextWatcher(inputView));
            inputView.setOnEditorActionListener(inputEditorActionListener);
        }
    }

    /**
     * 初始化监听事件
     *
     * @param dragItemListener
     * @param dragItemCallback
     */
    public void initListener(
            DragItemRecyclerView.DragItemListener dragItemListener,
            DragItemRecyclerView.DragItemCallback dragItemCallback) {
        mCanvasView.setDragItemListener(dragItemListener);
        mCanvasView.setDragItemCallback(dragItemCallback);
    }


    public void setSailListener(SailListener sailListener) {
        this.sailListener = sailListener;
    }

    public DragItemRecyclerView getCanvasView() {
        return mCanvasView;
    }

    interface SailListener {
        void setSail(boolean isSailOn);

        void onSelect(SailLayout onItemTouchListener);
    }

    public RecyclerView.OnItemTouchListener checkTouchListener = new RecyclerView.OnItemTouchListener() {
        boolean userDownAndMoveView = false;
        AtomicBoolean autoSailCheckRun = new AtomicBoolean(false);
        PointF downPoint = new PointF();
        int lastY;

        Runnable checkToStartSail = new Runnable() {
            @Override
            public void run() {
//                LogTools.i(TAG, "checkToStartSail: " + userDownAndMoveView);
                if (userDownAndMoveView) {
                    autoSailCheckRun.compareAndSet(false, true);
                    final long downTime = SystemClock.uptimeMillis();
                    onInterceptTouchEvent(mPlugRecyclerView, MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_UP, downPoint.x, downPoint.y, 0));
                    mCanvasView.post(new Runnable() {
                        @Override
                        public void run() {
//                            LogTools.i(TAG, "checkToStartSail performLongClick: " + dragPosition + " , " + userDownAndMoveView);
                            View childAt = mCanvasView.getChildAt(dragPosition);
                            if (childAt != null)
                                childAt.performLongClick();
                        }
                    });
                    autoSailCheckRun.compareAndSet(true, false);
                }
            }
        };
        int dragPosition;

        @Override
        public boolean onInterceptTouchEvent(final RecyclerView rv, final MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    userDownAndMoveView = true;
                    lastY = (int) e.getRawY();
                    downPoint.set(e.getRawX(), (int) e.getRawY());
                    postDelayed(checkToStartSail, 400);
                    if (rv instanceof DragItemRecyclerView)
                        dragPosition = ((DragItemRecyclerView) rv).getDragPosition(e.getX(), e.getY());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (userDownAndMoveView) {
                        float moveDisX = downPoint.x - e.getRawX(), moveDisY = downPoint.y - e.getRawY();
//                      Log.i(TAG, "onTouchEvent move: " + mTouchSlop + " = " + moveDisX + " x " + moveDisY);
                        if (Math.abs(moveDisX) > mTouchSlop || Math.abs(moveDisY) > mTouchSlop) {
                            removeCallbacks(checkToStartSail);
                            userDownAndMoveView = false;
                            Log.i(TAG, "actionMove: " + moveDisX + "x" + moveDisY + " : " + userDownAndMoveView);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!autoSailCheckRun.get()) {
                        removeCallbacks(checkToStartSail);
                    }
                    if (userDownAndMoveView) {
//                      && sailListener != null   sailListener.setSail(true);
                        if (sailListener != null) {
                            // && sailListener.setSail(true)
                            sailListener.onSelect(SailLayout.this);
                        }
                        setSail(true);
                        mCanvasView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                rquestInput(false);
                            }
                        }, 25);
                    }
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    Log.i(TAG, "isDragging handleTouchEvent: ACTION_OUTSIDE " + autoSailCheckRun.get());
                    if (!autoSailCheckRun.get()) {
                        removeCallbacks(checkToStartSail);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if (!autoSailCheckRun.get()) {
                        removeCallbacks(checkToStartSail);
                    }
                default:
                    break;
            }
//            if (e.getAction() != MotionEvent.ACTION_MOVE)
//                Log.i(TAG, "onTouchEvent: " + e.getAction() + " = " + userDownAndMoveView);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(
                boolean disallowIntercept) {
        }
    };

    public ContactInputCallback getInputCallback() {
        return inputCallback;
    }

    public void setInputCallback(ContactInputCallback inputCallback) {
        this.inputCallback = inputCallback;
    }

    public interface ContactInputCallback extends TextView.OnEditorActionListener, OnKeyListener {
        /**
         * 输入框焦点改变回调
         *
         * @param recyclerView
         * @param inputView
         * @param isFocused
         * @return
         */
        boolean onFocusChanged(DragItemRecyclerView recyclerView, View inputView, boolean isFocused);

        /**
         * 文本改变回调
         *
         * @param mCanvasView
         * @param view
         * @param afterS
         */
        void onTextChanged(DragItemRecyclerView mCanvasView, EditText view, CharSequence afterS);

        void onFindInputEdit(SailLayout sailLayout, EditText inputView);
    }

    /**
     * 联系人输入监听
     */
    class SailTextWatcher implements TextWatcher {
        EditText view;
        CharSequence afterS = "";
        boolean isEditedFlag;

        public SailTextWatcher(EditText view) {
            this.view = view;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after) {
            afterS = s;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable e) {
            isEditedFlag = true;
            if (inputCallback != null) {
                inputCallback.onTextChanged(mCanvasView, view, afterS);
            }
        }
    }

    private SailTextWatcher inputTextWatch;
    /**
     * 联系人的编辑监听
     */
    private TextView.OnEditorActionListener inputEditorActionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int id, KeyEvent event) {
            if (inputCallback != null)
                return inputCallback.onEditorAction(v, id, event);
            return false;
        }
    };

    /**
     * 联系人的按键事件监听
     */
    private OnKeyListener inputKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View arg0, int keyCode, KeyEvent event) {
            if (inputCallback != null)
                return inputCallback.onKey(arg0, keyCode, event);
            return false;

        }
    };
}
