package com.linfeng.licamera.videoEditor.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.linfeng.licamera.videoEditor.IVideoSurface;
import com.linfeng.licamera.videoEditor.VideoGlRender;
import com.linfeng.licamera.videoEditor.filter.GlFilter;

public class GlVideoView extends GLSurfaceView {

    private VideoGlRender mRenderer;

    private Context mContext;

    public GlVideoView(Context context) {
        super(context);
        init(context,null);
    }

    public GlVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (!supportsOpenGLES2(context)) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        }

        mContext = context;

    }

    public void init(IVideoSurface videoSurface){
        GlFilter filter = new GlFilter();
        mRenderer = new VideoGlRender(filter, videoSurface);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    /**
     * Checks if OpenGL ES 2.0 is supported on the current device.
     *
     * @param context the context
     * @return true, if successful
     */
    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }


    /**
     * Set the filter to be applied on the image.
     *
     * @param filter Filter that should be applied on the image.
     */
    public void setFilter(GlFilter filter) {
        mRenderer.setFilter(filter);
    }

    /**
     * Get the current applied filter.
     *
     * @return the current filter
     */
    public GlFilter getFilter() {
        return mRenderer.getFilter();
    }

}
