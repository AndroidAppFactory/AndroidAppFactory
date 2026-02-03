package com.bihe0832.android.lib.download;


import android.text.TextUtils;
import android.util.Log;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.lib.utils.MathUtils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Protocol;


/**
 * 下载任务信息实体类
 * 
 * <p>该类是整个下载模块的核心数据结构，封装了下载任务的所有配置和状态信息。
 * 一个 DownloadItem 实例代表一个完整的下载任务，包含：
 * <ul>
 *   <li>任务配置：URL、文件路径、校验信息等</li>
 *   <li>运行状态：下载进度、速度、状态等</li>
 *   <li>控制选项：优先级、网络策略、重试机制等</li>
 * </ul>
 * 
 * <h2>基本使用</h2>
 * <pre>{@code
 * DownloadItem item = new DownloadItem();
 * item.setDownloadURL("https://example.com/file.zip");  // 必填
 * item.setDownloadTitle("示例文件");                     // 可选
 * item.setContentMD5("abc123...");                       // 可选，用于校验
 * DownloadRangeManager.INSTANCE.startTask(item, listener);
 * }</pre>
 * 
 * <h2>字段分类说明</h2>
 * <ul>
 *   <li><b>必填字段</b>：downloadURL（下载地址）</li>
 *   <li><b>配置字段</b>：用户可设置，影响下载行为</li>
 *   <li><b>状态字段</b>：由系统维护，反映当前状态</li>
 *   <li><b>临时字段</b>：transient 标记，不会序列化</li>
 * </ul>
 * 
 * <h2>下载类型</h2>
 * <ul>
 *   <li>{@link #TYPE_FILE}：普通文件下载，不分片</li>
 *   <li>{@link #TYPE_RANGE}：分片下载，支持断点续传和多线程</li>
 * </ul>
 * 
 * <h2>暂停类型体系</h2>
 * 参见 {@link DownloadPauseType}，不同暂停类型有不同的恢复策略：
 * <ul>
 *   <li>PAUSED_BY_NETWORK(1)：WiFi→移动网络切换，WiFi恢复后自动重试</li>
 *   <li>PAUSED_BY_USER(2)：用户主动暂停，只能手动恢复（优先级最高）</li>
 *   <li>PAUSED_BY_ALL(3)：批量暂停，需调用 resumeAll() 恢复</li>
 *   <li>PAUSED_PENDING_START(4)：添加任务时未启动</li>
 *   <li>PAUSED_BY_NETWORK_ERROR(5)：网络异常，网络恢复后自动重试</li>
 * </ul>
 * 
 * <h2>网络异常重试机制</h2>
 * 当发生可恢复的网络错误时（如超时、连接失败等），系统会：
 * <ol>
 *   <li>将任务暂停为 PAUSED_BY_NETWORK_ERROR 状态</li>
 *   <li>等待网络恢复后自动重试</li>
 *   <li>重试轮数超过 {@link #MAX_NETWORK_ERROR_RETRY_ROUND} 次后标记失败</li>
 * </ol>
 * 
 * <h2>线程安全说明</h2>
 * <ul>
 *   <li>{@link #setFinished(long)} 和 {@link #addFinished(long)} 是同步方法</li>
 *   <li>lastSpeed、startTime、pauseTime 使用 volatile 保证可见性</li>
 *   <li>其他字段的并发访问由调用方保证</li>
 * </ul>
 * 
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 * @see DownloadStatus 下载状态定义
 * @see DownloadPauseType 暂停类型定义
 * @see DownloadErrorCode 错误码定义
 * @see DownloadListener 下载回调接口
 */
public class DownloadItem implements Serializable {

    /** 日志 TAG */
    public static final String TAG = "Download";
    
    // ==================== 优先级常量 ====================
    
    /** 最高下载优先级，值为100 */
    public static final int MAX_DOWNLOAD_PRIORITY = 100;
    
    /** 强制下载优先级，值为50，用于需要立即下载的场景 */
    public static final int FORCE_DOWNLOAD_PRIORITY = 50;
    
    /** 最低下载优先级，值为0 */
    public static final int MIN_DOWNLOAD_PRIORITY = 0;
    
    /** 默认下载优先级，值为10 */
    public static final int DEFAULT_DOWNLOAD_PRIORITY = 10;
    
    // ==================== 重试相关常量 ====================
    
    /**
     * 网络异常最大重试轮数
     * <p>当网络异常导致下载失败时，系统会自动进入暂停状态等待网络恢复。
     * 每次网络恢复后尝试重试，超过此轮数后将标记任务为失败状态（ERR_MAX_RETRY_EXCEEDED）。
     * <p>注意：这是"轮数"而非"次数"，每一轮内部可能有多次底层重试。
     */
    public static final int MAX_NETWORK_ERROR_RETRY_ROUND = 3;

    // ==================== 下载类型常量 ====================
    
    /** 普通文件下载，不分片，适用于小文件或不支持 Range 的服务器 */
    public static int TYPE_FILE = 1;
    
    /** 
     * 分片下载，支持断点续传和多线程
     * <p>适用于大文件下载，可提高下载速度和稳定性
     */
    public static int TYPE_RANGE = 2;
    
