package com.mlmobileapps.styletransfer;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Deepesh on 5/21/2017.
 */

public class HorizontalListAdapter extends RecyclerView.Adapter<HorizontalListAdapter.MyViewHolder> {
    private ArrayList<Bitmap> bmList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView riv;

        public MyViewHolder(View view) {
            super(view);

            riv = (ImageView) view.findViewById(R.id.horizontal_item_view_image);
        }
    }


    public HorizontalListAdapter( ArrayList<Bitmap> bmList) {
        this.bmList = bmList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_horizontal_list, parent, false);

        if (itemView.getLayoutParams ().width == RecyclerView.LayoutParams.MATCH_PARENT)
            itemView.getLayoutParams ().width = RecyclerView.LayoutParams.WRAP_CONTENT;

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.riv.setImageBitmap(bmList.get(position));
    }

    @Override
    public int getItemCount() {
        return bmList.size();
    }

}
