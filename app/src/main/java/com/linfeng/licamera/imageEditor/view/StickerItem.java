package com.linfeng.licamera.imageEditor.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import com.linfeng.licamera.R;
import com.linfeng.licamera.util.CommonUtil;

public class StickerItem {
    public static final int STICKER_BTN_HALF_SIZE = 30;
    private static final float MIN_SCALE = 0.15f;
    private static final int HELP_BOX_PAD = 25;

    private static final int BUTTON_WIDTH = STICKER_BTN_HALF_SIZE;

    public Bitmap bitmap;
    public Rect srcRect; // 原始坐标
    public RectF dstRect; // 移动的目标坐标
    private Rect helpToolsRect;
    public RectF deleteRect; // 删除按钮位置
    public RectF rotateRect; // 旋转按钮位置

    public RectF toolBox;
    public Matrix matrix;// 变化矩阵
    public float roatetAngle = 0;
    boolean isShowToolRect = false; //是否显示贴纸的工具框，用来旋转或删除
    private Paint dstPaint = new Paint();
    private Paint paint = new Paint();
    private Paint helpBoxPaint = new Paint();

    private float mInitWidth;// 加入屏幕时原始宽度

    private static Bitmap deleteBit;
    private static Bitmap rotateBit;

    private Paint debugPaint = new Paint();
    public RectF detectRotateRect;

    public RectF detectDeleteRect;

