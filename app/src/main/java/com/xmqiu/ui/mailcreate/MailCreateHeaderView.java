package com.xmqiu.ui.mailcreate;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.xmqiu.R;
import com.xmqiu.data.vo.Contact;
import com.xmqiu.data.vo.ContactEntity;
import com.xmqiu.utils.ContactUtil;
import com.xmqiu.utils.view.ViewHelper;
import com.xmqiu.utils.view.ViewsUtil;
import com.xmqiu.widget.dgv.BoardView;
import com.xmqiu.widget.dgv.DragItemRecyclerView;
import com.xmqiu.widget.dgv.SailLayout;
import com.xmqiu.widget.dragcontact.ContactDragItemAdapter;
import com.xmqiu.widget.dragcontact.CustomDragItem;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.xmqiu.ui.mailcreate.IMailCreateView.COPY_EDIT_LOGO;
import static com.xmqiu.ui.mailcreate.IMailCreateView.EXTRA_TAG_RECV;
import static com.xmqiu.ui.mailcreate.IMailCreateView.RECV_EDIT_LOGO;
import static com.xmqiu.ui.mailcreate.IMailCreateView.SECRET_EDIT_LOGO;

/**
 * @date 2018/2/9.
 * @author xmqiu
 */
public class MailCreateHeaderView extends BoardView {
    private SparseArray<EditText> contactInputs = new SparseArray(3);
    private SparseArray<ContactDragItemAdapter> contactAdapterPairs = new SparseArray(3);
    private com.xmqiu.ui.mailcreate.IMailCreateView createNewZMailView;

    public MailCreateHeaderView(Context context) {
        super(context);
    }

