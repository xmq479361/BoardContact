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

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 拖拽RecyclerView 对应子条目适配器
 * @date 2018/2/2.
 * @author xmqiu
 */
public abstract class DragItemAdapter<T, VH extends DragItemAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected RecyclerView recyclerView;
    protected final String TAG = getClass().getSimpleName();

    public void setDragView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    DragItemAdapter<T, VH> copyAdapter;

    DragItemAdapter<T, VH> copyTo(int amount, List sourceData, int summarySize) {
        return copyAdapter = copy(amount, sourceData, summarySize);
    }

    public abstract DragItemAdapter<T, VH> copy(int amount, List sourceData, int summarySize);

    public abstract void addTextItem(int itemType, String itemText);

    public T getItem(int position) {
        return mItemList.get(position);
    }

    public int checkHasSameItem(T item, long itemId) {
        return -1;
    }

    /**
     * 判断最后一个item是否是输入框或者（等。。人）
     *
     * @return
     */
    public abstract boolean isLastItemInputOrAmount();

    public void removeLastItem() {
        int lastItemIndex = getItemCount() - 1;
        if (isLastItemInputOrAmount()) {
            lastItemIndex -= 1;
        }
        if (lastItemIndex >= 0 && lastItemIndex < getItemCount()) {
            removeItem(lastItemIndex);
        }
    }

    public interface DragStartCallback {
        boolean startDrag(View itemView, long itemId);

        boolean isDragging();
    }

    private DragStartCallback mDragStartCallback;
    private long mDragItemId = RecyclerView.NO_ID;
    private long mDropTargetId = RecyclerView.NO_ID;
    protected List<T> mItemList;

    /**
     * @return a unique id for an item at the specific position.
     */
    public abstract long getUniqueItemId(int position);

    public DragItemAdapter() {
        setHasStableIds(true);
    }

    public void setItemList(List<T> itemList) {
        mItemList = itemList;
        notifyDataSetChanged();
    }

    public List<T> getItemList() {
        return mItemList;
    }

    public int getPositionForItem(T item) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (mItemList.get(i) == item) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public Object removeItem(int pos) {
        if (mItemList != null && mItemList.size() > pos && pos >= 0) {
            Object item = mItemList.remove(pos);
            notifyItemRemoved(pos);
            return item;
        }
        return null;
    }

    public void addItem(int pos, T item) {
        if (mItemList == null) {
            mItemList = Collections.EMPTY_LIST;
        }
        int len = mItemList.size();
        if (len < pos) {
            return;
        }
        mItemList.add(pos, item);
        Log.d(TAG, "addItem: " + pos + " / " + len + " => " + mItemList.size());
        if (copyAdapter != null) {
            copyAdapter.addItem(pos, item);
//            copyAdapter.notifyDataSetChanged();
        }
        notifyItemInserted(pos);
    }

    public void addItems(int pos, Collection<T> items) {
        if (mItemList == null) {
            mItemList = Collections.EMPTY_LIST;
        }
        int len = mItemList.size();
        if (len < pos) {
            return;
        }
        Log.d(TAG, "addItems: " + pos + " , " + items.size());
        mItemList.addAll(pos, items);
        notifyItemRangeInserted(pos, len);
    }

    public void changeItemPosition(int fromPos, int toPos) {
        if (mItemList == null) {
            mItemList = Collections.EMPTY_LIST;
        }
        int len = mItemList.size();
        if (len <= fromPos || len <= toPos) {
            return;
        }
        Log.d(TAG, "changeItemPosition: " + fromPos + " , " + toPos);
        T item = mItemList.remove(fromPos);
        mItemList.add(toPos, item);
        notifyItemMoved(fromPos, toPos);
    }

    public void swapItems(int pos1, int pos2) {
        if (mItemList == null) {
            mItemList = Collections.EMPTY_LIST;
        }
        int size = mItemList.size();
        if (size <= pos1 || size <= pos2) {
            return;
        }
        Log.d(TAG, "swapItems: " + pos1 + " , " + pos2);
        Collections.swap(mItemList, pos1, pos2);
        notifyDataSetChanged();
    }

    public int getPositionForItemId(long id) {
        int count = getItemCount() - 1;
        for (int i = 0; i < count; i++) {
            if (id == getItemId(i)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public final long getItemId(int position) {
        return getUniqueItemId(position);
    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.onBindViewHolderPre(position);
        long itemId = getItemId(position);
        holder.mItemId = itemId;
        holder.itemView.setVisibility(mDragItemId == itemId ? View.INVISIBLE : View.VISIBLE);
        holder.setDragStartCallback(mDragStartCallback);
    }

    @Override
    public void onViewRecycled(VH holder) {
        holder.onViewRecycled();
        super.onViewRecycled(holder);
        holder.setDragStartCallback(null);
    }

    void setDragStartedListener(DragStartCallback dragStartedListener) {
        mDragStartCallback = dragStartedListener;
    }

    protected void setDragItemId(long dragItemId) {
        mDragItemId = dragItemId;
    }

    void setDropTargetId(long dropTargetId) {
        mDropTargetId = dropTargetId;
    }

    public long getDropTargetId() {
        return mDropTargetId;
    }

    public static abstract class ViewHolder extends RecyclerView.ViewHolder {
        public View mGrabView;
        public long mItemId;

        private DragStartCallback mDragStartCallback;

        public ViewHolder(final View itemView, int handleResId, boolean dragOnLongPress) {
            super(itemView);
            if (handleResId == -1) {
                return;
            }
            mGrabView = itemView.findViewById(handleResId);

            if (dragOnLongPress) {
                mGrabView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (mDragStartCallback == null) {
                            return false;
                        }
                        if (mDragStartCallback.startDrag(itemView, mItemId)) {
                            return true;
                        }
                        if (itemView == mGrabView) {
                            return onItemLongClicked(view);
                        }
                        return false;
                    }
                });
            } else {
                mGrabView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (mDragStartCallback == null) {
                            return false;
                        }

                        if (event.getAction() == MotionEvent.ACTION_DOWN && mDragStartCallback.startDrag(itemView, mItemId)) {
                            return true;
                        }

                        if (!mDragStartCallback.isDragging() && itemView == mGrabView) {
                            return onItemTouch(view, event);
                        }
                        return false;
                    }
                });
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClicked(view);
                }
            });

            if (itemView != mGrabView) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return onItemLongClicked(view);
                    }
                });
                itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        return onItemTouch(view, event);
                    }
                });
            }
        }

        public void setDragStartCallback(DragStartCallback dragStartedListener) {
            mDragStartCallback = dragStartedListener;
        }

        public void onItemClicked(View view) {
        }

        public boolean onItemLongClicked(View view) {
            return false;
        }

        public boolean onItemTouch(View view, MotionEvent event) {
            return false;
        }

        public void onBindViewHolderPre(int position) {
        }

        public void onViewRecycled() {
        }
    }
}