    // ==================== 必填字段 ====================
    
    /**
     * 下载URL地址【必填】
     * <p>这是唯一的必填字段，其他字段都有默认值或由系统自动计算。
     * <p>URL 会参与 downloadID 的计算，相同 URL + actionKey 的任务被视为同一任务。
     */
    private String downloadURL = "";

    // ==================== 请求配置字段 ====================
    
    /**
     * 自定义请求头【可选】
     * <p>用于需要特定 Header 的下载场景，如需要认证、自定义 User-Agent 等。
     * <p>示例：{ "Authorization": "Bearer xxx", "User-Agent": "MyApp/1.0" }
     */
    private Map<String, String> requestHeader = null;
    
    /**
     * 下载类型【可选】
     * <p>默认为 TYPE_FILE（普通下载），可设置为 TYPE_RANGE（分片下载）。
     * <p>分片下载支持断点续传和多线程，适用于大文件。
     * @see #TYPE_FILE
     * @see #TYPE_RANGE
     */
    private int downloadType = TYPE_FILE;
    
    /**
     * 实际下载URL【系统维护】
     * <p>某些情况下，原始 URL 可能会重定向到真实地址，此字段保存最终的下载地址。
     * <p>由系统在下载过程中自动设置，用户无需填写。
     */
    private String realURL = "";
    
    /**
     * 下载起始位置（字节偏移量）【可选】
     * <p>用于分片下载场景，指定从文件的哪个位置开始下载。
     * <p>普通下载时为 0，分片下载时由系统计算。
     */
    private long rangeStart = 0L;
    
    /**
     * 本地文件写入起始位置【可选】
     * <p>用于分片下载场景，指定写入本地文件的起始偏移量。
     * <p>通常与 rangeStart 配合使用。
     */
    private long localStart = 0L;
    
    /**
     * 文件总长度（字节）【可选】
     * <p>如果已知文件大小可预先设置，否则由系统在下载时自动获取。
     * <p>-1 表示未知长度。
     */
    private long contentLength = -1;
    
    // ==================== 文件校验字段 ====================
    
    /**
     * 文件 MD5 校验值【可选】
     * <p>下载完成后会与此值进行校验，不一致则根据 forceDeleteBad 决定是否删除。
     * <p>建议对重要文件设置此值以保证完整性。
     */
    private String contentMD5 = "";
    
    /**
     * 文件 SHA256 校验值【可选】
     * <p>比 MD5 更安全的校验方式，优先使用此值进行校验。
     */
    private String contentSHA256 = "";
    
    // ==================== 下载行为控制字段 ====================
    
    /**
     * 是否强制重新下载【可选】
     * <p>默认 false。设为 true 时，即使本地已有同名文件也会重新下载。
     * <p>注意：如果文件有 MD5 校验且校验通过，此选项不会生效（已校验通过的文件不会重下）。
     * <p>主要用于：强制更新、不支持分片续传的场景。
     */
    private boolean shouldForceReDownload = false;
    
    /**
     * MD5 校验失败时是否自动删除文件【可选】
     * <p>默认 true。当下载完成但 MD5 校验失败时，自动删除损坏的文件。
     * <p>建议保持默认值，避免使用损坏的文件。
     */
    private boolean forceDeleteBad = true;
    
    /**
     * 下载完成后是否自动安装【可选】
     * <p>默认 false。仅对 APK 文件有效，设为 true 会在下载完成后自动调起安装。
     */
    private boolean autoInstall = false;
    
    /**
     * 是否在通知栏显示下载进度【可选】
     * <p>默认 false。设为 true 会在系统通知栏显示下载进度。
     */
    private boolean notificationVisibility = false;
    
    /**
     * 移动网络下是否允许下载【可选】
     * <p>默认 false（仅 WiFi 下载）。设为 true 允许在移动网络下下载。
     * <p>当从 WiFi 切换到移动网络时：
     * <ul>
     *   <li>false：任务暂停，pauseType = PAUSED_BY_NETWORK</li>
     *   <li>true：继续下载</li>
     * </ul>
     */
    private boolean downloadWhenUseMobile = false;
    
    /**
     * 添加任务后是否自动开始下载【可选】
     * <p>默认 true。设为 false 时，任务添加后处于暂停状态，需手动调用恢复。
     * <p>暂停时 pauseType = PAUSED_PENDING_START
     */
    private boolean downloadWhenAdd = true;
    
    // ==================== 下载状态字段（系统维护）====================
    
    /**
     * 当前下载状态【系统维护】
     * <p>由下载管理器实时更新，反映任务当前状态。
     * @see DownloadStatus
     */
    private int status = DownloadStatus.STATUS_DOWNLOAD_PAUSED;
    
    /**
     * 暂停类型【系统维护】
     * <p>当状态为暂停时，此字段标识暂停的原因，用于决定恢复策略。
     * <p>不同暂停类型有不同的优先级，用户暂停(PAUSED_BY_USER)优先级最高，不会被其他类型覆盖。
     * @see DownloadPauseType
     */
    private int pauseType = 0;
    
