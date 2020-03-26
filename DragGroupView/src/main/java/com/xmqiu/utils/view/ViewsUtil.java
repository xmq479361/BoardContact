package com.xmqiu.utils.view;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

;
;import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 控件公共类
 *
 * @date 2018/2/2.
 * @author xmqiu
 */
public class ViewsUtil {

    /**
     * @param v
     * @return void
     * @Description描述: 显示软键盘
     */
    public static void showSoftInputMethod(final View v) {
        if (v != null) {
            if (!v.isFocused()) {
                v.setFocusable(true);
                v.requestFocus();
            }
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.showSoftInput(v, 0);
            }
        }
    }

    /**
     * @param v
     * @return void
     * @Description描述: 收起软键盘
     */
    public static void hideSoftInputMethod(final View v) {
        if (v != null && v.isFocused()) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            v.clearFocus();
        }
    }
    /**
     * @param a
     * @param b
     * @return boolean
     * @Description描述: 比较两个集合是否相等
     */
    public static <T> boolean equalsCollection(Collection<T> a, Collection<T> b) {
//        LogTools.i("Contact", "equalsCollection: a" +  "(" + (a != null ? a.size() : "Null") + ")"
//                + " : b" + "(" + (b != null ? b.size() : "Null") + ")");
        if (a == null && b == null) {
            return true;
        } else if (a == null) {
            return false;
        } else if (b == null) {
            return false;
        }

        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }

        if (a.size() != b.size()) {
            return false;
        }

        List<T> alist = new ArrayList<T>(a);
        List<T> blist = new ArrayList<T>(b);
        Collections.sort(alist, new Comparator<T>() {
            public int compare(T o1, T o2) {
                return o1.hashCode() - o2.hashCode();
            }

        });
        Collections.sort(blist, new Comparator<T>() {
            public int compare(T o1, T o2) {
                return o1.hashCode() - o2.hashCode();
            }
        });
        return alist.equals(blist);

    }
    /**
     * 获取 DisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    /**
     * 获取屏幕密度
     */
    public static float getScreenDensity(Context context) {
        return getDisplayMetrics(context).density;
    }

}
