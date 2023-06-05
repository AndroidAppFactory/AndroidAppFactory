package com.bihe0832.android.lib.theme.core;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground;
import com.bihe0832.android.lib.ui.custom.view.background.ViewWithBackground;

import java.util.ArrayList;
import java.util.List;


public class ThemeAttributeTrans {
    public static final List<String> list = new ArrayList<>();

    static {
        list.add("background");
        list.add("src");
        list.add("textColor");
        list.add("bgtv_backgroundColor");
        list.add("bgtv_strokeColor");
        list.add("tint");
    }

    private final ArrayList<ViewWithThemeAttribute> mViewWithThemeAttributes = new ArrayList<ViewWithThemeAttribute>();

    public void load(View view, AttributeSet attrs) {
        ArrayList<SkinAttrParms> skinAttrParms = new ArrayList<>();
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attributeName = attrs.getAttributeName(i);
            if (list.contains(attributeName)) {
                String attributeValue = attrs.getAttributeValue(i);
                if (attributeValue.startsWith("#")) {
                    continue;
                }
                int id;
                if (attributeValue.startsWith("?")) {
                    int attrid = Integer.parseInt(attributeValue.substring(1));
                    id = ThemeTools.getThemeResid(view.getContext(), new int[]{attrid})[0];
                } else {
                    id = Integer.parseInt(attributeValue.substring(1));
                }
                if (id != 0) {
                    SkinAttrParms attrParms = new SkinAttrParms(attributeName, id);
                    skinAttrParms.add(attrParms);
                }
            }
        }
        //将View与之对应的可以动态替换的属性集合 放入 集合中
        if (!skinAttrParms.isEmpty()) {
            ViewWithThemeAttribute viewWithThemeAttribute = new ViewWithThemeAttribute(view, skinAttrParms);
            viewWithThemeAttribute.applyTheme();
            mViewWithThemeAttributes.add(viewWithThemeAttribute);
        }
    }

    public void applyTheme() {
        for (ViewWithThemeAttribute skinView : mViewWithThemeAttributes) {
            skinView.applyTheme();
        }
    }

    public class SkinAttrParms {
        private String attrName;
        private int id;

        public SkinAttrParms(String attrName, int id) {
            this.attrName = attrName;
            this.id = id;
        }

        public String getAttrName() {
            return attrName;
        }

        public void setAttrName(String attrName) {
            this.attrName = attrName;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    class ViewWithThemeAttribute {
        View view;
        List<SkinAttrParms> parms;

        public ViewWithThemeAttribute(View view, List<SkinAttrParms> parms) {
            this.view = view;
            this.parms = parms;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public void applyTheme() {
            for (SkinAttrParms parms : parms) {
                Drawable left = null, top = null, right = null, bottom = null;
                switch (parms.attrName) {
                    case "background":
                        Object background = ThemeResourcesManager.INSTANCE.getBackground(parms.id);
                        //Color
                        if (background instanceof Integer) {
                            view.setBackgroundColor((Integer) background);
                        } else {
                            ViewCompat.setBackground(view, (Drawable) background);
                        }
                        break;
                    case "src":
                        background = ThemeResourcesManager.INSTANCE.getBackground(parms.id);
                        if (background instanceof Integer) {
                            ((ImageView) view).setImageDrawable(new ColorDrawable((Integer) background));
                        } else {
                            ((ImageView) view).setImageDrawable((Drawable) background);
                        }
                        break;
                    case "textColor":
                        ((TextView) view).setTextColor(ThemeResourcesManager.INSTANCE.getColorStateList(parms.id));
                        break;
                    case "drawableLeft":
                        left = ThemeResourcesManager.INSTANCE.getDrawable(parms.id);
                        break;
                    case "drawableTop":
                        top = ThemeResourcesManager.INSTANCE.getDrawable(parms.id);
                        break;
                    case "drawableRight":
                        right = ThemeResourcesManager.INSTANCE.getDrawable(parms.id);
                        break;
                    case "drawableBottom":
                        bottom = ThemeResourcesManager.INSTANCE.getDrawable(parms.id);
                        break;
                    case "tint":
                        ((ImageView) view).setColorFilter(ThemeResourcesManager.INSTANCE.getColor(parms.id));
                        break;
                    case "bgtv_backgroundColor":
                        if (view instanceof TextViewWithBackground) {
                            ((TextViewWithBackground) view).setBackgroundColor(ThemeResourcesManager.INSTANCE.getColor(parms.id));
                        } else {
                            ((ViewWithBackground) view).setBackgroundColor(ThemeResourcesManager.INSTANCE.getColor(parms.id));
                        }
                        break;
                    case "bgtv_strokeColor":
                        if (view instanceof TextViewWithBackground) {
                            ((TextViewWithBackground) view).setStrokeColor(ThemeResourcesManager.INSTANCE.getColor(parms.id));
                        } else {
                            ((ViewWithBackground) view).setStrokeColor(ThemeResourcesManager.INSTANCE.getColor(parms.id));
                        }
                        break;
                    default:
                        break;
                }
                if (null != left || null != right || null != top || null != bottom) {
                    ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
                }
            }
        }
    }
}
