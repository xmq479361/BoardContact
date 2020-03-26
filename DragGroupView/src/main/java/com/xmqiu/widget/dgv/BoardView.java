/*
 * Copyright 2014 Magnus Woxblom
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xmqiu.widget.dgv;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.xmqiu.utils.view.ViewHelper;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * 船体容器View. 支持内部NestRecyclerView 交互
 * @date 2018/2/2.
 * @author xmqiu
 */
public class BoardView extends NestedScrollView implements AutoScroller.AutoScrollListener {
    public static final int TYPE_AMOUNT = -3;
    public static final int TYPE_INPUT = -2;
    protected final String TAG = getClass().getSimpleName();

    public interface BoardListener {
        void onItemDragStarted(int column, int row);

        void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow);

        void onItemChangedColumn(int oldColumn, int newColumn);

        void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow);
    }

    public interface BoardCallback {
        boolean canDragItemAtPosition(int column, int row);

        boolean canDropItemAtPosition(int oldColumn, int oldRow, int newColumn, int newRow);
    }

    public enum ColumnSnapPosition {
        TOP, CENTER, BOTTOM
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return true;
    }

    private static final int SCROLL_ANIMATION_DURATION = 325;
    private Scroller mScroller;
    private AutoScroller mAutoScroller;
    private GestureDetector mGestureDetector;
    private ViewGroup mRootLayout;
    private ViewGroup mColumnLayout;
    private ArrayList<DragItemRecyclerView> mLists = new ArrayList<>();
    //    private ArrayList<SailLayout> mLists = new ArrayList<>();
    private SparseArray<View> mHeaders = new SparseArray<>();
    private DragItemRecyclerView mCurrentRecyclerView;
    private DragItem mDragItem;
    private BoardListener mBoardListener;
    private BoardCallback mBoardCallback;
    private boolean mSnapToColumnWhenScrolling = false;
    private boolean mSnapToColumnWhenDragging = true;
    private boolean mSnapToColumnInLandscape = false;
    private ColumnSnapPosition mSnapPosition = ColumnSnapPosition.TOP;
    private int mCurrentColumn;
    private float mTouchX;
    private float mTouchY;
    private int mColumnHeight;
    private int mDragStartColumn;
    private int mDragStartRow;
    private boolean mHasLaidOut;
    private boolean mDragEnabled = true;
    private int mLastDragColumn = NO_POSITION;
    private int mLastDragRow = NO_POSITION;
    private SavedState mSavedState;
    //    protected int boardViewTopOffsetY = 0; // 顶部偏移量
    private int containerOffsetY = 0; // 顶部偏移量
    protected int mTouchSlop;

    public BoardView(Context context) {
        super(context);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        Resources res = getResources();
        mColumnHeight = res.getDisplayMetrics().heightPixels * 2;

        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
        mScroller = new Scroller(getContext(), new DecelerateInterpolator(1.1f));
        mAutoScroller = new AutoScroller(getContext(), this);
        mAutoScroller.setAutoScrollMode(snapToColumnWhenDragging() ? AutoScroller.AutoScrollMode.COLUMN : AutoScroller.AutoScrollMode.POSITION);
        mDragItem = new DragItem(getContext());

        mRootLayout = ViewHelper.findById(this, R.id.id_board_view_frame);
        mColumnLayout = ViewHelper.findById(mRootLayout, R.id.id_board_sail_container);
        mRootLayout.addView(mDragItem.getDragItemView());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // Snap to closes column after first layout.
        // This is needed so correct column is scrolled to after a rotation.
        if (!mHasLaidOut && mSavedState != null) {
            mCurrentColumn = mSavedState.currentColumn;
            scrollToColumn(mCurrentColumn, false);
            mSavedState = null;
        }
        mHasLaidOut = true;
        if (!mLists.isEmpty()) {
            View view = mLists.get(0);
            containerOffsetY = getViewParentTopLimit(view, mRootLayout, 0);
        } else { //if (mRootLayout != null && mColumnLayout!=null)
            containerOffsetY = mRootLayout.getTop() + mColumnLayout.getTop();
        }
    }

    public int getViewParentTopLimit(View view, ViewGroup mColumnLayout, int containerOffsetY) {
        if (view == null) {
            return containerOffsetY;
        }
        containerOffsetY += view.getTop();
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        if (layoutParams != null && layoutParams instanceof RelativeLayout.LayoutParams) {
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutParams;
//            containerOffsetY += params.topMargin + params.bottomMargin;
//            LogTools.d(TAG, "getViewParentTopLimit: " + containerOffsetY + " = " + ViewsHelper.viewsInfo(view)
//                    + " ,topMargin: " + ((RelativeLayout.LayoutParams) layoutParams).topMargin+ " ="+ params.bottomMargin);
//        }
//        Log.d(TAG, "getViewParentTopLimit: " + containerOffsetY + " = " + view);
        if (view == mColumnLayout) {
            return containerOffsetY;
        }
        return getViewParentTopLimit((View) view.getParent(), mColumnLayout, containerOffsetY);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mSavedState = ss;
        requestLayout();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getClosestSnapColumn());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean retValue = handleTouchEvent(event);
        return retValue || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retValue = handleTouchEvent(event);
        return retValue || super.onTouchEvent(event);
    }

    boolean isMoveing = false;

    private boolean handleTouchEvent(MotionEvent event) {
        if (mLists.size() == 0) {
            return false;
        }

        mTouchX = event.getX();
        mTouchY = event.getY();
        if (isDragging()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (!mAutoScroller.isAutoScrolling()) {
                        updateScrollPosition();
                    }
                    if (downY != -1 && Math.abs(event.getY() - downY) > mTouchSlop) {
                        downY = -1;
                        onBoardViewScroll();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mAutoScroller.stopAutoScroll();
                    mCurrentRecyclerView.onDragEnded();
                    if (snapToColumnWhenScrolling()) {
                        scrollToColumn(getColumnOfList(mCurrentRecyclerView), true);
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_DOWN:
                    downY = event.getY();
                    break;
                default:
            }
            return true;
        } else {
            if (mGestureDetector.onTouchEvent(event) && snapToColumnWhenScrolling()) {
                // A page fling occurred, consume event
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!mScroller.isFinished()) {
                        // View was grabbed during animation
                        mScroller.forceFinished(true);
                    }
                    downY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
//                    LogTools.i(TAG, "handleTouchEvent: ACTION_MOVE " + mScroller.getStartY()
//                            + " , " + mScroller.getCurrY() + " = " +
//                            mScroller.computeScrollOffset() +
//                            " =" + mScroller.getFinalY() + " : " + mScroller.isFinished());
                    if (downY != -1 && Math.abs(event.getY() - downY) > mTouchSlop) {
                        isMoveing = true;
                        downY = -1;
                        onBoardViewScroll();
//                        LogTools.i(TAG, "handleTouchEvent: ACTION_MOVE " + mScroller.getStartY()
//                                + " , " + mScroller.getCurrY() + " = " +  mScroller.computeScrollOffset() +
//                                " =" + mScroller.getFinalY() + " : " + mScroller.isFinished());
                        return super.onTouchEvent(event);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isMoveing = false;
                    if (snapToColumnWhenScrolling()) {
                        scrollToColumn(getClosestSnapColumn(), true);
                    }
                default:
            }
            return false;
        }
    }

    float downY = 0;

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (getScrollX() != x || getScrollY() != y) {
                scrollTo(x, y);
            }

            // If auto scrolling at the same time as the scroller is running,
            // then update the drag item position to prevent stuttering item
            if (mAutoScroller.isAutoScrolling()) {
                mDragItem.setPosition(getListTouchX(mCurrentRecyclerView), getListTouchY(mCurrentRecyclerView));
            }

            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            super.computeScroll();
        }
    }

    @Override
    public void onAutoScrollPositionBy(int dx, int dy) {
        if (isDragging()) {
            scrollBy(dx, dy);
            updateScrollPosition();
        } else {
            mAutoScroller.stopAutoScroll();
        }
    }

    @Override
    public void onAutoScrollColumnBy(int columns) {
        if (isDragging()) {
            int newColumn = mCurrentColumn + columns;
            if (columns != 0 && newColumn >= 0 && newColumn < mLists.size()) {
                scrollToColumn(newColumn, true);
            }
            updateScrollPosition();
        } else {
            mAutoScroller.stopAutoScroll();
        }
    }

    boolean recentToggleView = false;

    private void updateScrollPosition() {
        // Updated event to scrollview coordinates
        DragItemRecyclerView currentList = getCurrentRecyclerView(mTouchY + getScrollY());
        final float listTouchY = getListTouchY(mCurrentRecyclerView);
        final boolean isToggleView = (mCurrentRecyclerView != currentList);
        if (isToggleView) {
            int oldColumn = getColumnOfList(mCurrentRecyclerView);
            int newColumn = getColumnOfList(currentList);
            long itemId = mCurrentRecyclerView.getDragItemId();

            // Check if it is ok to drop the item in the new column first
            int newPosition = currentList.getDragPosition(getListTouchX(currentList), getListTouchY(currentList));
            if (mBoardCallback == null || mBoardCallback.canDropItemAtPosition(mDragStartColumn, mDragStartRow, newColumn, newPosition)) {
                Object item = mCurrentRecyclerView.removeDragItemAndEnd();
                // 如果要 移出收缩，则改这里
//                if(mCurrentRecyclerView.getVisibility() == View.VISIBLE){
//                    ViewParent parent = mCurrentRecyclerView.getParent();
//                    if(parent instanceof SailLayout){
//                        ((SailLayout)parent).setSail(false);
////                            ((SailLayout) parent).invalidate();
//                    }
//                }
                if (item != null) {
                    mCurrentRecyclerView = currentList;
                    if (mCurrentRecyclerView.removeWhenHasSameItemInTarget(listTouchY, item, itemId)) {
                        mCurrentRecyclerView.addDragItemAndStart(listTouchY, item, itemId);
                    }
                    if (mCurrentRecyclerView.getVisibility() == View.GONE) {
                        ViewParent parent = mCurrentRecyclerView.getParent();
                        if (parent != null && parent instanceof SailLayout) {
                            ((SailLayout) parent).setSail(true);
                        }
                    }
                    setDragOffset(mCurrentRecyclerView);
                    recentToggleView = true;
                    if (mBoardListener != null) {
                        mBoardListener.onItemChangedColumn(oldColumn, newColumn);
                    }
                }
            }
        } else if (recentToggleView) {
            recentToggleView = false;
            setDragOffset(mCurrentRecyclerView);
        }
        // Updated event to list coordinates
        mCurrentRecyclerView.onDragging(getListTouchX(mCurrentRecyclerView), getListTouchY(mCurrentRecyclerView));

        float scrollEdge = getResources().getDisplayMetrics().heightPixels * 0.14f;

        if (mTouchY > getHeight() - scrollEdge && getScrollY() < mColumnLayout.getHeight()) {
            mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.UP);
        } else if (mTouchY < scrollEdge && getScrollY() > 0) {
            mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.DOWN);
        } else {
            mAutoScroller.stopAutoScroll();
        }
