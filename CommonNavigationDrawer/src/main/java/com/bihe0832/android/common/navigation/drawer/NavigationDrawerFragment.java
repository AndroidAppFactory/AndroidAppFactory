package com.bihe0832.android.common.navigation.drawer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener;

import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public abstract class NavigationDrawerFragment extends BaseFragment {

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    protected DrawerLayout mDrawerLayout;
    protected View mFragmentContainerView;

    protected boolean mFromSavedInstanceState;
    protected boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    protected void parseBundle(@NotNull Bundle bundle, boolean isOnCreate) {
        super.parseBundle(bundle, isOnCreate);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(View fragmentContainerView, DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;


        mFragmentContainerView = fragmentContainerView;
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.com_bihe0832_drawer_shadow, GravityCompat.START);
        mDrawerLayout.setStatusBarBackgroundColor(Color.TRANSPARENT);

        mDrawerLayout.addDrawerListener(new DrawerListener() {

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                NavigationDrawerFragment.this.setUserVisibleHint(true);
                setChildUserVisibleHint(true);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                NavigationDrawerFragment.this.setUserVisibleHint(false);
                setChildUserVisibleHint(false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        setDrawerLeftEdgeSize();
    }

    public void openDrawer() {
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }
    }

    public void closeDrawer() {
        if (mDrawerLayout != null && mFragmentContainerView != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    public void enableDrawerGesture() {
        if (mDrawerLayout != null && mFragmentContainerView != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void disableDrawerGesture() {
        if (mDrawerLayout != null && mFragmentContainerView != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    /*设置可以全屏滑动*/
    private void setDrawerLeftEdgeSize() {
        try {
            Field viewDragHelper = mDrawerLayout.getClass().getDeclaredField("mLeftDragger");
            viewDragHelper.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) viewDragHelper.get(mDrawerLayout);
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            int screenWidth = DisplayUtil.getRealScreenSizeX(ZixieContext.INSTANCE.getApplicationContext());
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, screenWidth));

            Field leftCallbackField = mDrawerLayout.getClass().getDeclaredField("mLeftCallback");
            leftCallbackField.setAccessible(true);
            ViewDragHelper.Callback leftCallback = (ViewDragHelper.Callback) leftCallbackField.get(mDrawerLayout);
            Field peekRunnableField = leftCallback.getClass().getDeclaredField("mPeekRunnable");
            peekRunnableField.setAccessible(true);
            Runnable nullRb = new Runnable() {
                @Override
                public void run() {

                }
            };
            peekRunnableField.set(leftCallback, nullRb);
        } catch (Exception e) {
            ZLog.e("setDrawerLeftEdgeSize failed:" + e.getMessage());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