    public StickerItem(Context context) {

        helpBoxPaint.setColor(Color.BLACK);
        helpBoxPaint.setStyle(Paint.Style.STROKE);
        helpBoxPaint.setAntiAlias(true);
        helpBoxPaint.setStrokeWidth(4);

        dstPaint = new Paint();
        dstPaint.setColor(Color.RED);
        dstPaint.setAlpha(120);

        debugPaint = new Paint();
        debugPaint.setColor(Color.GREEN);
        debugPaint.setAlpha(120);

        // 导入工具按钮位图
        if (deleteBit == null) {
            deleteBit = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sticker_delete);
        }
        if (rotateBit == null) {
            rotateBit = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sticker_rotate);
        }
    }

    public void init(Bitmap addBit, View parentView) {
        this.bitmap = addBit;
        this.srcRect = new Rect(0, 0, addBit.getWidth(), addBit.getHeight());
        int bitWidth = Math.min(addBit.getWidth(), parentView.getWidth() >> 1);
        int bitHeight = (int) bitWidth * addBit.getHeight() / addBit.getWidth();
        int left = (parentView.getWidth() >> 1) - (bitWidth >> 1);
        int top = (parentView.getHeight() >> 1) - (bitHeight >> 1);
        this.dstRect = new RectF(left, top, left + bitWidth, top + bitHeight);
        this.matrix = new Matrix();
        this.matrix.postTranslate(this.dstRect.left, this.dstRect.top);
        this.matrix.postScale((float) bitWidth / addBit.getWidth(),
                (float) bitHeight / addBit.getHeight(), this.dstRect.left,
                this.dstRect.top);
        mInitWidth = this.dstRect.width();// 记录原始宽度
        // item.matrix.setScale((float)bitWidth/addBit.getWidth(),
        // (float)bitHeight/addBit.getHeight());
        this.isShowToolRect = true;
        this.toolBox = new RectF(this.dstRect);
        updateToolBoxRect();
        helpToolsRect = new Rect(0, 0, deleteBit.getWidth(),
                deleteBit.getHeight());

        deleteRect = new RectF(toolBox.left - BUTTON_WIDTH, toolBox.top
                - BUTTON_WIDTH, toolBox.left + BUTTON_WIDTH, toolBox.top
                + BUTTON_WIDTH);
        rotateRect = new RectF(toolBox.right - BUTTON_WIDTH, toolBox.bottom
                - BUTTON_WIDTH, toolBox.right + BUTTON_WIDTH, toolBox.bottom
                + BUTTON_WIDTH);

        detectRotateRect = new RectF(rotateRect);
        detectDeleteRect = new RectF(deleteRect);
    }

    private void updateToolBoxRect() {
        this.toolBox.left -= HELP_BOX_PAD;
        this.toolBox.right += HELP_BOX_PAD;
        this.toolBox.top -= HELP_BOX_PAD;
        this.toolBox.bottom += HELP_BOX_PAD;
    }

    /**
     * 位置更新
     */
    public void updatePos(final float dx, final float dy) {
        this.matrix.postTranslate(dx, dy);// 记录到矩阵中
        dstRect.offset(dx, dy);
        toolBox.offset(dx, dy); // 工具按钮随之移动
        deleteRect.offset(dx, dy);
        rotateRect.offset(dx, dy);
        this.detectRotateRect.offset(dx, dy);
        this.detectDeleteRect.offset(dx, dy);
    }

    /**
     * 旋转 缩放 更新
     */
    public void updateRotateAndScale(final float dx, final float dy) {
        // 获得贴纸的中心点坐标
        float centerX = dstRect.centerX();
        float centerY = dstRect.centerY();
        // 获得旋转按钮的中心点坐标
        float rotateBtnX = this.detectRotateRect.centerX();
        float rotateBtnY = this.detectRotateRect.centerY();
        // 当前的旋转按钮中心点坐标
        float curRotateBtnX = rotateBtnX + dx;
        float curRotateBtnY = rotateBtnY + dy;
        // 获得新老位置的旋转点到贴纸中心点的距离
        float xDistance = rotateBtnX - centerX;
        float yDistance = rotateBtnY - centerY;
        float xCurDistance = curRotateBtnX - centerX;
        float yCurDistance = curRotateBtnY - centerY;
        float srcLen = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        float curLen = (float) Math.sqrt(xCurDistance * xCurDistance + yCurDistance * yCurDistance);
        // 计算缩放比
        float scale = curLen / srcLen;
        float newWidth = dstRect.width() * scale;
        if (newWidth / mInitWidth < MIN_SCALE) { // 最小缩放值检测
            return;
        }
        this.matrix.postScale(scale, scale, this.dstRect.centerX(), this.dstRect.centerY()); // 存入scale矩阵
        CommonUtil.scaleRect(this.dstRect, scale); // 缩放目标矩形
        toolBox.set(dstRect); // 重新坐标赋值
        updateToolBoxRect();
        rotateRect.offsetTo(toolBox.right - BUTTON_WIDTH, toolBox.bottom
                - BUTTON_WIDTH);
        deleteRect.offsetTo(toolBox.left - BUTTON_WIDTH, toolBox.top
                - BUTTON_WIDTH);

        detectRotateRect.offsetTo(toolBox.right - BUTTON_WIDTH, toolBox.bottom
                - BUTTON_WIDTH);
        detectDeleteRect.offsetTo(toolBox.left - BUTTON_WIDTH, toolBox.top
                - BUTTON_WIDTH);
        // 通过余弦定理推得余弦值
        double cos = (xDistance * xCurDistance + yDistance * yCurDistance) / (srcLen * curLen);
        if (cos > 1 || cos < -1) {
            return;
        }
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xDistance * yCurDistance - xCurDistance * yDistance; // 确定转动方向
        angle = calMatrix > 0 ? angle : (-1) * angle;
        roatetAngle += angle;
        this.matrix.postRotate(angle, this.dstRect.centerX(), this.dstRect.centerY());
       CommonUtil.rotateRect(this.detectRotateRect, this.dstRect.centerX(),
                this.dstRect.centerY(), roatetAngle);
        CommonUtil.rotateRect(this.detectDeleteRect, this.dstRect.centerX(),
                this.dstRect.centerY(), roatetAngle);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.bitmap, this.matrix, null); //贴图元素绘制
        if (this.isShowToolRect) {// 绘制辅助工具线
            canvas.save();
            canvas.rotate(roatetAngle, toolBox.centerX(), toolBox.centerY());
            canvas.drawRoundRect(toolBox, 10, 10, helpBoxPaint);
            // 绘制工具按钮
            canvas.drawBitmap(deleteBit, helpToolsRect, deleteRect, null);
            canvas.drawBitmap(rotateBit, helpToolsRect, rotateRect, null);
            canvas.restore();
        }
    }
}