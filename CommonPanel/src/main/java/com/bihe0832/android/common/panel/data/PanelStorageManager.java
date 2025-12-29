package com.bihe0832.android.common.panel.data;


import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.text.TextUtils;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.gson.JsonHelper;
import com.bihe0832.android.lib.panel.PanelManager;
import com.bihe0832.android.lib.panel.bean.BoardInfo;
import com.bihe0832.android.lib.panel.bean.BoardsInfo;
import com.bihe0832.android.lib.panel.bean.DrawPenPoint;
import com.bihe0832.android.lib.panel.bean.DrawPenStr;
import com.bihe0832.android.lib.panel.bean.DrawPoint;
import com.bihe0832.android.lib.panel.bean.Point;
import com.bihe0832.android.lib.panel.constants.DrawEvent;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.utils.time.DateUtil;

public class PanelStorageManager {

    public static final String CACHE_DIR = "Panel";
    private static final String CACHE_DIR_PHOTO = "image";
    private static final String CACHE_DIR_WB = "panel";

    private static final String FORMAT_PHOTO = ".png";
    private static final String FORMAT_PANEL = ".panel";

    public static String getRootPath() {
        return FileUtils.INSTANCE.getFolderPathWithSeparator(ZixieContext.INSTANCE.getZixieFolder() + CACHE_DIR);
    }

    public static String getPhotoPath() {
        return FileUtils.INSTANCE.getFolderPathWithSeparator(getRootPath() + CACHE_DIR_PHOTO);
    }

    public static String getPanelPath() {
        return FileUtils.INSTANCE.getFolderPathWithSeparator(getRootPath() + CACHE_DIR_WB);
    }

    public static String getPhotoSavePath() {
        return getPhotoSavePath(DateUtil.getCurrentDateEN("yyyyMMdd_HHmmss"));
    }

    public static String getPhotoSavePath(String name) {
        return getPhotoPath() + name + FORMAT_PHOTO;
    }


    public static String getPanelSavePath() {
        return getPanelSavePath(DateUtil.getCurrentDateEN("yyyyMMdd_HHmmss"));
    }

    public static String getPanelSavePath(String name) {
        return getPhotoPath() + name + FORMAT_PANEL;
    }


    /**
     * 存储白板集
     */
    public static void saveBoard(String filePath) {
        BoardsInfo boardPoints = PanelManager.getInstance().getDrawBoard();
        if (boardPoints == null
                || boardPoints.getBoardList() == null
                || boardPoints.getBoardList().isEmpty()) {
            return;
        }
        String result = JsonHelper.INSTANCE.toJson(boardPoints);
        FileUtils.INSTANCE.writeToFile(filePath, result, false);
        ZixieContext.INSTANCE.showToast(ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.model.res.R.string.white_board_save_sucess));
    }

    /**
     * 读取白板集
     *
     * @return
     */
    public static BoardsInfo loadBoardByPath(String filePath) {
        String strJson = FileUtils.INSTANCE.getFileContent(filePath);
        return loadBoardByContent(strJson);
    }

    /**
     * 读取白板集
     *
     * @return
     */
    public static BoardsInfo loadBoardByContent(String content) {
        if (!TextUtils.isEmpty(content)) {
            BoardsInfo whiteBoardPoints = JsonHelper.INSTANCE.fromJson(content, BoardsInfo.class);
            resetBoard(whiteBoardPoints);
            return whiteBoardPoints;
        }
        return null;
    }

    /**
     * 从json字符中将Path、Paint系统类转换出来
     */
    public static void resetBoard(BoardsInfo whiteBoardPoints) {
        for (BoardInfo whiteBoardPoint : whiteBoardPoints.getBoardList()) {
            whiteBoardPoint.getDeletePoints().clear();
            for (DrawPoint drawPoint : whiteBoardPoint.getSavePoints()) {
                if (drawPoint.getType() == DrawEvent.TYPE_DRAW_PEN) {
                    DrawPenStr drawPenStr = drawPoint.getDrawPenStr();
                    Paint paint = new Paint();
                    //是否使用抗锯齿功能,会消耗较大资源，绘制图形速度会变慢
                    paint.setAntiAlias(true);
                    // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
                    paint.setDither(true);
                    //设置绘制的颜色
                    paint.setColor(drawPenStr.getColor());
                    //设置画笔的样式
                    paint.setStyle(Paint.Style.STROKE);
                    //设置绘制时各图形的结合方式，如平滑效果等
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    //当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式    Cap.ROUND,或方形样式Cap.SQUARE
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    //当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的粗细度
                    paint.setStrokeWidth(drawPenStr.getStrokeWidth());
                    if (drawPenStr.getIsEraser()) {
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));//擦除模式
                    }
                    Path path = new Path();
                    path.moveTo(drawPenStr.getMoveTo().getX(), drawPenStr.getMoveTo().getY());
                    for (int i = 0; i < drawPenStr.getQuadToA().size(); i++) {
                        Point pointA = drawPenStr.getQuadToA().get(i);
                        Point pointB = drawPenStr.getQuadToB().get(i);
                        path.quadTo(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
                    }
                    path.lineTo(drawPenStr.getLineTo().getX(), drawPenStr.getLineTo().getY());
                    path.offset(drawPenStr.getOffset().getX(), drawPenStr.getOffset().getY());

                    DrawPenPoint drawPenPoint = new DrawPenPoint();
                    drawPenPoint.setPaint(paint);
                    drawPenPoint.setPath(path);
                    drawPoint.setDrawPen(drawPenPoint);
                }
            }
        }
    }

}
