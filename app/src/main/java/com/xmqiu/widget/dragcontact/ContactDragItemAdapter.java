/*
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xmqiu.widget.dragcontact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.xmqiu.R;
import com.xmqiu.data.vo.Contact;
import com.xmqiu.utils.ContactUtil;
import com.xmqiu.utils.view.ViewHelper;
import com.xmqiu.widget.dgv.CustomDragFlexboxLayoutManager;
import com.xmqiu.widget.dgv.DragItemAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.xmqiu.widget.dgv.BoardView.TYPE_AMOUNT;
import static com.xmqiu.widget.dgv.BoardView.TYPE_INPUT;

public class ContactDragItemAdapter extends DragItemAdapter<Contact, ContactDragItemAdapter.ViewHolder> {
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    boolean isSummaryAdapter = false;
    public EditText inputEdit;

    @Override
    public void addTextItem(int itemType, String itemText) {
        Contact itemPair = new Contact(itemType + "", itemText);
        getItemList().add(itemPair);
    }

    @Override
    public int checkHasSameItem(Contact item, long itemId) {
        int srcPos = new ContactUtil().checkAndGetSameDataIndex(mItemList, item);
        return srcPos;
    }

    @Override
    public boolean isLastItemInputOrAmount() {
        int itemCount = getItemCount();
        if (itemCount > 0) {
            Contact contactInfo = getItem(itemCount - 1);
            if (contactInfo != null && ContactUtil.isItemAmountOrInput(contactInfo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DragItemAdapter<Contact, ViewHolder> copy(int amount, List sourceData, int summarySize) {
        List summaryData = new ArrayList(summarySize);
        for (int i = 0; i < summarySize; i++) {
            summaryData.add(sourceData.get(i));
        }
        return new ContactDragItemAdapter(summaryData, true, this.mLayoutId, this.mGrabHandleId, mDragOnLongPress);
    }

    private ItemListener mItemListener;

    public interface ItemListener {
        void onItemClick(ViewHolder viewHolder, int position);

        void onItemLongClick(ViewHolder viewHolder, int position);
    }

    public void setItemListener(ItemListener listener) {
        this.mItemListener = listener;
    }


    public ContactDragItemAdapter(List<Contact> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        isSummaryAdapter = false;
        setItemList(list);
    }

    ContactDragItemAdapter(List<Contact> list, boolean isSummaryAdapter, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        this.isSummaryAdapter = isSummaryAdapter;
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_INPUT) {
            return new EditTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_drag_column, parent, false));
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Contact contactInfo = mItemList.get(position);
        int itemViewType = getItemViewType(position);
        String contactName = contactInfo.getName();
        if (holder instanceof TextViewHolder) {
            if (itemViewType != TYPE_AMOUNT) {
                contactName = contactInfo.getId()+" "+contactName;
            }
        }
        holder.mText.setText(contactName);
        holder.itemView.setTag(contactName);
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).getId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        String id = getItemList().get(position).getId();
        if (String.valueOf(TYPE_AMOUNT).equals(id)) {
            return TYPE_AMOUNT;
        } else if (String.valueOf(TYPE_INPUT).equals(id)) {
            return TYPE_INPUT;
        }
        return super.getItemViewType(position);
    }

    public abstract class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView mText;

        public ViewHolder(View itemView, int handleResId, boolean dragOnLongPress) {
            super(itemView, handleResId, dragOnLongPress);
        }

        public boolean isSummaryAdapter() {
            return isSummaryAdapter;
        }
    }

    /**
     * 各个联系人（拖拽）viewHolder
     */
    class TextViewHolder extends ViewHolder {

        TextViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = (TextView) itemView.findViewById(R.id.tv_contact_text);
        }

        @Override
        public void onItemClicked(View view) {
            if (mItemListener != null) {
                mItemListener.onItemClick(this, getAdapterPosition());
            }
        }

        @Override
        public boolean onItemLongClicked(View view) {
            if (mItemListener != null) {
                mItemListener.onItemLongClick(this, getAdapterPosition());
            }
            return true;
        }
    }

    /**
     * 联系人输入框
     */
    class EditTextViewHolder extends ViewHolder {

        EditTextViewHolder(final View itemView) {
            super(itemView, -1, false);
            inputEdit = ViewHelper.findById(itemView, R.id.id_board_drag_item_input);
            mText = inputEdit;
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            if (lp instanceof CustomDragFlexboxLayoutManager.LayoutParams) {
                ((CustomDragFlexboxLayoutManager.LayoutParams) lp).setFlexGrow(1.0f);
            }
        }
    }

}
