package com.linfeng.licamera.imageEditor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.linfeng.licamera.R;
import com.linfeng.licamera.imageEditor.fragment.StickerFragment;

public class StickerTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int[] typeIcon = {R.drawable.stickers_type_animal,
            R.drawable.stickers_type_motion, R.drawable.stickers_type_cos,
            R.drawable.stickers_type_mark, R.drawable.stickers_type_decoration};
    public static final String[] stickerPath = {"stickers/type1", "stickers/type2", "stickers/type3", "stickers/type4", "stickers/type5", "stickers/type6"};
    public static final String[] stickerPathName = {"表情1", "表情2", "表情3", "表情4", "表情5", "表情6"};
    private StickerFragment mStickerFragment;
    private ImageClick mImageClick = new ImageClick();

    public StickerTypeAdapter(StickerFragment fragment) {
        super();
        this.mStickerFragment = fragment;
    }

    public class ImageHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView text;

        public ImageHolder(View itemView) {
            super(itemView);
            this.icon = (ImageView) itemView.findViewById(R.id.icon);
            this.text = (TextView) itemView.findViewById(R.id.text);
        }
    }// end inner class

    @Override
    public int getItemCount() {
        return stickerPathName.length;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewtype) {
        View v = null;
        v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_sticker_type_item, parent, false);
        ImageHolder holer = new ImageHolder(v);
        return holer;
    }

    /**
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ImageHolder imageHoler = (ImageHolder) holder;
        // imageHoler.icon.setImageResource(R.drawable.ic_launcher);
        String name = stickerPathName[position];
        imageHoler.text.setText(name);
        // TODO
        //imageHoler.icon.setImageResource(typeIcon[position]);
        imageHoler.text.setTag(stickerPath[position]);
        imageHoler.text.setOnClickListener(mImageClick);
    }

    /**
     * 选择贴图类型
     */
    private final class ImageClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String data = (String) v.getTag();
            // System.out.println("data---->" + data);
            mStickerFragment.swipToStickerDetails(data);
        }
    }// end inner class
}