    /**
     * 下载回调监听器【临时字段】
     * <p>transient 标记，不会序列化。
     * <p>用于接收下载进度、成功、失败等回调。
     * @see DownloadListener
     */
    private transient DownloadListener mDownloadListener = null;
    
    // ==================== 展示信息字段 ====================
    
    /**
     * 下载描述信息【可选】
     * <p>用于 UI 展示的描述文本，如 "更新日志：修复若干问题..."
     */
    private String downloadDesc = "";
    
    /**
     * 下载标题【可选】
     * <p>用于 UI 展示的标题，如通知栏显示的标题。
     */
    private String downloadTitle = "";
    
    /**
     * 下载图标 URL【可选】
     * <p>用于 UI 展示的图标地址，如通知栏显示的图标。
     */
    private String downloadIcon = "";
    
    // ==================== 文件路径字段 ====================
    
    /**
     * 下载目录【可选】
     * <p>指定文件保存的目录路径。如不设置，使用默认下载目录。
     * <p>注意：只支持传入目录路径，不支持完整文件路径。如需下载到指定文件，请使用 DownloadTools 封装。
     */
    private String fileFolder = "";
    
    /**
     * 最终文件路径【系统维护】
     * <p>下载完成后文件的完整路径，由系统根据 fileFolder 和文件名计算得出。
     */
    private String filePath = "";
    
    // ==================== 扩展信息字段 ====================
    
    /**
     * 动作标识【可选】
     * <p>用于区分同一 URL 的不同下载任务，会参与 downloadID 的计算。
     * <p>透传字段，会传递到所有下载相关的事件回调中（包括安装回调）。
     * <p>使用场景：同一文件需要下载多份到不同位置。
     */
    private String actionKey = "";
    
    /**
     * 扩展信息【可选】
     * <p>透传字段，会传递到所有下载相关的事件回调中。
     * <p>可用于携带业务自定义数据，如来源页面、下载原因等。
     */
    private String extraInfo = "";
    
    /**
     * 应用包名【可选】
     * <p>下载 APK 时设置，用于安装后的应用识别。
     */
    private String packageName = "";
    
    /**
     * 应用版本号【可选】
     * <p>下载 APK 时设置，配合 packageName 使用。
     */
    private long versionCode = 0;

    // ==================== 下载进度字段（系统维护）====================
    
    /**
     * 已下载字节数【系统维护】
     * <p>实时更新的已下载数据量，用于计算下载进度。
     * <p>通过 {@link #setFinished(long)} 和 {@link #addFinished(long)} 同步方法更新。
     */
    private long finishedLength = 0;
    
    /**
     * 本轮下载开始前的已完成字节数【系统维护】
     * <p>用于计算本轮下载的增量和平均速度。
     */
    private long finishedLengthBefore = 0;

    // ==================== 实时统计字段（临时，系统维护）====================
    
    /**
     * 实时下载速度（字节/秒）【临时字段，系统维护】
     * <p>transient + volatile：不序列化，多线程可见。
     * <p>由下载线程实时更新。
     */
    private volatile transient long lastSpeed = 0;
    
    /**
     * 本轮下载开始时间戳（毫秒）【临时字段，系统维护】
     * <p>用于计算平均下载速度。
     */
    private volatile transient long startTime = 0;
    
    /**
     * 最后暂停时间戳（毫秒）【临时字段，系统维护】
     * <p>记录任务进入暂停状态的时间。
     */
    private volatile transient long pauseTime = 0;

    // ==================== 调度相关字段 ====================
    
    /**
     * 下载优先级【可选】
     * <p>取值范围 [{@link #MIN_DOWNLOAD_PRIORITY}, {@link #MAX_DOWNLOAD_PRIORITY}]，默认 {@link #DEFAULT_DOWNLOAD_PRIORITY}。
     * <p>优先级高的任务会优先下载。当下载队列有空位时，优先调度高优先级任务。
     */
    private int downloadPriority = DEFAULT_DOWNLOAD_PRIORITY;
    
    /**
     * 是否保存下载记录【可选】
     * <p>默认 false。设为 true 会将下载记录持久化，用于历史记录展示等场景。
     */
    private boolean needRecord = false;

    // ==================== 协议相关字段 ====================
    
    /**
     * HTTP 协议版本【系统维护】
     * <p>在获取文件长度时自动检测并记录，用于分片策略选择。
     * <p>HTTP/2 支持更高效的多路复用，可能影响分片数量的计算。
     * @see #isHttp2()
     */
    private Protocol protocol = Protocol.HTTP_1_1;
    
    // ==================== 重试机制字段 ====================
    
    /**
     * 网络异常重试轮数【临时字段，系统维护】
     * <p>transient 标记，不持久化，App 重启后自动重置为 0。
     * 
     * <p><b>设计说明：</b>
     * <ul>
     *   <li>当发生可恢复的网络错误时，此计数递增</li>
     *   <li>网络恢复后自动重试，但超过 {@link #MAX_NETWORK_ERROR_RETRY_ROUND} 轮后标记失败</li>
     *   <li>下载成功或用户手动恢复时重置为 0</li>
     *   <li>App 重启后自动重置，给用户新的重试机会</li>
     * </ul>
     * 
     * <p><b>重试流程：</b>
     * <pre>
     * 网络异常 → incrementNetworkErrorRetryRound() → 
     *   if (轮数 < 3) → 暂停等待网络恢复 → 网络恢复 → 重试
     *   if (轮数 >= 3) → 标记失败 (ERR_MAX_RETRY_EXCEEDED)
     * </pre>
     * 
     * @see #incrementNetworkErrorRetryRound()
     * @see #resetNetworkErrorRetryRound()
     * @see #MAX_NETWORK_ERROR_RETRY_ROUND
     */
    private transient int networkErrorRetryRound = 0;