    public MailCreateHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MailCreateHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setCustomDragItem(new CustomDragItem(getContext(), R.layout.item_child_drag_group_dragging));
    }

    public void registerSubscriber(IMailCreateView createNewZMailView) {
        this.createNewZMailView = createNewZMailView;
    }

    public ContactDragItemAdapter buildAdapter(List<Contact> contacts, final int contactEditLogo, int labelRes) {
        final ContactDragItemAdapter adapter = new ContactDragItemAdapter(contacts, R.layout.item_child_drag_group_dragging,
                R.id.id_board_drag_item_layout, true);
        adapter.setItemListener(new ContactDragItemAdapter.ItemListener() {
            @Override
            public void onItemClick(ContactDragItemAdapter.ViewHolder viewHolder, int position) {
                if (viewHolder.isSummaryAdapter()) {
                    setSail(true);
                } else if (position >= 0 && position < adapter.getItemCount()) {
                    createNewZMailView.showMoreWhenClickHeadView(adapter.getItem(position), viewHolder.itemView, contactEditLogo);
                    getContactInput(contactEditLogo).requestFocus();
                }
            }

            @Override
            public void onItemLongClick(ContactDragItemAdapter.ViewHolder viewHolder, int position) {
                setSail(true);
            }
        });
        return adapter;
    }


    SailLayout.ContactInputCallback inputCallback = new SailLayout.ContactInputCallback() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (v.getText() != null && v.getText().toString().trim().length() != 0) {
                    addOtherMailAllAddress(v);
                }
                dismissPopwindow();
                return true;
            } else if (actionId == EditorInfo.IME_ACTION_NEXT) { // 点击下一项，且文本不为空，则判断当前文本内容是否完整
                dismissPopwindow();
                Log.d(TAG, "onEditorAction: " + ViewHelper.idResName(v));
                if (v.getText() != null && v.getText().toString().trim().length() != 0) {
                    addOtherMailAllAddress(v);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            /** 统一处理编辑框的Key事件 */
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && v instanceof EditText) {
                final EditText inputEdit = (EditText) v;
                if (TextUtils.isEmpty(inputEdit.getText())) {
//                    getHandler().removeCallbacks(checkToShowSearch);
                    dismissPopwindow();
                    getAdapter(getColumnOfContactInput(inputEdit)).removeLastItem();
                    postInputGetFocus(inputEdit);
                    return true;
                } else {
                    dismissPopwindow();
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public boolean onFocusChanged(DragItemRecyclerView recyclerView, View view, boolean isFocused) {
            if (!isDragging() && !isMoveing()) {
                removeCallbacks(checkToSetSailRunnable);
                postDelayed(checkToSetSailRunnable, 240);
            }
            return false;
        }

        @Override
        public void onTextChanged(DragItemRecyclerView recyclerView, EditText view, CharSequence afterS) {
            if (isDragging()) {
                return;
            }
            if (afterS != null && afterS.toString().trim().length() > 0) {
                editViewAfterTextChanged(getContactInput(view), view.isFocused(), (View) recyclerView.getParent(), view);
            } else {
                dismissPopwindow();
            }
        }

        @Override
        public void onFindInputEdit(SailLayout sailLayout, EditText inputView) {
            int sailLayoutId = sailLayout.getId();
            int contactLogo = getContactLogoById(sailLayoutId);
            if (contactLogo != -1) {
                contactInputs.put(contactLogo, inputView);
            }
        }

        private int getContactLogoById(int sailLayoutId) {
            if (sailLayoutId == R.id.id_sail_layout_recv) {
                return RECV_EDIT_LOGO;
            } else if (sailLayoutId == R.id.id_sail_layout_copy) {
                return COPY_EDIT_LOGO;
            } else if (sailLayoutId == R.id.id_sail_layout_secret_copy) {
                return SECRET_EDIT_LOGO;
            }
            return -1;
        }
    };

    void postInputGetFocus(final EditText inputEdit) {
        if (inputEdit == null) {
            return;
        }
        postDelayed(new Runnable() {
            public void run() {
                ViewsUtil.showSoftInputMethod(inputEdit);
            }
        }, 50);
    }

    private Runnable checkToSetSailRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isDragging() && !isMoveing()) {
                EditText inputFocused = getInputFocused();
                if (inputFocused == null) {
                    dismissPopwindow();
                    setSail(false);
                } else {
                    ViewsUtil.showSoftInputMethod(inputFocused);
                }
            }
        }
    };

    public void scrollToView(View view) {
        scrollToView(view, 2);
    }

    public void scrollToView(View view, int offset) {
        if (view == null) {
            return;
        }
        int topOffset = -offset;
        int viewParentTopLimit = getViewParentTopLimit(view, this, topOffset);
        if (Math.abs(viewParentTopLimit - getScrollY()) >= mTouchSlop) {
            scrollTo(0, viewParentTopLimit);
        }
    }

    @Override
    protected void onBoardViewScroll() {
        super.onBoardViewScroll();
        dismissPopwindow();
    }

    public EditText getContactInput(int contactEditLogo) {
        return contactInputs.get(contactEditLogo);
    }

    public int getContactInput(EditText inputEdit) {
        int index = contactInputs.indexOfValue(inputEdit);
        if (index < 0)
            return -1;
        return contactInputs.keyAt(index);
    }

    public int getColumnOfContactInput(EditText inputEdit) {
        int index = contactInputs.indexOfValue(inputEdit);
        if (index < 0) return -1;
        return index;
    }

    /**
     * 添加邮箱地址
     */
    private void addOtherMailAllAddress(TextView editText) {
        if (null != editText) {
            String inputText = editText.getText().toString();
            if (!TextUtils.isEmpty(inputText)) {
                addAllContact(inputText.replaceAll(",", ";").replaceAll(" ", "").split(";"), editText);
            }
            editText.getEditableText().clear();
        }
    }

    public SailLayout addContactsAuto(List<Contact> contacts, int contactEditLogo) {
        Log.i(TAG, "addContactsAuto: " + contactEditLogo + " ," + contacts.size());
        return addContactsAuto(contacts, contactEditLogo, true);
    }

    public SailLayout addContactsAuto(List<Contact> contacts, int contactEditLogo, boolean isNeedSetSail) {
        int labelRes = R.string.mail_create_lable_receiver;
        int sailId = R.id.id_sail_layout_recv;
        ContactDragItemAdapter adapter = contactAdapterPairs.get(contactEditLogo);
        switch (contactEditLogo) {
            case RECV_EDIT_LOGO: // 收件人
                sailId = R.id.id_sail_layout_recv;
                break;
            case COPY_EDIT_LOGO:// 抄送人
                labelRes = R.string.mail_create_lable_copy;
                sailId = R.id.id_sail_layout_copy;
                break;
            case SECRET_EDIT_LOGO: // 密送人
                labelRes = R.string.mail_create_lable_secrect;
                sailId = R.id.id_sail_layout_secret_copy;
                break;
        }
        Log.d(TAG, "addContactsAuto: " + contactEditLogo + " => " +
                getContext().getString(labelRes) + " ,contact: " + contacts.size() + " = " + adapter);
        if (adapter == null) {
            adapter = buildAdapter(contacts, contactEditLogo, labelRes);
            contactAdapterPairs.put(contactEditLogo, adapter);
        } else {
            adapter.setItemList(contacts);
        }
        SailLayout layout = setSailLayout(sailId, isNeedSetSail, adapter, inputCallback);
        layout.setSail(true);
        return layout;
    }


    /**********************************************************************************************/


    private void addContactAddress(int contactLogo, Contact contactInfo) {
        if (contactInfo != null) {
            addContact(contactLogo, contactInfo);
        }
        dismissPopwindow();
    }

    /**
     * 添加到通讯地址
     *
     * @param editText
     * @param contactInfo
     */
    private void addContactInfo(TextView editText, Contact contactInfo) {
        int contactLogo = getContactInput((EditText) editText);
        addContactAddress(contactLogo, contactInfo);
    }

    /**
     * 分割输入内容,创建邮箱地址
     *
     * @param address
     * @param editText
     */
    public void addAllContact(String address[], TextView editText) {
        if (address != null) {
            for (String add : address) {
                Contact contactInfo = new Contact(String.format("10%6d", new Random().nextInt(1000000)), add);
                addContactInfo(editText, contactInfo);
            }
        }
    }


    private List<Contact> getContactLists(int contactLogo) {
        switch (contactLogo) {
            case RECV_EDIT_LOGO: // 收件人
                return getContactEntity().ciList;
            case COPY_EDIT_LOGO:// 抄送人
                return getContactEntity().copyciList;
            case SECRET_EDIT_LOGO: // 密送人
                return getContactEntity().secret_ciList;
        }
        return null;
    }

    /**
     * 移除联系人
     *
     * @param contactLogo
     * @param contact
     */
    public void removeContact(int contactLogo, Contact contact) {
        ContactDragItemAdapter adapter = contactAdapterPairs.get(contactLogo);
        if (adapter != null) {
            int positionForItem = adapter.getPositionForItem(contact);
            if (positionForItem >= 0) {
                adapter.removeItem(positionForItem);
            }
        }
    }

    /**
     * 添加联系人
     *
     * @param contactLogo
     * @param contact
     */
    public void addContact(int contactLogo, Contact contact) {
        ContactDragItemAdapter adapter = contactAdapterPairs.get(contactLogo);
        if (adapter != null) {
            int itemCount = adapter.getItemCount();
            int insertIndex = Math.max(0, itemCount - 1);
            adapter.addItem(insertIndex, contact);
        }
    }

    /**
     * 添加联系人
     *
     * @param contactLogo
     * @param contacts
     */
    public void addContact(int contactLogo, List<Contact> contacts) {
        ContactDragItemAdapter adapter = contactAdapterPairs.get(contactLogo);
        if (adapter != null && contacts != null) {
            int itemCount = adapter.getItemCount();
            int insertIndex = Math.max(0, itemCount - 1);
            adapter.addItems(insertIndex, contacts);
        }
    }

    private void clearContactsText() {
        int size = contactInputs.size();
        if (size != ContactUtil.CONTACT_TYPE_NUM) {
            return;
        }
        for (int i = 0; i < size; i++) {
            contactInputs.valueAt(i).getEditableText().clear();
        }
    }

    public void removeInputListener() {
        int size = contactInputs.size();
        if (size != ContactUtil.CONTACT_TYPE_NUM) {
            return;
        }
        for (int i = 0; i < size; i++) {
            EditText inputView = contactInputs.valueAt(i);
            if (inputView != null) { /** 收件人输入框事件 */
                /** 收件人输入框 输入监听 */
//                    inputView.addTextChangedListener(null);
                inputView.setOnEditorActionListener(null);
                /** 退格键删除收件人 */
                inputView.setOnKeyListener(null);
                /** 收件人输入框失去焦点时候 人员选择状态改变 */
                inputView.setOnFocusChangeListener(null);
                inputView.setEnabled(false);
                inputView.setFocusable(false);
            }
        }
    }

    private EditText getInputFocused() {
        int size = contactInputs.size();
        EditText inputView = null;
        if (size == 3) {
            for (int i = 0; i < size; i++) {
                EditText inputView2 = contactInputs.valueAt(i);
                if (inputView2 != null && inputView2.isFocused()) {
                    inputView = inputView2;
                    break;
                }
            }
        }
        return inputView;
    }

    private void changeContactsStatus(int logo, boolean flag) {
        EditText inputFocused = getInputFocused();
        if (inputFocused != null) {
//            if (flag) {
//                inputFocused.setTextColor(ContextCompat.getColor(getContext(), R.color.));
//            } else {
//                inputFocused.setTextColor(Color.parseColor("#666666"));
//            }
//            scrollToInput(inputFocused);
        }
    }

    /**
     * 统一处理编辑框的输入监听
     *
     * @param logo
     * @param flag
     * @param v
     * @param edt
     */
    private void editViewAfterTextChanged(final int logo, boolean flag, final View v, final EditText edt) {
        changeContactsStatus(logo, flag);
        scrollToView((View) edt.getParent(), edt.getMeasuredHeight() * 3);
    }

    int focuseOnContactIndex = -1;

    public void focuseOnContact(final int contactLogo) {
        postDelayed(new Runnable() {
            public void run() {
                focuseOnContactIndex = contactLogo;
                setSailTrueOtherFalse(0);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EditText contactInput = getContactInput(contactLogo);
                        Log.w(TAG, "Input focuseOnContact: " + contactLogo + " = " + contactInput);
                        ViewsUtil.showSoftInputMethod(contactInput);
                    }
                }, 300);
            }
        }, 50);
    }

    public boolean isContactEmpty() {
        return contactEntity.isEmpty();
    }
    ContactEntity contactEntity = new ContactEntity();

    public void addContactsAuto() {
        Log.i(TAG, "addContactsAuto: ");
        addContactsAuto(contactEntity.ciList, RECV_EDIT_LOGO);
        addContactsAuto(contactEntity.copyciList, COPY_EDIT_LOGO);
        addContactsAuto(contactEntity.secret_ciList, SECRET_EDIT_LOGO);
    }

    public ContactEntity getContactEntity() {
        return contactEntity;
    }


    public void getContactFromBundle(Bundle b) {
        if (b != null) {
            contactEntity = b.getParcelable(EXTRA_TAG_RECV);
        }
        addContactsAuto();
    }

    //    /**
//     * 去掉搜索结果
//     */
    public void dismissPopwindow() {
//        if (mSearchContactPopupView != null && mSearchContactPopupView.isShowing()) {
//            mSearchContactPopupView.dismissPop();
//        }
    }
//
//    /**
//     * 展示联系人搜索结果
//     */
//
//    private void showPopUp(View v, EditText edt, int logo) {
//        if (mSearchContactPopupView != null) {
//            mSearchContactPopupView.dismissPop();
//        }
//        mSearchContactPopupView = new SearchContactPopupView(v, edt, getContext(), this);
//    }

    public void onDestory() {
//        dismissPopwindow();
        removeInputListener();
        notifySailLayoutDestory(R.id.id_sail_layout_recv);
        notifySailLayoutDestory(R.id.id_sail_layout_copy);
        notifySailLayoutDestory(R.id.id_sail_layout_secret_copy);
    }

    void notifySailLayoutDestory(int sailId) {
        View viewById = findViewById(sailId);
        if (viewById != null && viewById instanceof SailLayout) {
            ((SailLayout) viewById).onDestory();
        }
    }

}