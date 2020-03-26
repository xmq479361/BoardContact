//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xmqiu.utils.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.xmqiu.widget.dgv.BoardView;

/**
 * @date 2018/2/2.
 * @author xmqiu
 */
public class ViewHelper {
    private static final String TAG = ViewHelper.class.getSimpleName();

    public ViewHelper() {
    }

    public static <T extends View> T findById(View view, int id) {
        return (T) view.findViewById(id);
    }

    public static <T extends View> T findById(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    public static TextView toTextView(View textView) {
        return (TextView)textView;
    }

    public static ListView toListView(View listView) {
        return (ListView)listView;
    }

    public static Button toButton(View button) {
        return (Button)button;
    }

    public static ImageButton toImageButton(View button) {
        return (ImageButton)button;
    }

    public static AutoCompleteTextView toAutoCompleteTextView(View autoCompleteTextView) {
        return (AutoCompleteTextView)autoCompleteTextView;
    }

    public static ExpandableListView toExpandableListView(View expandableListView) {
        return (ExpandableListView)expandableListView;
    }

    public static EditText toEditText(View editText) {
        return (EditText)editText;
    }

    public static Spinner toSpinner(View spinner) {
        return (Spinner)spinner;
    }

    public static TableRow toTableRow(View tableRow) {
        return (TableRow)tableRow;
    }

    public static ImageView toImageView(View imageView) {
        return (ImageView)imageView;
    }

    public static ProgressBar toProgressBar(View progressBar) {
        return (ProgressBar)progressBar;
    }

    public static LinearLayout toLinearLayout(View linearLayout) {
        return (LinearLayout)linearLayout;
    }

    public static TextView findTextView(View parent, int textViewId) {
        return (TextView)parent.findViewById(textViewId);
    }

    public static WebView findWebView(View parent, int textViewId) {
        return (WebView)parent.findViewById(textViewId);
    }

    public static ListView findListView(View parent, int listViewId) {
        return (ListView)parent.findViewById(listViewId);
    }

    public static Button findButton(View parent, int buttonId) {
        return (Button)parent.findViewById(buttonId);
    }

    public static ImageButton findImageButton(View parent, int buttonId) {
        return (ImageButton)parent.findViewById(buttonId);
    }

    public static AutoCompleteTextView findAutoCompleteTextView(View parent, int autoCompleteTextViewId) {
        return (AutoCompleteTextView)parent.findViewById(autoCompleteTextViewId);
    }

    public static ExpandableListView findExpandableListView(View parent, int expandableListViewId) {
        return (ExpandableListView)parent.findViewById(expandableListViewId);
    }

    public static EditText findEditText(View parent, int editTextId) {
        return (EditText)parent.findViewById(editTextId);
    }

    public static Spinner findSpinner(View parent, int spinnerId) {
        return (Spinner)parent.findViewById(spinnerId);
    }

    public static TableRow findTableRow(View parent, int tableRowId) {
        return (TableRow)parent.findViewById(tableRowId);
    }

    public static GridView findGridView(View parent, int gridViewId) {
        return (GridView)parent.findViewById(gridViewId);
    }

    public static ImageView findImageView(View parent, int imageViewId) {
        return (ImageView)parent.findViewById(imageViewId);
    }

    public static ProgressBar findProgressBar(View parent, int progressBarId) {
        return (ProgressBar)parent.findViewById(progressBarId);
    }

    public static LinearLayout findLinearLayout(View parent, int linearLayoutId) {
        return (LinearLayout)parent.findViewById(linearLayoutId);
    }

    public static RelativeLayout findRelativeLayout(View parent, int relativeLayoutId) {
        return (RelativeLayout)parent.findViewById(relativeLayoutId);
    }

    public static View findView(View parent, int viewId) {
        return parent.findViewById(viewId);
    }


    public static void dealMaxDecimalLength(Editable editable, int maxDecimalLength) {
        String strEditable = editable.toString();
        int posDot = strEditable.indexOf(".");
        if (posDot > 0 && strEditable.length() - posDot - 1 > maxDecimalLength) {
            editable.delete(posDot + maxDecimalLength + 1, posDot + maxDecimalLength + 2);
        }

    }

    public static boolean notAllowZeroStart(EditText etView, Editable editable) {
        String strEditable = etView.getText().toString();
        if (strEditable.startsWith("0")) {
            if (strEditable.length() > 0) {
                deleteCharActCurrentSelection(etView, editable);
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean notAllowZeroZeroStart(EditText etView, Editable editable) {
        String strEditable = etView.getText().toString();
        if (strEditable.startsWith("00")) {
            if (strEditable.length() > 1) {
                deleteCharActCurrentSelection(etView, editable);
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean notAllowDotStart(EditText etView, Editable editable) {
        String strEditable = etView.getText().toString();
        if (strEditable.startsWith(".")) {
            if (strEditable.length() > 0) {
                deleteCharActCurrentSelection(etView, editable);
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean notAllowZeroDotStart(EditText etView, Editable editable) {
        String strEditable = etView.getText().toString();
        if (strEditable.startsWith("0.")) {
            deleteCharActCurrentSelection(etView, editable);
            return true;
        } else {
            return false;
        }
    }

    public static boolean onlyAllowZeroDotStartIfZeroStart(EditText etView, Editable editable) {
        String strEditable = etView.getText().toString();
        if (strEditable.startsWith("0") && strEditable.length() > 1 && !strEditable.startsWith("0.")) {
            deleteCharActCurrentSelection(etView, editable);
            return true;
        } else {
            return false;
        }
    }

    public static void deleteCharActCurrentSelection(EditText etView, Editable editable) {
        int index = etView.getSelectionStart();
        if (index > 0) {
            editable.delete(index - 1, index);
        }

    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5F);
    }

    public static void setLayoutAnimation(ViewGroup viewGroup, int animResId) {
        setLayoutAnimation(viewGroup, animResId, 0, 0.0F);
    }

    public static void setLayoutAnimation(ViewGroup viewGroup, int animResId, int order, float delay) {
        Animation animation = AnimationUtils.loadAnimation(viewGroup.getContext(), animResId);
        LayoutAnimationController lac = new LayoutAnimationController(animation);
        lac.setOrder(order);
        lac.setDelay(delay);
        viewGroup.setLayoutAnimation(lac);
    }

    /**
     * 获取view的信息(id 名称 ,tag , 文字)
     *
     * @param view
     * @return
     */
    public static StringBuilder viewsInfo(View view) {
        if(view==null){
            return new StringBuilder();
        }
        StringBuilder out = new StringBuilder(view.getClass().getSimpleName());
        out.append(idResName(view));
        Object tag = view.getTag();
        if (tag != null) {
            out.append(" ,tag = ").append(tag);
        }
        if (view instanceof TextView) {
            out.append(" ,value = ").append(((TextView) view).getText());
//            } else if (view instanceof ImageView) {
//                int resId = ReflectUtils.getField(view, "mResource");
//                info(out, r, resId);
        }
        return out;
    }
    public static String idResName(View view) {
        return idResName(view.getResources(), view.getId());
    }

    public static String idResName(Resources r, int id) {
        StringBuilder out = new StringBuilder();
        try {
            String pkgname;
            switch (id & 0xff000000) {
                case 0x7f000000:
                    pkgname = "app";
                    break;
                case 0x01000000:
                    pkgname = "android";
                    break;
                default:
                    pkgname = r.getResourcePackageName(id);
                    break;
            }
            String typename = r.getResourceTypeName(id);
            String entryname = r.getResourceEntryName(id);
            out.append(" ");
            out.append(pkgname);
            out.append(":");
            out.append(typename);
            out.append("/");
            out.append(entryname);
        } catch (Resources.NotFoundException e) {
            e.getMessage();
        }
        return out.toString();
    }
}
