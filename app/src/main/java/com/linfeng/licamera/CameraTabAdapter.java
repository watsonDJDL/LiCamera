package com.linfeng.licamera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linfeng.licamera.camera.CameraPresenter;
import com.linfeng.licamera.camera.CameraTabPresenter;

import java.util.List;

public class CameraTabAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<CameraTabEntity> mList;
    private CameraTabPresenter mPresenter;

    public CameraTabAdapter(Context context, CameraPresenter cameraPresenter) {
        mContext = context;
        mList = cameraPresenter.getCameraTabList();
        mPresenter = cameraPresenter.getCameraTabPresenter();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.camera_tab_item, parent, false);
        return new CameraTabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CameraTabEntity entity = mList.get(position);
        ((CameraTabViewHolder)holder).button.setText(entity.getText());
        ((CameraTabViewHolder)holder).button.setOnClickListener(v -> mPresenter.onCameraTabChanged(entity.getTabId()));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class CameraTabViewHolder extends RecyclerView.ViewHolder {
        public Button button;

        public CameraTabViewHolder(@NonNull View itemView) {
            super(itemView);
            button = (Button) itemView.findViewById(R.id.camera_tab_item_text);
        }
    }
}
