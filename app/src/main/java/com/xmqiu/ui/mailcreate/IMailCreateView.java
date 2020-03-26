package com.xmqiu.ui.mailcreate;

import android.view.View;

import com.xmqiu.data.vo.Contact;

public interface IMailCreateView {
    /**
     * 收件人的标识
     */
    int RECV_EDIT_LOGO = 0x001;
    /**
     * 抄送人的标识
     */
    int COPY_EDIT_LOGO = 0x002;
    /**
     * 密送人的标识
     */
    int SECRET_EDIT_LOGO = 0x003;

    String EXTRA_TAG_RECV = "recv";
    String EXTRA_TAG_COPY = "copy";
    String EXTRA_TAG_SECRET = "secret";


    void showMoreWhenClickHeadView(Contact item, View itemView, int contactEditLogo);


}