//        invalidate();
        postInvalidate();
    }

    private float getListTouchX(DragItemRecyclerView list) {
        return mTouchX + getScrollX() - ((View) list.getParent()).getLeft() - list.getX();
    }

    private float getListTouchY(DragItemRecyclerView list) {
        return mTouchY - containerOffsetY + getScrollY()
                - list.getY() - ((View) list.getParent()).getY();
    }

    private DragItemRecyclerView getCurrentRecyclerView(float y) {
        y -= containerOffsetY;
        for (DragItemRecyclerView list : mLists) {
            View parent = (View) list.getParent();
            if (parent.getTop() <= y && parent.getBottom() > y) {
                return list;
            }
        }
        return mCurrentRecyclerView;
    }

    protected int getColumnOfList(DragItemRecyclerView list) {
        int column = 0;
        for (int i = 0; i < mLists.size(); i++) {
            RecyclerView tmpList = mLists.get(i);
            if (tmpList == list) {
                column = i;
            }
        }
        return column;
    }

    private int getCurrentColumn(float posX) {
        for (int i = 0; i < mLists.size(); i++) {
            RecyclerView list = mLists.get(i);
            View parent = (View) list.getParent();
            if (parent.getLeft() <= posX && parent.getRight() > posX) {
                return i;
            }
        }
        return 0;
    }

    private int getClosestSnapColumn() {
        int column = 0;
        int minDiffY = Integer.MAX_VALUE;
        for (int i = 0; i < mLists.size(); i++) {
            View listParent = (View) mLists.get(i).getParent();

            int diffY = 0;
            switch (mSnapPosition) {
                case TOP:
                    int topPosY = getScrollY();
//                    diffY = Math.abs(listParent.getTop() - topPosY);
                    diffY = Math.abs(topPosY);
                    break;
                case CENTER:
                    int middlePosY = getScrollY() + getMeasuredHeight() / 2;
                    diffY = Math.abs(listParent.getTop() + mColumnHeight / 2 - middlePosY);
                    break;
                case BOTTOM:
                    int bottomPosY = getScrollY() + getMeasuredHeight();
                    diffY = Math.abs(listParent.getBottom() - bottomPosY);
                    break;
                default:
            }

            if (diffY < minDiffY) {
                minDiffY = diffY;
                column = i;
            }
        }
        return column;
    }

    private boolean snapToColumnWhenScrolling() {
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        return mSnapToColumnWhenScrolling && (isPortrait || mSnapToColumnInLandscape);
    }

    private boolean snapToColumnWhenDragging() {
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        return mSnapToColumnWhenDragging && (isPortrait || mSnapToColumnInLandscape);
    }

    public boolean isDragging() {
        return mCurrentRecyclerView != null &&
                (mCurrentRecyclerView.isDragging() || mCurrentRecyclerView.isDragStart());
    }
