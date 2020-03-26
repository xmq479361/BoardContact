package com.xmqiu.ui.mailcreate;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.xmqiu.R;
import com.xmqiu.data.vo.Contact;
import com.xmqiu.ui.AppActivity;
import com.xmqiu.utils.view.ViewHelper;

public class MailCreateActivity extends AppActivity implements IMailCreateView{
    private MailCreateHeaderView mailContactView;
    /**
     * 正文编辑区域
     */
    private EditText mail_create_edit_content;
    /**
     * 标题编辑区域
     */
    private EditText mail_create_mailinfo_title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_create);
        initViews();
        initViewWithParams();
    }

    private void initViews() {
        mailContactView = ViewHelper.findById(this, R.id.creat_new_mail_header_container);
        mail_create_edit_content = ViewHelper.findById(this, R.id.id_mail_create_edit_content);
        mail_create_mailinfo_title = ViewHelper.findById(this, R.id.id_mail_create_mailinfo_title);
        mail_create_edit_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    postDelayed(new Runnable() {
                        public void run() {
                            mailContactView.setSailWithOut(false, null,
                                    mailContactView.getTop());
                        }
                    }, 100);
                }
            }
        });
        mail_create_mailinfo_title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mailContactView.setSailWithOut(false, null,
                                    mailContactView.getTop());
                        }
                    }, 100);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mailContactView != null) {
            mailContactView.setOnTouchListener(null);
            mailContactView.onDestory();
        }
    }

    private void dismissPopwindow() {
        mailContactView.dismissPopwindow();
    }

    public void initViewWithParams() {
        mailContactView.registerSubscriber(this);
        /**  根据数据初始化发件人等联系人布局 */
        mailContactView.getContactFromBundle(getIntent().getExtras());
    }

    @Override
    protected boolean queueIdle() {
//        if(!mailContactView.isContactEmpty()){
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mailContactView.focuseOnContact(RECV_EDIT_LOGO);
                }
            }, 60);
//        } else {
//            mail_create_mailinfo_title.requestFocus();
//        }
        return super.queueIdle();
    }

    private Context getContext() {
        return this;
    }

    @Override
    public void showMoreWhenClickHeadView(Contact item, View itemView, int contactEditLogo) {

    }
}
