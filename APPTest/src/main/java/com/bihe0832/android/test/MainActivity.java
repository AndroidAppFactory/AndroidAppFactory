package com.bihe0832.android.test;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_icon);

		findViewById(R.id.changeLuncher).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				changeLuncher();
			}
		});

	}


	private void changeLuncher() {
		String name = "com.vension.app.changeappicon.newsLuncherActivity";
		PackageManager pm = getPackageManager();
		pm.setComponentEnabledSetting(getComponentName(),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(new ComponentName(MainActivity.this, name),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

		//Intent 重启 Launcher 应用
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);
		for (ResolveInfo res : resolves) {
			if (res.activityInfo != null) {
				ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				am.killBackgroundProcesses(res.activityInfo.packageName);
			}
		}
		Toast.makeText(this,"桌面图标已更换",Toast.LENGTH_LONG).show();
	}


}