    // ==================== 静态工具方法 ====================
    
    /**
     * 根据 URL 和 actionKey 计算下载任务ID
     * 
     * <p>downloadID 是任务的唯一标识，相同 URL + actionKey 的任务被视为同一任务。
     * 
     * @param url 下载地址
     * @param actionKey 动作标识，用于区分同一 URL 的不同任务
     * @return 无符号整数形式的任务ID
     */
    public static long getDownloadIDByURL(String url, String actionKey) {
        return ConvertUtils.getUnsignedInt((actionKey + url).hashCode());
    }

    /**
     * 生成下载动作标识
     * 
     * <p>对于分片下载，actionKey 包含分片的位置信息，用于区分同一文件的不同分片。
     * 
     * @param downloadType 下载类型，TYPE_FILE 或 TYPE_RANGE
     * @param start 分片起始位置
     * @param length 分片长度
     * @param localStart 本地写入起始位置
     * @return 普通下载返回空字符串，分片下载返回位置信息字符串
     */
    public static String getDownloadActionKey(int downloadType, long start, long length, long localStart) {
        if (downloadType == DownloadItem.TYPE_FILE) {
            return "";
        } else {
            return start + "-" + length + "-" + (start + length - 1) + "-" + localStart;
        }
    }

    // ==================== 通知相关方法 ====================
    
    /**
     * 设置是否在通知栏显示下载进度
     * 
     * @param visibility true 显示，false 不显示
     */
    public void setNotificationVisibility(boolean visibility) {
        notificationVisibility = visibility;
    }

    /**
     * 获取是否在通知栏显示下载进度
     * 
     * @return true 显示，false 不显示
     */
    public boolean notificationVisibility() {
        return notificationVisibility;
    }
    
    // ==================== 优先级相关方法 ====================

    /**
     * 获取下载优先级
     * 
     * @return 当前优先级，范围 [MIN_DOWNLOAD_PRIORITY, MAX_DOWNLOAD_PRIORITY]
     */
    public int getDownloadPriority() {
        return downloadPriority;
    }

    /**
     * 设置下载优先级
     * 
     * <p>优先级会被限制在有效范围内 [0, 100]：
     * <ul>
     *   <li>小于 0 会被设为 0</li>
     *   <li>大于 100 会被设为 100</li>
     * </ul>
     * 
     * @param downloadPriority 期望的优先级值
     */
    public void setDownloadPriority(int downloadPriority) {
        if (downloadPriority > MIN_DOWNLOAD_PRIORITY) {
            if (downloadPriority > MAX_DOWNLOAD_PRIORITY) {
                this.downloadPriority = MAX_DOWNLOAD_PRIORITY;
            } else {
                this.downloadPriority = downloadPriority;
            }
        } else {
            this.downloadPriority = MIN_DOWNLOAD_PRIORITY;
        }
    }
    
    // ==================== 监听器相关方法 ====================

    /**
     * 获取下载监听器
     * 
     * @return 当前设置的监听器，可能为 null
     */
    public DownloadListener getDownloadListener() {
        return mDownloadListener;
    }

