package com.linfeng.licamera.imageEditor.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.linfeng.licamera.util.CommonUtil;

import java.util.LinkedHashMap;

/**
 * 贴纸总的View类
 * 负责各个StickerItem的状态管理
 */
public class StickerView extends View {
    private static int STATUS_INIT = 0;
    private static int STATUS_MOVE = 1; // 移动
    private static int STATUS_DELETE = 2; // 删除
    private static int STATUS_ROTATE = 3; // 旋转
    private Context mContext;
    private StickerItem currentItem; //当前操作的StickerItem
    private LinkedHashMap<Integer, StickerItem> bank = new LinkedHashMap<>(); //存储当前贴纸数据
    private Point mPoint = new Point(0 , 0);
    private int imageCount; //已加入照片的数量
    private int currentStatus; //当前状态
    private float oldx, oldy;

    public StickerView(Context context) {
        super(context);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        currentStatus = STATUS_INIT;
    }

    public void addBitImage(final Bitmap bitmap) {
        StickerItem item = new StickerItem(this.getContext());
        item.init(bitmap, this);
        if (currentItem != null) {
            currentItem.isShowToolRect = false;
        }
        bank.put(++imageCount, item);
        this.invalidate(); // 重绘视图
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Integer id : bank.keySet()) {
            StickerItem item = bank.get(id);
            item.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);// 是否向下传递事件标志 true为消耗

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                int deleteId = -1;
                for (Integer id : bank.keySet()) {
                    StickerItem item = bank.get(id);
                    if (item.detectDeleteRect.contains(x, y)) { // 删除模式
                        // ret = true;
                        deleteId = id;
                        currentStatus = STATUS_DELETE;
                    } else if (item.detectRotateRect.contains(x, y)) { // 点击了旋转按钮
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isShowToolRect = false;
                        }
                        currentItem = item;
                        currentItem.isShowToolRect = true;
                        currentStatus = STATUS_ROTATE;
                        oldx = x;
                        oldy = y;
                    } else if (detectInItemContent(item , x , y)) { // 移动模式
                        // 被选中一张贴图
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isShowToolRect = false;
                        }
                        currentItem = item;
                        currentItem.isShowToolRect = true;
                        currentStatus = STATUS_MOVE;
                        oldx = x;
                        oldy = y;
                    }
                }
                if (!ret && currentItem != null && currentStatus == STATUS_INIT) { // 没有贴图被选择
                    currentItem.isShowToolRect = false;
                    currentItem = null;
                    invalidate();
                }
                if (deleteId > 0 && currentStatus == STATUS_DELETE) { // 删除选定贴图
                    bank.remove(deleteId);
                    currentStatus = STATUS_INIT; // 返回空闲状态
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                ret = true;
                if (currentStatus == STATUS_MOVE) {
                    // 获得位移，进行更新
                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updatePos(dx, dy);
                        invalidate();
                    }
                    oldx = x;
                    oldy = y;
                } else if (currentStatus == STATUS_ROTATE) { // 旋转 缩放图片操作
                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updateRotateAndScale(dx, dy);// 旋转
                        invalidate();
                    }
                    oldx = x;
                    oldy = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                ret = false;
                currentStatus = STATUS_INIT;
                break;
        }
        return ret;
    }

    /**
     * 判定点击点是否在内容范围之内  需考虑旋转
     */
    private boolean detectInItemContent(StickerItem item , float x , float y){
        mPoint.set((int)x , (int)y);
        //旋转点击点
        CommonUtil.rotatePoint(mPoint , item.toolBox.centerX() , item.toolBox.centerY() , -item.roatetAngle);
        return item.toolBox.contains(mPoint.x, mPoint.y);
    }

    public LinkedHashMap<Integer, StickerItem> getBank() {
        return bank;
    }

    public void clear() {
        bank.clear();
        this.invalidate();
    }
}
