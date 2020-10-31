package com.bihe0832.android.test.base

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bihe0832.android.framework.base.BaseFragment
import com.bihe0832.android.lib.http.common.HttpBasicRequest.LOG_TAG
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.DebugTools
import com.bihe0832.android.lib.text.InputDialogCompletedCallback
import com.bihe0832.android.test.R
import com.bihe0832.android.test.module.web.WebPageActivity
import com.bihe0832.android.test.module.web.WebviewFragment

abstract class BaseTestFragment : BaseFragment() {
    private var mRecy: RecyclerView? = null
    protected var mAdapter: TestPagerAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test_tab, container, false)
        initView(view)
        return view
    }

    protected val testTips: String
        protected get() = ""

    private fun initView(view: View) {
        mAdapter = TestPagerAdapter(_mActivity)
        mAdapter!!.setDatas(getDataList())
        mRecy = view.findViewById(R.id.test_recy)
        val textView = view.findViewById<TextView>(R.id.test_tips)
        if (TextUtils.isEmpty(testTips)) {
            textView.visibility = View.GONE
        } else {
            textView.text = testTips
            textView.visibility = View.VISIBLE
        }
        mRecy?.setHasFixedSize(true)
        val manager = LinearLayoutManager(_mActivity)
        mRecy?.setLayoutManager(manager)
        mRecy?.setAdapter(mAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    abstract fun getDataList(): List<TestItem>
    protected fun sendInfo(title: String, content: String) {
        DebugTools.sendInfo(context, title, content, false)
    }

    protected fun showInfo(title: String, content: String) {
        DebugTools.showInfo(context, title, content, "分享给我们")
    }

    fun showInputDialog(titleName: String, msg: String, defaultValue: String, listener: InputDialogCompletedCallback) {
        DebugTools.showInputDialog(context, titleName, msg, defaultValue, listener)
    }

    protected fun openWeb(url: String) {
        val intent = Intent(context, WebPageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(WebviewFragment.INTENT_KEY_URL, url)
        startActivity(intent)
    }

    protected fun startActivity(cls: Class<*>) {
        val intent = Intent(context, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    protected fun showResult(s: String?) {
        s?.let {
            ZLog.d(LOG_TAG, "showResult:$s")
            val textView = view?.findViewById<TextView>(R.id.test_tips)
            textView?.text = "Result: $s"
        }
    }
}