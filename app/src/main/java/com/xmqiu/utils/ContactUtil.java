package com.xmqiu.utils;

import android.text.TextUtils;

import com.xmqiu.data.vo.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author 作者 10165251
 * @version 创建时间：2015-6-11 下午7:05:36
 *          类说明
 */
public class ContactUtil {
    public static final int TYPE_AMOUNT = -3;
    public static final int TYPE_INPUT = -2;
    public static final int CONTACT_TYPE_NUM = 3;


    /**
     * 去重添加联系人
     *
     * @return 是否存在相同联系人，
     */
    public boolean checkAddContactInfoAndRemoveSame(List<Contact> cIList2, Contact ciTemp1) {
        return checkAddContactInfoAndRemoveSame(cIList2, ciTemp1, -1);
    }

    public boolean checkAddContactInfoAndRemoveSame(List<Contact> cIList2, Contact ciTemp1, int index) {
        if (cIList2 == null) {
            return false;
        }
        boolean hasSameContactInfo = checkHasSame(cIList2, ciTemp1);
        if (!hasSameContactInfo) {
            if (-1 == index) {
                index = cIList2.size() - 1;
                if (isLastItemAmountOrInput(cIList2)) {
                    index--;
                }
            }
            cIList2.add(Math.max(0, index), ciTemp1);
        }
        return hasSameContactInfo;
    }

    public boolean checkHasSame(List<Contact> cIList2, Contact ciTemp1) {
        return checkAndGetSameDataIndex(cIList2, ciTemp1) >= 0;
    }

    public int checkAndGetSameDataIndex(List<Contact> cIList2, Contact ciTemp1) {
        if (ciTemp1 != null && ciTemp1.getId() != null && !TextUtils.isEmpty(ciTemp1.getId())) {
            int size = cIList2.size();
            for (int i = 0; i < size; i++) {
                Contact t_zm_contactInfo = cIList2.get(i);
                if (!TextUtils.isEmpty(t_zm_contactInfo.getId()) && t_zm_contactInfo.getId().equals(ciTemp1.getId())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 去重添加联系人
     */
    public List<Contact> addContactInfoAndRemoveSame(List<Contact> cIList2,
                                                              Contact ciTemp1) {
        if (cIList2 == null)
            return new CopyOnWriteArrayList<>();
        boolean hasSameContactInfo = false;
        if (ciTemp1 != null && !TextUtils.isEmpty(ciTemp1.getId())) {
            int size = cIList2.size();
            for (int i = 0; i < size; i++) {
                Contact t_zm_contactInfo = cIList2.get(i);
                if (ciTemp1.getId().equals(t_zm_contactInfo.getId())) {
                    hasSameContactInfo = true;
                    break;
                }
            }
            if (!hasSameContactInfo) {
                int index = cIList2.size() - 1;
                if (isLastItemAmountOrInput(cIList2)) {
                    index--;
                }
//                LogTools.i("contact", "addContactInfoAndRemoveSame 去重添加联系人: " + Math.max(0, index) + " ," + ciTemp1.getDisplayNameInListWithLanuage());
                cIList2.add(Math.max(0, index), ciTemp1);
            }
        }
        return cIList2;
    }

    public List<Contact> addContactsInfoAndRemoveSame(List<Contact> cIList2,
                                                               List<Contact> cIList) {
        if (cIList2 == null)
            return new CopyOnWriteArrayList<>();
        if (cIList != null) {
            int size = cIList.size();
            for (int i = 0; i < size; i++) {
                cIList2 = addContactInfoAndRemoveSame(cIList2, cIList.get(i));
            }
        }
        return cIList2;

    }


    public static List<Contact> removeAmountAndInputItem(List<Contact> contacts) {
        if (contacts == null) {
            return new ArrayList<Contact>(0);
        }
        List<Contact> contactsCopy = new ArrayList<Contact>(contacts);
        synchronized (contactsCopy) {
            final int size = contacts.size();
            for (int i = size - 1; i >= 0; i--) {
                Contact contactInfo = contactsCopy.get(i);
                if (contactInfo == null)
                    continue;
                if (isItemAmountOrInput(contactInfo)) {
//                    LogTools.w("ContactUtil", "removeAmountAndInputItem 移除无效Item: " + i + " ," +
//                            contactInfo.getDisplayNameInListWithLanuage());
                    contactsCopy.remove(contactInfo);
                } else {
                    break;
                }
            }
        }
        return contactsCopy;
    }
    public static boolean isItemAmountOrInput(Contact contact) {
        if (String.valueOf(TYPE_INPUT).equals(contact.getId()) || String.valueOf(TYPE_AMOUNT).equals(contact.getId())) {
            return true;
        }
        return false;
    }

    public boolean isLastItemAmountOrInput(List<Contact> contacts) {
        int size = contacts.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                Contact contactInfo = contacts.get(i);
                if (isItemAmountOrInput(contactInfo)) {
//                    LogTools.trackW(3, "ContactUtil", "removeAmountAndInputItem 移除无效Item: " + i + " ," + contactInfo.getDisplayNameInListWithLanuage());
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    public static boolean isHasReciverContact(List<Contact> contacts) {
        final List<Contact> t_zm_contactInfos = removeAmountAndInputItem(contacts);
        if (t_zm_contactInfos != null && !t_zm_contactInfos.isEmpty()) {
            t_zm_contactInfos.clear();
            return true;
        }
        return false;
    }
}
 