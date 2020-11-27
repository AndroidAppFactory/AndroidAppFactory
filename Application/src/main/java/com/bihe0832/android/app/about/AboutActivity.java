package com.bihe0832.android.app.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;
import com.bihe0832.android.app.R;
import com.bihe0832.android.app.update.UpdateManager;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.debug.ShowDebugClick;
import com.bihe0832.android.framework.ui.BaseActivity;
import com.bihe0832.android.lib.router.annotation.Module;
import java.util.Calendar;

@Module(AboutActivity.MODULE_NAME_BASE_ABOUT)
public class AboutActivity extends BaseActivity {
    public static final String MODULE_NAME_BASE_ABOUT = "babout";

    protected Class getAboutItemClass() {
        return AboutFragment.class;
    }

    protected AboutFragment getItemFragment() {
        return new AboutFragment();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);
        initToolbar(R.id.app_about_toolbar, "关于我们", true);
        initView();
        //仅检查更新，不做升级
        if (findFragment(getAboutItemClass()) == null) {
            loadRootFragment(R.id.about_fragment_content, getItemFragment());
        }

        UpdateManager.INSTANCE.checkUpdateAndShowDialog(this, false);
    }

    private void initView() {
        TextView mVersion = (TextView) findViewById(R.id.about_version_info);
        mVersion.setText("当前版本号：" + ZixieContext.INSTANCE.getVersionNameAndCode());

        ImageView mAppIconImg = (ImageView) findViewById(R.id.about_app_icon);
        mAppIconImg.setOnClickListener(new ShowDebugClick());

        TextView mRight = (TextView) findViewById(R.id.about_copyright);
        mRight.setText("Copyright 2019-" + Calendar.getInstance().get(Calendar.YEAR) + " ZIXIE.All Rights Reserved");
    }

}
