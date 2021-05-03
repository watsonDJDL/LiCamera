package com.linfeng.licamera.imageEditor.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.linfeng.licamera.R;
import com.linfeng.licamera.util.PaintUtil;

/**
 * 裁剪图片View
 */
public class CropImageView extends View {
    private static final int LEFT_TOP_POINT = 1;
    private static final int RIGHT_TOP_POINT = 2;
    private static final int LEFT_BOTTOM_POINT = 3;
    private static final int RIGHT_BOTTOM_POINT = 4;
    private static int STATUS_IDLE = 1;// 空闲状态
    private static int STATUS_MOVE = 2;// 移动状态
    private static int STATUS_SCALE = 3;// 缩放状态
    private int CIRCLE_WIDTH = 46;
    private Context mContext;
    private float oldx, oldy;
    private int status = STATUS_IDLE;
    private int selectedControllerCicle;
    private RectF backUpRect = new RectF();// 上
    private RectF backLeftRect = new RectF();// 左
    private RectF backRightRect = new RectF();// 右
    private RectF backDownRect = new RectF();// 下
    private RectF cropRect = new RectF();// 剪切矩形
    private Paint mBackgroundPaint;// 背景Paint
    private Bitmap circleBit;
    private Rect circleRect = new Rect();
    private RectF leftTopCircleRect;
    private RectF rightTopCircleRect;
    private RectF leftBottomRect;
    private RectF rightBottomRect;
    private RectF imageRect = new RectF();// 存贮图片位置信息
    private RectF tempRect = new RectF();// 临时存贮矩形数据
    private float ratio = -1;// 剪裁缩放比率

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mBackgroundPaint = PaintUtil.newBackgroundPaint();
        circleBit = BitmapFactory.decodeResource(context.getResources(), R.drawable.sticker_rotate);
        circleRect.set(0, 0, circleBit.getWidth(), circleBit.getHeight());
        leftTopCircleRect = new RectF(0, 0, CIRCLE_WIDTH, CIRCLE_WIDTH);
        rightTopCircleRect = new RectF(leftTopCircleRect);
        leftBottomRect = new RectF(leftTopCircleRect);
        rightBottomRect = new RectF(leftTopCircleRect);
    }

    /**
     * 重置剪裁面
     */
    public void setCropRect(RectF rect) {
        if(rect == null)
            return;
        imageRect.set(rect);
        cropRect.set(rect);
        scaleRect(cropRect, 0.5f);
        invalidate();
    }

    public void setRatioCropRect(RectF rect, float r) {
        this.ratio = r;
        if (r < 0) {
            setCropRect(rect);
            return;
        }
        imageRect.set(rect);
        cropRect.set(rect);
        float h, w;
        if (cropRect.width() >= cropRect.height()) {
            h = cropRect.height() / 2;
            w = this.ratio * h;
        } else {
            w = rect.width() / 2;
            h = w / this.ratio;
        }
        float scaleX = w / cropRect.width();
        float scaleY = h / cropRect.height();
        scaleRect(cropRect, scaleX, scaleY);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0)
            return;
        // 绘制黑色背景
        backUpRect.set(0, 0, w, cropRect.top);
        backLeftRect.set(0, cropRect.top, cropRect.left, cropRect.bottom);
        backRightRect.set(cropRect.right, cropRect.top, w, cropRect.bottom);
        backDownRect.set(0, cropRect.bottom, w, h);

        canvas.drawRect(backUpRect, mBackgroundPaint);
        canvas.drawRect(backLeftRect, mBackgroundPaint);
        canvas.drawRect(backRightRect, mBackgroundPaint);
        canvas.drawRect(backDownRect, mBackgroundPaint);

        // 绘制四个控制点
        int radius = CIRCLE_WIDTH >> 1;
        leftTopCircleRect.set(cropRect.left - radius, cropRect.top - radius,
                cropRect.left + radius, cropRect.top + radius);
        rightTopCircleRect.set(cropRect.right - radius, cropRect.top - radius,
                cropRect.right + radius, cropRect.top + radius);
        leftBottomRect.set(cropRect.left - radius, cropRect.bottom - radius,
                cropRect.left + radius, cropRect.bottom + radius);
        rightBottomRect.set(cropRect.right - radius, cropRect.bottom - radius,
                cropRect.right + radius, cropRect.bottom + radius);

        canvas.drawBitmap(circleBit, circleRect, leftTopCircleRect, null);
        canvas.drawBitmap(circleBit, circleRect, rightTopCircleRect, null);
        canvas.drawBitmap(circleBit, circleRect, leftBottomRect, null);
        canvas.drawBitmap(circleBit, circleRect, rightBottomRect, null);
    }

    /**
     * 触摸事件处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);// 是否向下传递事件标志 true为消耗
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int selectCircle = isSelectedControllerCircle(x, y);
                if (selectCircle > 0) {// 选择控制点
                    ret = true;
                    selectedControllerCicle = selectCircle;// 记录选中控制点编号
                    status = STATUS_SCALE;// 进入缩放状态
                } else if (cropRect.contains(x, y)) {// 选择缩放框内部
                    ret = true;
                    status = STATUS_MOVE;// 进入移动状态
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (status == STATUS_SCALE) {// 缩放
                    scaleCropController(x, y);
                } else if (status == STATUS_MOVE) {// 移动
                    translateCrop(x - oldx, y - oldy);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                status = STATUS_IDLE;// 回归空闲状态
                break;
        }
        oldx = x;
        oldy = y;
        return ret;
    }

    /**
     * 移动剪切框
     */
    private void translateCrop(float dx, float dy) {
        tempRect.set(cropRect);// 存贮原有数据，以便还原

        translateRect(cropRect, dx, dy);
        // 边界判定算法优化
        float mdLeft = imageRect.left - cropRect.left;
        if (mdLeft > 0) {
            translateRect(cropRect, mdLeft, 0);
        }
        float mdRight = imageRect.right - cropRect.right;
        if (mdRight < 0) {
            translateRect(cropRect, mdRight, 0);
        }
        float mdTop = imageRect.top - cropRect.top;
        if (mdTop > 0) {
            translateRect(cropRect, 0, mdTop);
        }
        float mdBottom = imageRect.bottom - cropRect.bottom;
        if (mdBottom < 0) {
            translateRect(cropRect, 0, mdBottom);
        }

        this.invalidate();
    }

    /**
     * 移动矩形
     */
    private static void translateRect(RectF rect, float dx, float dy) {
        rect.left += dx;
        rect.right += dx;
        rect.top += dy;
        rect.bottom += dy;
    }

    /**
     * 操作控制点 控制缩放
     */
    private void scaleCropController(float x, float y) {
        tempRect.set(cropRect);
        switch (selectedControllerCicle) {
            case 1:// 左上角控制点
                cropRect.left = x;
                cropRect.top = y;
                break;
            case 2:// 右上角控制点
                cropRect.right = x;
                cropRect.top = y;
                break;
            case 3:// 左下角控制点
                cropRect.left = x;
                cropRect.bottom = y;
                break;
            case 4:// 右下角控制点
                cropRect.right = x;
                cropRect.bottom = y;
                break;
        }
        if (ratio < 0) {// 任意缩放比
            // 边界条件检测
            validateCropRect();
            invalidate();
        } else if (cropRect.left < imageRect.left
                    || cropRect.right > imageRect.right
                    || cropRect.top < imageRect.top
                    || cropRect.bottom > imageRect.bottom
                    || cropRect.width() < CIRCLE_WIDTH
                    || cropRect.height() < CIRCLE_WIDTH) {
                cropRect.set(tempRect);

            invalidate();
        }
    }

    /**
     * 边界条件检测
     */
    private void validateCropRect() {
        if (cropRect.width() < CIRCLE_WIDTH) {
            cropRect.left = tempRect.left;
            cropRect.right = tempRect.right;
        }
        if (cropRect.height() < CIRCLE_WIDTH) {
            cropRect.top = tempRect.top;
            cropRect.bottom = tempRect.bottom;
        }
        if (cropRect.left < imageRect.left) {
            cropRect.left = imageRect.left;
        }
        if (cropRect.right > imageRect.right) {
            cropRect.right = imageRect.right;
        }
        if (cropRect.top < imageRect.top) {
            cropRect.top = imageRect.top;
        }
        if (cropRect.bottom > imageRect.bottom) {
            cropRect.bottom = imageRect.bottom;
        }
    }

    /**
     * 是否选中控制点
     */
    private int isSelectedControllerCircle(float x, float y) {
        if (leftTopCircleRect.contains(x, y))// 选中左上角
            return LEFT_TOP_POINT;
        if (rightTopCircleRect.contains(x, y))// 选中右上角
            return RIGHT_TOP_POINT;
        if (leftBottomRect.contains(x, y))// 选中左下角
            return LEFT_BOTTOM_POINT;
        if (rightBottomRect.contains(x, y))// 选中右下角
            return RIGHT_BOTTOM_POINT;
        return -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 返回剪切矩形
     */
    public RectF getCropRect() {
        return new RectF(this.cropRect);
    }

    /**
     * 缩放指定矩形
     */
    private static void scaleRect(RectF rect, float scaleX, float scaleY) {
        float w = rect.width();
        float h = rect.height();
        float newW = scaleX * w;
        float newH = scaleY * h;
        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;
        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    /**
     * 缩放指定矩形
     */
    private static void scaleRect(RectF rect, float scale) {
        scaleRect(rect, scale, scale);
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

}
