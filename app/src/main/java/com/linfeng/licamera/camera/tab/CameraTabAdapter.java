package com.linfeng.licamera.camera.tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linfeng.licamera.R;
import com.linfeng.licamera.camera.CameraPresenter;
import com.linfeng.licamera.camera.tab.CameraTabEntity;
import com.linfeng.licamera.camera.tab.CameraTabPresenter;
import com.linfeng.licamera.imageEditor.view.TextStickerView;

import java.util.List;

public class CameraTabAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<CameraTabEntity> mList;
    private CameraTabPresenter mPresenter;

    public CameraTabAdapter(Context context, CameraTabPresenter cameraTabPresenter) {
        mContext = context;
        mList = cameraTabPresenter.getCameraTabList();
        mPresenter = cameraTabPresenter;
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
        ((CameraTabViewHolder)holder).textView.setText(entity.getText());
        ((CameraTabViewHolder)holder).textView.setSelected(false);
        ((CameraTabViewHolder)holder).textView.setOnClickListener(v -> {
            ((CameraTabViewHolder)holder).textView.setSelected(true);
            mPresenter.onCameraTabClick(position, entity.getTabId());
            notifyItemChanged(position, false);
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class CameraTabViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public CameraTabViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.camera_tab_item_text);
        }
    }
}
