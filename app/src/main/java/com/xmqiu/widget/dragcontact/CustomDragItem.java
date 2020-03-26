package com.xmqiu.widget.dragcontact;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.xmqiu.R;
import com.xmqiu.utils.view.ViewHelper;
import com.xmqiu.widget.dgv.DragItem;

public class CustomDragItem extends DragItem {
    final String TAG = "DragItem";

    public CustomDragItem(Context context, int layoutId) {
        super(context, layoutId);
    }

    @Override
    public void onBindDragView(View clickedView, View dragView) {
        TextView contactText = ViewHelper.findById(clickedView, R.id.tv_contact_text);
        TextView contactTextInDrag = ViewHelper.findById(dragView, R.id.tv_contact_text);
        if(contactTextInDrag!=null && contactText!=null) {
            CharSequence text = contactText.getText();
            contactTextInDrag.setSelected(true);
            contactTextInDrag.setText(text);
            contactTextInDrag.setBackgroundResource(R.drawable.bg_shape_contact_drag);
        }
    }

    @Override
    protected void startDrag(View startFromView, float touchX, float touchY) {
        super.startDrag(startFromView, touchX, touchY);
        TextView contactText = ViewHelper.findById(startFromView, R.id.tv_contact_text);
        if(contactText!=null) {
            contactText.setSelected(true);
        }
    }

    @Override
    protected  void endDrag(View endToView, AnimatorListenerAdapter listener) {
        super.endDrag(endToView, listener);
        TextView contactText = ViewHelper.findById(endToView, R.id.tv_contact_text);
        if(contactText!=null) {
            contactText.setSelected(false);
        }
    }
}