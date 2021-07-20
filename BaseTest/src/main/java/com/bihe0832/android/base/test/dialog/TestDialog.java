package com.bihe0832.android.base.test.dialog;

import android.content.Context;
import com.bihe0832.android.base.test.R;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;

/**
 * @author hardyshi code@bihe0832.com Created on 7/20/21.
 */
class TestDialog extends CommonDialog {

    public TestDialog(Context context) {
        super(context, R.style.testDialog);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.dialog_test_layout;
    }
}