    /**
     * 设置下载监听器
     * 
     * <p>监听器用于接收下载过程中的各种事件回调：
     * <ul>
     *   <li>onWait - 等待下载</li>
     *   <li>onStart - 开始下载</li>
     *   <li>onProgress - 下载进度</li>
     *   <li>onPause - 下载暂停</li>
     *   <li>onSuccess - 下载成功</li>
     *   <li>onFail - 下载失败</li>
     * </ul>
     * 
     * @param listener 监听器实例
     */
    public void setDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
    }
    
    // ==================== 速度与进度相关方法 ====================

    /**
     * 获取平均下载速度
     * 
     * <p>计算公式：(已下载量 - 开始时已下载量) / 已用时间
     * 
     * @return 平均速度（字节/秒），如果计算结果为负则返回 0
     */
    public int getAverageSpeed() {
        long finished = finishedLength - finishedLengthBefore;
        if (finished < 0) {
            return 0;
        } else {
            return (int) (finished * 1.0f * 1000 / (System.currentTimeMillis() - startTime));
        }
    }

    /**
     * 获取下载任务唯一ID
     * 
     * <p>由 downloadURL 和 actionKey 共同计算得出，是任务的唯一标识。
     * 
     * @return 任务ID
     */
    public long getDownloadID() {
        return getDownloadIDByURL(downloadURL, getDownloadActionKey());
    }

    /**
     * 获取当前任务的动作标识
     * 
     * @return 动作标识字符串
     */
    public String getDownloadActionKey() {
        return getDownloadActionKey(downloadType, rangeStart, contentLength, localStart);
    }

    /**
     * 获取下载进度描述（格式化百分比字符串）
     * 
     * @return 格式化的进度字符串，如 "45.23%"
     */
    public String getProcessDesc() {
        return MathUtils.getFormatPercentDesc(getProcess(), 2);
    }

    /**
     * 获取下载进度（浮点数）
     * 
     * @return 下载进度，范围 [0, 1]，保留 4 位小数
     */
    public float getProcess() {
        return MathUtils.getFormatPercent(finishedLength, contentLength, 4);
    }
    
    // ==================== 下载类型相关方法 ====================

    /**
     * 获取下载类型
     * 
     * @return TYPE_FILE（普通下载）或 TYPE_RANGE（分片下载）
     */
    public int getDownloadType() {
        return downloadType;
    }

    /**
     * 设置下载类型
     * 
     * @param downloadType TYPE_FILE 或 TYPE_RANGE
     */
    public void setDownloadType(int downloadType) {
        this.downloadType = downloadType;
    }
    
    // ==================== Range 相关方法 ====================

    /**
     * 获取下载起始位置（服务器端偏移量）
     * 
     * @return 起始字节位置
     */
    public long getRangeStart() {
        return rangeStart;
    }

    /**
     * 设置下载起始位置
     * 
     * @param rangeStart 起始字节位置
     */
    public void setRangeStart(long rangeStart) {
        this.rangeStart = rangeStart;
    }

    /**
     * 获取本地文件写入起始位置
     * 
     * @return 本地文件偏移量
     */
    public long getLocalStart() {
        return localStart;
    }

    /**
     * 设置本地文件写入起始位置
     * 
     * @param localStart 本地文件偏移量
     */
    public void setLocalStart(long localStart) {
        this.localStart = localStart;
    }
    
    // ==================== 文件校验相关方法 ====================

    /**
     * 是否在校验失败时删除文件
     * 
     * @return true 删除，false 保留
     */
    public boolean isForceDeleteBad() {
        return forceDeleteBad;
    }

    /**
     * 设置校验失败时是否删除文件
     * 
     * @param forceDeleteBad true 删除，false 保留
     */
    public void setForceDeleteBad(boolean forceDeleteBad) {
        this.forceDeleteBad = forceDeleteBad;
    }

    /**
     * 获取文件 MD5 校验值
     * 
     * @return MD5 字符串，未设置时返回空字符串
     */
    public String getContentMD5() {
        return contentMD5;
    }

    /**
     * 设置文件 MD5 校验值
     * 
     * @param contentMD5 MD5 字符串
     */
    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * 获取文件 SHA256 校验值
     * 
     * @return SHA256 字符串，未设置时返回空字符串
     */
    public String getContentSHA256() {
        return contentSHA256;
    }

    /**
     * 设置文件 SHA256 校验值
     * 
     * @param contentSHA256 SHA256 字符串
     */
    public void setContentSHA256(String contentSHA256) {
        this.contentSHA256 = contentSHA256;
    }
    
    // ==================== 展示信息相关方法 ====================

    /**
     * 获取下载描述
     * 
     * @return 描述文本
     */
    public String getDownloadDesc() {
        return downloadDesc;
    }

    /**
     * 设置下载描述
     * 
     * @param desc 描述文本
     */
    public void setDownloadDesc(String desc) {
        downloadDesc = desc;
    }

    /**
     * 获取下载标题
     * 
     * @return 标题文本
     */
    public String getDownloadTitle() {
        return downloadTitle;
    }

    /**
     * 设置下载标题
     * 
     * @param downloadTitle 标题文本
     */
    public void setDownloadTitle(String downloadTitle) {
        this.downloadTitle = downloadTitle;
    }

    /**
     * 获取下载图标 URL
     * 
     * @return 图标地址
     */
    public String getDownloadIcon() {
        return downloadIcon;
    }

    /**
     * 设置下载图标 URL
     * 
     * @param downloadIcon 图标地址
     */
    public void setDownloadIcon(String downloadIcon) {
        this.downloadIcon = downloadIcon;
    }
    
    // ==================== 网络策略相关方法 ====================

    /**
     * 是否允许在移动网络下下载
     * 
     * @return true 允许，false 仅 WiFi
     */
    public boolean isDownloadWhenUseMobile() {
        return downloadWhenUseMobile;
    }

    /**
     * 设置是否允许在移动网络下下载
     * 
     * <p>设为 false 时，从 WiFi 切换到移动网络会触发 PAUSED_BY_NETWORK 暂停。
     * 
     * @param downloadWhenUseMobile true 允许，false 仅 WiFi
     */
    public void setDownloadWhenUseMobile(boolean downloadWhenUseMobile) {
        this.downloadWhenUseMobile = downloadWhenUseMobile;
    }

    /**
     * 是否在添加任务后自动开始下载
     * 
     * @return true 自动开始，false 需手动恢复
     */
    public boolean isDownloadWhenAdd() {
        return downloadWhenAdd;
    }

    /**
     * 设置是否在添加任务后自动开始下载
     * 
     * @param downloadWhenAdd true 自动开始，false 添加后暂停
     */
    public void setDownloadWhenAdd(boolean downloadWhenAdd) {
        this.downloadWhenAdd = downloadWhenAdd;
    }
    
    // ==================== URL 相关方法 ====================

    /**
     * 获取实际下载 URL
     * 
     * <p>某些情况下原始 URL 会重定向，此字段保存最终地址。
     * 
     * @return 实际下载地址
     */
    public String getRealURL() {
        return realURL;
    }

    /**
     * 设置实际下载 URL
     * 
     * @param realURL 实际下载地址
     */
    public void setRealURL(String realURL) {
        this.realURL = realURL;
    }

    /**
     * 获取原始下载 URL
     * 
     * @return 原始下载地址（必填字段）
     */
    public String getDownloadURL() {
        return downloadURL;
    }

    /**
     * 设置下载 URL
     * 
     * <p>这是唯一的必填字段，URL 会参与 downloadID 的计算。
     * 
     * @param downloadURL 下载地址
     */
    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }
    
    // ==================== 应用信息相关方法 ====================

    /**
     * 获取应用包名
     * 
     * @return 包名字符串
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * 设置应用包名
     * 
     * @param packageName 包名字符串
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * 获取应用版本号
     * 
     * @return 版本号
     */
    public long getVersionCode() {
        return versionCode;
    }

    /**
     * 设置应用版本号
     * 
     * @param versionCode 版本号
     */
    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }
    
    // ==================== 文件路径相关方法 ====================

    /**
     * 获取最终文件路径
     * 
     * @return 完整文件路径
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置文件路径
     * 
     * @param filePath 完整文件路径
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 获取下载目录
     * 
     * @return 目录路径
     */
    public String getFileFolder() {
        return fileFolder;
    }

    /**
     * 设置下载目录
     * 
     * <p>只支持目录路径，不支持完整文件路径。
     * 
     * @param fileFolder 目录路径
     */
    public void setFileFolder(String fileFolder) {
        this.fileFolder = fileFolder;
    }
    
    // ==================== 下载控制相关方法 ====================

    /**
     * 是否强制重新下载
     * 
     * @return true 强制重下，false 使用已有文件
     */
    public boolean shouldForceReDownload() {
        return shouldForceReDownload;
    }

    /**
     * 设置是否强制重新下载
     * 
     * <p>设为 true 时，即使本地已有同名文件也会重新下载。
     * <p>注意：如果文件有 MD5 校验且校验通过，此选项不会生效。
     * 
     * @param shouldForceReDownload true 强制重下
     */
    public void setShouldForceReDownload(boolean shouldForceReDownload) {
        this.shouldForceReDownload = shouldForceReDownload;
    }

    /**
     * 是否自动安装
     * 
     * @return true 下载完成后自动安装
     */
    public boolean isAutoInstall() {
        return autoInstall;
    }

    /**
     * 设置是否自动安装
     * 
     * <p>仅对 APK 文件有效。
     * 
     * @param autoInstall true 自动安装
     */
    public void setAutoInstall(boolean autoInstall) {
        this.autoInstall = autoInstall;
    }
    
    // ==================== 下载状态相关方法 ====================

    /**
     * 获取当前下载状态
     * 
     * @return 状态值
     * @see DownloadStatus
     */
    public @DownloadStatus int getStatus() {
        return status;
    }

    /**
     * 设置下载状态
     * 
     * @param newStatus 新状态值
     */
    public void setStatus(@DownloadStatus int newStatus) {
        Log.d("DownloadItem", "status change , before: " + this.status + " after : " + newStatus);
        this.status = newStatus;
    }
    
    // ==================== 下载进度相关方法 ====================

    /**
     * 获取开始时的已完成量
     * 
     * <p>用于计算本轮下载的增量。
     * 
     * @return 字节数
     */
    public long getFinishedLengthBefore() {
        return finishedLengthBefore;
    }

    /**
     * 设置开始时的已完成量
     * 
     * @param finishedLengthBefore 字节数
     */
    public void setFinishedLengthBefore(long finishedLengthBefore) {
        this.finishedLengthBefore = finishedLengthBefore;
    }

    /**
     * 获取已下载字节数
     * 
     * @return 已完成的字节数
     */
    public long getFinished() {
        return finishedLength;
    }

    /**
     * 设置已下载字节数（线程安全）
     * 
     * <p>此方法是同步的，可在多线程环境下安全调用。
     * 
     * @param finished 已下载的字节数
     */
    public synchronized void setFinished(long finished) {
        this.finishedLength = finished;
    }

    /**
     * 增加已下载字节数（线程安全）
     * 
     * <p>此方法是同步的，适用于多线程累加进度的场景。
     * 
     * @param data 新增的字节数
     * @return 更新后的已下载总字节数
     */
    public synchronized long addFinished(long data) {
        this.finishedLength = this.finishedLength + data;
        return this.finishedLength;
    }
    
    // ==================== 速度与时间相关方法 ====================

    /**
     * 获取实时下载速度
     * 
     * @return 速度（字节/秒）
     */
    public long getLastSpeed() {
        return lastSpeed;
    }

    /**
     * 设置实时下载速度
     * 
     * @param lastSpeed 速度（字节/秒），负值会被忽略
     */
    public void setLastSpeed(long lastSpeed) {
        if (lastSpeed >= 0) {
            this.lastSpeed = lastSpeed;
        }
    }

    /**
     * 获取暂停时间戳
     * 
     * @return 暂停时间（毫秒）
     */
    public long getPauseTime() {
        return pauseTime;
    }

    /**
     * 获取下载开始时间戳
     * 
     * @return 开始时间（毫秒）
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 设置下载开始时间戳
     * 
     * @param startTime 开始时间（毫秒）
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    // ==================== 暂停相关方法 ====================

    /**
     * 设置暂停状态和类型
     * 
     * <p>此方法会：
     * <ol>
     *   <li>检查优先级：用户暂停(PAUSED_BY_USER)优先级最高，不会被其他类型覆盖</li>
     *   <li>设置状态为 STATUS_DOWNLOAD_PAUSED</li>
     *   <li>记录暂停类型和时间</li>
     * </ol>
     * 
     * <p><b>暂停类型优先级</b>（从高到低）：
     * <ul>
     *   <li>PAUSED_BY_USER - 用户主动暂停，只能被同类型覆盖</li>
     *   <li>其他类型 - 可以相互覆盖</li>
     * </ul>
     * 
     * @param pauseType 暂停类型
     * @see DownloadPauseType
     */
    public void setPause(int pauseType) {
        // 用户主动暂停的优先级最高，不允许被其他类型覆盖
        if (this.pauseType == DownloadPauseType.PAUSED_BY_USER && 
            pauseType != DownloadPauseType.PAUSED_BY_USER) {
            return;
        }
        this.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED;
        this.pauseType = pauseType;
        this.pauseTime = System.currentTimeMillis();
    }

    /**
     * 获取暂停类型
     * 
     * @return 暂停类型值
     * @see DownloadPauseType
     */
    public int getPauseType() {
        return pauseType;
    }
    
    // ==================== 网络异常重试相关方法 ====================
    
    /**
     * 获取网络异常重试轮数
     * 
     * <p>每次因网络异常暂停并等待恢复算一轮。
     * 
     * @return 当前重试轮数
     */
    public int getNetworkErrorRetryRound() {
        return networkErrorRetryRound;
    }
    
    /**
     * 增加网络异常重试轮数
     * 
     * <p>在可恢复的网络错误发生时调用。
     */
    public void incrementNetworkErrorRetryRound() {
        this.networkErrorRetryRound++;
    }
    
    /**
     * 重置网络异常重试轮数
     * 
     * <p>在以下情况下调用：
     * <ul>
     *   <li>下载成功完成</li>
     *   <li>用户手动恢复下载</li>
     * </ul>
     */
    public void resetNetworkErrorRetryRound() {
        this.networkErrorRetryRound = 0;
    }
    
    // ==================== 扩展信息相关方法 ====================

    /**
     * 获取扩展信息
     * 
     * @return 扩展信息字符串
     */
    public String getExtraInfo() {
        return extraInfo;
    }

    /**
     * 设置扩展信息
     * 
     * <p>空值会被忽略。扩展信息会透传到所有下载相关的事件回调。
     * 
     * @param extraInfo 扩展信息字符串
     */
    public void setExtraInfo(String extraInfo) {
        if (!TextUtils.isEmpty(extraInfo)) {
            this.extraInfo = extraInfo;
        }
    }

    /**
     * 获取动作标识
     * 
     * @return 动作标识字符串
     */
    public String getActionKey() {
        return actionKey;
    }

    /**
     * 设置动作标识
     * 
     * <p>空值会被忽略。actionKey 参与 downloadID 计算，用于区分同一 URL 的不同任务。
     * 
     * @param actionKey 动作标识字符串
     */
    public void setActionKey(String actionKey) {
        if (!TextUtils.isEmpty(actionKey)) {
            this.actionKey = actionKey;
        }
    }
    
    // ==================== 记录相关方法 ====================

    /**
     * 是否需要保存下载记录
     * 
     * @return true 保存记录
     */
    public boolean isNeedRecord() {
        return needRecord;
    }

    /**
     * 设置是否需要保存下载记录
     * 
     * @param needRecord true 保存记录
     */
    public void setNeedRecord(boolean needRecord) {
        this.needRecord = needRecord;
    }
    
    // ==================== 文件长度相关方法 ====================

    /**
     * 获取文件总长度
     * 
     * @return 文件字节数，-1 表示未知
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * 设置文件总长度
     * 
     * @param rangeLength 文件字节数
     */
    public void setContentLength(long rangeLength) {
        this.contentLength = rangeLength;
    }
    
    // ==================== 请求头相关方法 ====================

    /**
     * 获取自定义请求头
     * 
     * @return 请求头 Map
     */
    public Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    /**
     * 设置自定义请求头
     * 
     * @param requestHeader 请求头 Map
     */
    public void setRequestHeader(Map<String, String> requestHeader) {
        this.requestHeader = requestHeader;
    }
    
    // ==================== 协议相关方法 ====================

    /**
     * 获取 HTTP 协议版本
     * 
     * @return Protocol 枚举值
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * 设置 HTTP 协议版本
     * 
     * <p>null 值会被忽略。
     * 
     * @param protocol Protocol 枚举值
     */
    public void setProtocol(Protocol protocol) {
        if (protocol != null) {
            this.protocol = protocol;
        }
    }

    /**
     * 判断是否使用 HTTP/2 协议
     * 
     * <p>HTTP/2 支持多路复用，可能影响分片策略的选择。
     * 
     * @return true 使用 HTTP/2，false 使用 HTTP/1.x
     */
    public boolean isHttp2() {
        return protocol == Protocol.HTTP_2;
    }
    
    // ==================== 数据更新方法 ====================

    /**
     * 从另一个 DownloadItem 更新数据
     * 
     * <p>只有 downloadID 相同时才会更新。更新的字段包括：
     * <ul>
     *   <li>URL 相关：downloadURL, realURL</li>
     *   <li>Range 相关：rangeStart, localStart, contentLength</li>
     *   <li>校验相关：contentMD5, contentSHA256</li>
     *   <li>控制选项：shouldForceReDownload, forceDeleteBad, autoInstall 等</li>
     *   <li>展示信息：downloadDesc, downloadTitle, downloadIcon 等</li>
     *   <li>扩展信息：actionKey, extraInfo, packageName, versionCode</li>
     *   <li>调度相关：downloadPriority, needRecord, protocol</li>
     * </ul>
     * 
     * <p><b>不会更新的字段</b>：
     * <ul>
     *   <li>状态相关：status, pauseType, pauseTime</li>
     *   <li>进度相关：finishedLength, finishedLengthBefore, lastSpeed, startTime</li>
     *   <li>重试相关：networkErrorRetryRound</li>
     * </ul>
     * 
     * @param item 源 DownloadItem
     */
    public void update(DownloadItem item) {
        if (item.getDownloadID() == getDownloadID()) {
            this.downloadURL = item.downloadURL;
            this.downloadType = item.downloadType;
            if (!TextUtils.isEmpty(item.realURL)) {
                this.realURL = item.realURL;
            }
            this.rangeStart = item.rangeStart;
            this.localStart = item.localStart;
            this.contentLength = item.contentLength;
            this.contentMD5 = item.contentMD5;
            this.contentSHA256 = item.contentSHA256;
            this.shouldForceReDownload = item.shouldForceReDownload;
            this.forceDeleteBad = item.forceDeleteBad;
            this.autoInstall = item.autoInstall;
            this.notificationVisibility = item.notificationVisibility;
            this.downloadWhenUseMobile = item.downloadWhenUseMobile;
            this.downloadWhenAdd = item.downloadWhenAdd;
            this.mDownloadListener = item.mDownloadListener;
            this.downloadDesc = item.downloadDesc;
            this.downloadTitle = item.downloadTitle;
            this.fileFolder = item.fileFolder;
            this.filePath = item.filePath;
            this.actionKey = item.actionKey;
            this.extraInfo = item.extraInfo;
            this.packageName = item.packageName;
            this.versionCode = item.versionCode;
            this.downloadIcon = item.downloadIcon;

            this.downloadPriority = item.downloadPriority;
            this.needRecord = item.needRecord;
            if (item.protocol != null) {
                this.protocol = item.protocol;
            }
            if (item.requestHeader != null) {
                this.requestHeader = new HashMap<>(item.requestHeader);
            }
        } else {
            Log.e(TAG, "update error , download id is bad ");
        }
    }
    
    // ==================== 调试方法 ====================

    /**
     * 返回下载任务的字符串表示
     * 
     * <p>包含所有关键字段的信息，用于日志输出和调试。
     * 
     * @return 格式化的字符串
     */
    @Override
    public String toString() {
        long code = 0;
        if (mDownloadListener != null) {
            code = mDownloadListener.hashCode();
        }
        return "下载资源：{" + " downloadURL='" + downloadURL + " ,realURL='" + realURL + ", listener=" + code
                + ", title='" + downloadTitle + ", actionKey='" + actionKey + ", extraInfo='" + extraInfo
                + ", rangeStart=" + rangeStart + ", localStart=" + localStart + ", contentLength=" + contentLength
                + ", fileFolder='" + fileFolder + ", filePath='" + filePath + ", fileMD5='" + contentMD5
                + ", fileSHA256='" + contentSHA256 + ", forceDownloadNew=" + shouldForceReDownload + ", downloadDesc='"
                + downloadDesc + ", packageName='" + packageName + ", versionCode=" + versionCode + ", finishedLength="
                + finishedLength + ", finishedLengthBefore=" + finishedLengthBefore + ", lastSpeed=" + lastSpeed
                + ", startTime=" + startTime + ", pauseTime=" + pauseTime + ", downloadIcon='" + downloadIcon
                + ", autoInstall=" + autoInstall + ", status=" + status + ", downloadWhenUseMobile="
                + downloadWhenUseMobile + ", downloadWhenAdd=" + downloadWhenAdd
                + ", protocol=" + protocol + '}';
    }
}
