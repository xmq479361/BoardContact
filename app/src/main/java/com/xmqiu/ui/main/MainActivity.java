package com.xmqiu.ui.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.xmqiu.R;
import com.xmqiu.data.vo.Contact;
import com.xmqiu.data.vo.ContactEntity;
import com.xmqiu.ui.mailcreate.MailCreateActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void dumpToMailCreate(View view) {
        startActivity(new Intent(this, MailCreateActivity.class));
    }

    public void dumpToMailCreateWithTempContact(View view) {
        Intent intent = new Intent(this, MailCreateActivity.class);
        ContactEntity contactEntity = createContactSampleData();
        intent.putExtras(contactEntity.attachContactToBundle(new Bundle()));
        startActivity(intent);
    }

    private ContactEntity createContactSampleData() {
        ContactEntity entity = new ContactEntity();
        entity.ciList = randomContacts();
        entity.copyciList = randomContacts();
        entity.secret_ciList = randomContacts();
        return entity;
    }

    private List<Contact> randomContacts() {
        List<Contact> contacts = new ArrayList<Contact>();
        for (int i = 0, len = new Random().nextInt(15); i < len; i++) {
            int index = i*(len+3) % NEAMS_ARRAY.length ;
            contacts.add(new Contact(String.format("10%06d", i), NEAMS_ARRAY[index]));
        }
        return contacts;
    }

    String[] NEAMS_ARRAY = {
            "路易斯",
            "马克尔",
            "史密斯",
            "收款方就",
            "史蒂芬森",
            "尼古拉斯赵四",
            "张三",
            "迈克尔",
            "隔壁老王",
            "管第三方科技",
            "水电费",
    };
}