//    public boolean isDragStart() {
//        return mCurrentRecyclerView != null && mCurrentRecyclerView.isDragStart();
//    }

    public boolean isMoveing() {
        return isMoveing;
    }

    public DragItemRecyclerView getRecyclerView(int column) {
        if (column >= 0 && column < mLists.size()) {
            return mLists.get(column);
        }
        return null;
    }

    public DragItemAdapter getAdapter(int column) {
        if (column >= 0 && column < mLists.size()) {
            return (DragItemAdapter) mLists.get(column).getAdapter();
        }
        return null;
    }

    public int getItemCount() {
        int count = 0;
        for (DragItemRecyclerView list : mLists) {
            count += list.getAdapter().getItemCount();
        }
        return count;
    }

    public int getItemCount(int column) {
        if (mLists.size() > column) {
            return mLists.get(column).getAdapter().getItemCount();
        }
        return 0;
    }

    public int getColumnCount() {
        return mLists.size();
    }

    public View getHeaderView(int column) {
        return mHeaders.get(column);
    }

    public void removeItem(int column, int row) {
        if (!isDragging() && mLists.size() > column && mLists.get(column).getAdapter().getItemCount() > row) {
            DragItemAdapter adapter = (DragItemAdapter) mLists.get(column).getAdapter();
            adapter.removeItem(row);
        }
    }

    public void addItem(int column, int row, Object item, boolean scrollToItem) {
        if (!isDragging() && mLists.size() > column && mLists.get(column).getAdapter().getItemCount() >= row) {
            DragItemAdapter adapter = (DragItemAdapter) mLists.get(column).getAdapter();
            adapter.addItem(row, item);
            if (scrollToItem) {
                scrollToItem(column, row, false);
            }
        }
    }

    public void moveItem(int fromColumn, int fromRow, int toColumn, int toRow, boolean scrollToItem) {
        if (!isDragging() && mLists.size() > fromColumn && mLists.get(fromColumn).getAdapter().getItemCount() > fromRow
                && mLists.size() > toColumn && mLists.get(toColumn).getAdapter().getItemCount() >= toRow) {
            DragItemAdapter adapter = (DragItemAdapter) mLists.get(fromColumn).getAdapter();
            Object item = adapter.removeItem(fromRow);
            adapter = (DragItemAdapter) mLists.get(toColumn).getAdapter();
            adapter.addItem(toRow, item);
            if (scrollToItem) {
                scrollToItem(toColumn, toRow, false);
            }
        }
    }

    public void moveItem(long itemId, int toColumn, int toRow, boolean scrollToItem) {
        for (int i = 0; i < mLists.size(); i++) {
            RecyclerView.Adapter adapter = mLists.get(i).getAdapter();
            final int count = adapter.getItemCount();
            for (int j = 0; j < count; j++) {
                long id = adapter.getItemId(j);
                if (id == itemId) {
                    moveItem(i, j, toColumn, toRow, scrollToItem);
                    return;
                }
            }
        }
    }

    public void replaceItem(int column, int row, Object item, boolean scrollToItem) {
        if (!isDragging() && mLists.size() > column && mLists.get(column).getAdapter().getItemCount() > row) {
            DragItemAdapter adapter = (DragItemAdapter) mLists.get(column).getAdapter();
            adapter.removeItem(row);
            adapter.addItem(row, item);
            if (scrollToItem) {
                scrollToItem(column, row, false);
            }
        }
    }

    public void scrollToItem(int column, int row, boolean animate) {
        if (!isDragging() && mLists.size() > column && mLists.get(column).getAdapter().getItemCount() > row) {
            mScroller.forceFinished(true);
            scrollToColumn(column, animate);
            if (animate) {
                mLists.get(column).smoothScrollToPosition(row);
            } else {
                mLists.get(column).scrollToPosition(row);
            }
        }
    }

    public void scrollToColumn(int column, boolean animate) {
        if (mLists.size() <= column) {
            return;
        }

        DragItemRecyclerView recyclerView = mLists.get(column);
        View parent = (View) recyclerView.getParent();
        int newY = 0;
        switch (mSnapPosition) {
            case TOP:
                newY = containerOffsetY; //parent.getTop() +  + parent.getTop()
                break;
            case CENTER:
                newY = parent.getTop() - (getMeasuredHeight() - parent.getMeasuredHeight()) / 2;
                break;
            case BOTTOM:
                newY = parent.getBottom() - getMeasuredHeight();
                break;
            default:
        }

        int maxScroll = mRootLayout.getMeasuredHeight() - getMeasuredHeight();
        newY = newY < 0 ? 0 : newY;
        newY = newY > maxScroll ? maxScroll : newY;
        if (getScrollY() != newY) {
            mScroller.forceFinished(true);
//            LogTools.trackI(3, TAG, "scrollToColumn: " + snapToColumnWhenScrolling());
            if (animate) {
                mScroller.startScroll(getScrollX(), getScrollY(), 0, newY - getScrollY(), SCROLL_ANIMATION_DURATION);
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                scrollTo(getScrollX(), newY);
            }
        }
        mCurrentColumn = column;
    }

    public void clearBoard() {
        int count = mLists.size();
        for (int i = count - 1; i >= 0; i--) {
            mColumnLayout.removeViewAt(i);
            mHeaders.remove(i);
            mLists.remove(i);
        }
    }

    public void removeColumn(int column) {
        if (column >= 0 && mLists.size() > column) {
            mColumnLayout.removeViewAt(column);
            mHeaders.remove(column);
            mLists.remove(column);
        }
    }

    public boolean isDragEnabled() {
        return mDragEnabled;
    }

    public void setDragEnabled(boolean enabled) {
        mDragEnabled = enabled;
        if (mLists.size() > 0) {
            for (DragItemRecyclerView list : mLists) {
                list.setDragEnabled(mDragEnabled);
            }
        }
    }

    /**
     * @param width the width of columns in both portrait and landscape. This must be called before {@link #setSailLayout(int, boolean, DragItemAdapter, SailLayout.ContactInputCallback)} (int, DragItemAdapter, SailLayout.ContactInputCallback)} (int, DragItemAdapter)} is
     *              called for the width to take effect.
     */
    public void setColumnHeight(int width) {
        mColumnHeight = width;
    }

    /**
     * @param snapToColumn true if scrolling should snap to columns. Only applies to portrait mode.
     */
    public void setSnapToColumnsWhenScrolling(boolean snapToColumn) {
        mSnapToColumnWhenScrolling = snapToColumn;
    }

    /**
     * @param snapToColumn true if dragging should snap to columns when dragging towards the edge. Only applies to portrait mode.
     */
    public void setSnapToColumnWhenDragging(boolean snapToColumn) {
        mSnapToColumnWhenDragging = snapToColumn;
        mAutoScroller.setAutoScrollMode(snapToColumnWhenDragging() ? AutoScroller.AutoScrollMode.COLUMN : AutoScroller.AutoScrollMode
                .POSITION);
    }

    /**
     * @param snapToColumnInLandscape true if dragging should snap to columns when dragging towards the edge also in landscape mode.
     */
    public void setSnapToColumnInLandscape(boolean snapToColumnInLandscape) {
        mSnapToColumnInLandscape = snapToColumnInLandscape;
        mAutoScroller.setAutoScrollMode(snapToColumnWhenDragging() ? AutoScroller.AutoScrollMode.COLUMN : AutoScroller.AutoScrollMode
                .POSITION);
    }

    /**
     * @param snapPosition determines what position a column will snap to. LEFT, CENTER or RIGHT.
     */
    public void setColumnSnapPosition(ColumnSnapPosition snapPosition) {
        mSnapPosition = snapPosition;
    }

    /**
     * @param snapToTouch true if the drag item should snap to touch position when a drag is started.
     */
    public void setSnapDragItemToTouch(boolean snapToTouch) {
        mDragItem.setSnapToTouch(snapToTouch);
    }

    public void setBoardListener(BoardListener listener) {
        mBoardListener = listener;
    }

    public void setBoardCallback(BoardCallback callback) {
        mBoardCallback = callback;
    }

    public void setCustomDragItem(DragItem dragItem) {
        DragItem newDragItem;
        if (dragItem != null) {
            newDragItem = dragItem;
        } else {
            newDragItem = new DragItem(getContext());
        }
        newDragItem.setSnapToTouch(mDragItem.isSnapToTouch());
        mDragItem = newDragItem;
        mRootLayout.addView(mDragItem.getDragItemView());
    }

    public SailLayout setSailLayout(int sailLayoutId, boolean isNeedSetSail, DragItemAdapter adapter, SailLayout.ContactInputCallback inputCallback) {
        if (mColumnLayout != null) {
            SailLayout sailLayout = (SailLayout) mColumnLayout.findViewById(sailLayoutId);
            if (sailLayout != null && sailLayout.mCanvasView != null) {
                final DragItemRecyclerView recyclerView = sailLayout.mCanvasView;
                sailLayout.setInputCallback(inputCallback);
                if (!mLists.contains(recyclerView)) {
                    mLists.add(recyclerView);
                    recyclerView.setDragItem(mDragItem);
                    sailLayout.setSailListener(new SailLayout.SailListener() {
                        @Override
                        public void setSail(boolean isSailOn) {
                            BoardView.this.setSail(isSailOn);
                        }

                        @Override
                        public void onSelect(SailLayout sailLayout) {
                            BoardView.this.setSailWithOut(false, sailLayout, false);
                        }
                    });
                    sailLayout.initListener(new DragItemRecyclerView.DragItemListener() {
                        @Override
                        public void onDragStarted(View itemView, int itemPosition, float x, float y) {
                            mDragStartColumn = getColumnOfList(recyclerView);
                            mDragStartRow = itemPosition;
                            mCurrentRecyclerView = recyclerView;
                            setDragOffset(mCurrentRecyclerView);
                            if (mBoardListener != null) {
                                mBoardListener.onItemDragStarted(mDragStartColumn, mDragStartRow);
                            }
                            invalidate();
                        }

                        @Override
                        public void onDragging(int itemPosition, float x, float y) {
                            int column = getColumnOfList(recyclerView);
                            boolean positionChanged = column != mLastDragColumn || itemPosition != mLastDragRow;
                            if (mBoardListener != null && positionChanged) {
                                mLastDragColumn = column;
                                mLastDragRow = itemPosition;
                                mBoardListener.onItemChangedPosition(mDragStartColumn, mDragStartRow, column, itemPosition);
                            }
                        }

                        @Override
                        public void onDragEnded(int newItemPosition) {
                            mLastDragColumn = NO_POSITION;
                            mLastDragRow = NO_POSITION;
                            if (mBoardListener != null) {
                                final int columnOfList = getColumnOfList(recyclerView);
                                mBoardListener.onItemDragEnded(mDragStartColumn, mDragStartRow, columnOfList, newItemPosition);
                            }

                        }
                    }, new DragItemRecyclerView.DragItemCallback() {
                        @Override
                        public boolean canDragItemAtPosition(int dragPosition) {
                            int column = getColumnOfList(recyclerView);
                            return mBoardCallback == null || mBoardCallback.canDragItemAtPosition(column, dragPosition);
                        }

                        @Override
                        public boolean canDropItemAtPosition(int dropPosition) {
                            int column = getColumnOfList(recyclerView);
                            return mBoardCallback == null || mBoardCallback.canDropItemAtPosition(mDragStartColumn, mDragStartRow, column, dropPosition);
                        }
                    });
                }
                adapter.setDragView(recyclerView);
                adapter.setDragStartedListener(new DragItemAdapter.DragStartCallback() {
                    @Override
                    public boolean startDrag(View itemView, long itemId) {
                        return recyclerView.startDrag(itemView, itemId, getListTouchX(recyclerView), getListTouchY(recyclerView));
                    }

                    @Override
                    public boolean isDragging() {
                        return recyclerView.isDragging();
                    }
                });
                sailLayout.attachDragRecyclerView(adapter, true);
            }
            return sailLayout;
        }
        return null;
    }

    private void setDragOffset(View mCurrentRecyclerView) {
        View parentView = ((View) mCurrentRecyclerView.getParent());
        final float offsetX = parentView.getLeft() + mCurrentRecyclerView.getLeft();
        final float offsetY = parentView.getTop() + mCurrentRecyclerView.getTop() + containerOffsetY;
        mDragItem.setOffset(offsetX, offsetY);
    }

    public void setSail(final boolean isEditable) {
        setSailWithOut(isEditable, null, true);
    }

    public void setSailTrueOtherFalse(int column) {
        DragItemRecyclerView recyclerView = getRecyclerView(column);
        if (recyclerView != null) {
            SailLayout sailLayout = (SailLayout) recyclerView.getParent();
            sailLayout.setSail(true);
            if (!isDragging()) {
                int childCount = mColumnLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = mColumnLayout.getChildAt(i);
                    if (childView instanceof SailLayout) {
                        if (childView == sailLayout) {
                            continue;
                        }
                        ((SailLayout) childView).setSail(false);
//                    LogTools.i(TAG, "setSail: " + isEditable + " : " + i + "/" + childCount);
                    }
                }
                scrollTo(0, 0);
            }
        }
    }

    public void setSailWithOut(final boolean isEditable, SailLayout layout, boolean scrollToTop) {
//        Log.d(TAG, "setSailWithOut: " + isEditable + " : " + scrollToTop + " ,Draging: " + isDragging());
        setSailWithOut(isEditable, layout, scrollToTop ? 0 : -1);
    }

    public void setSailWithOut(final boolean isEditable, SailLayout layout, int offsetY) {
//        Log.d(TAG, "setSailWithOut: " + isEditable + " : " + offsetY + " ,Draging: " + isDragging());
        if (isDragging()) {
            return;
        }
        int childCount = mColumnLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = mColumnLayout.getChildAt(i);
            if (childView instanceof SailLayout) {
                if (childView == layout) {
                    continue;
                }
                ((SailLayout) childView).setSail(isEditable);
//                    LogTools.i(TAG, "setSail: " + isEditable + " : " + i + "/" + childCount);
            }
        }
        if (offsetY != -1 && !isEditable) {
            scrollTo(0, offsetY);
        }
    }

    protected void onBoardViewScroll() {

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private float mStartScrollY;
        private int mStartColumn;

        @Override
        public boolean onDown(MotionEvent e) {
            mStartScrollY = getScrollY();
            mStartColumn = mCurrentColumn;
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            LogTools.i(TAG, "onScroll: " + isDragging() + " = " + distanceY + " ," + mStartColumn);
            if (Math.abs(distanceY) > mTouchSlop) {
                onBoardViewScroll();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX2, float velocityY) {
            if (mStartColumn == -1) {
                return false;
            }
            // Calc new column to scroll to
            int closestColumn = getClosestSnapColumn();
            int newColumn = closestColumn;

            // This can happen if you start to drag in one direction and then fling in the other direction.
            // We should then switch column in the fling direction.
            boolean wrongSnapDirection = newColumn > mStartColumn && velocityY > 0 || newColumn < mStartColumn && velocityY < 0;

            if (mStartScrollY == getScrollY()) {
                newColumn = mStartColumn;
            } else if (mStartColumn == closestColumn || wrongSnapDirection) {
                if (velocityY < 0) {
//                    newColumn = closestColumn + 1;
                } else {
                    newColumn = closestColumn - 1;
                }
            }
            if (newColumn < 0 || newColumn > mLists.size() - 1) {
                newColumn = newColumn < 0 ? 0 : mLists.size() - 1;
            }
            if (isDragging()) {
                // Calc new scrollY position
                scrollToColumn(newColumn, true);
                return true;
            } else {
                mScroller.fling(0, getScrollY(), 0, -(int) velocityY, 0, 0, 0, mMaxScrollEdge);
                return super.onFling(e1, e2, velocityX2, velocityY);
            }
        }
    }

    //
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMaxScrollEdge = mRootLayout.getMeasuredHeight();
    }

    int mMaxScrollEdge;
//

    @SuppressWarnings("WeakerAccess")
    static class SavedState extends View.BaseSavedState {
        public int currentColumn;

        private SavedState(Parcelable superState, int currentColumn) {
            super(superState);
            this.currentColumn = currentColumn;
        }

        public SavedState(Parcel source) {
            super(source);
            currentColumn = source.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentColumn);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


}