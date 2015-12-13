package com.mrsmyx.jmapi.adapters;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mrsmyx.JMAPI;
import com.mrsmyx.jmapi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cj on 10/10/15.
 */
public abstract class SystemAdapter extends RecyclerView.Adapter<SystemAdapter.SystemHolder> {
    List<SystemStruct> systemStructList = new ArrayList<SystemStruct>();

    @Override
    public SystemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SystemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.system_struct, parent, false));
    }

    @Override
    public void onBindViewHolder(SystemHolder holder, int position) {
        SystemStruct systemStruct = getItemAtPosition(position);
        holder.title.setText(systemStruct.getTitle());
    }

    public SystemStruct getItemAtPosition(int position){
        return systemStructList.get(position);
    }

    @Override
    public int getItemCount() {
        return systemStructList.size();
    }
    public abstract void onItemClicked(View view, int position);
    public abstract boolean onItemLongClicked(View view, int position);

    public void appendItem(SystemStruct builder){
        systemStructList.add(builder);
        notifyItemInserted(systemStructList.size());
    }

    public class SystemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public AppCompatTextView title;
        public ImageView imageView;

        public SystemHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.sys_icon);
            title = (AppCompatTextView) itemView.findViewById(R.id.sys_title);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return onItemLongClicked(v, getAdapterPosition());
        }
    }

    public static class SystemStruct {
        public enum PS3OPTIONS{
            SHUTDOWN,
            SOFTBOOT,
            HARDBOOT,
            REBOOT,
            DELHIST,
            DELHISTD,

        }
        private PS3OPTIONS ps3OP;
        private String title;
        private int img;

        public static SystemStruct Builder(){
            return new SystemStruct();
        }

        public PS3OPTIONS getPs3OP() {
            return ps3OP;
        }

        public SystemStruct setPs3OP(PS3OPTIONS ps3OP) {
            this.ps3OP = ps3OP;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public SystemStruct setTitle(String title) {
            this.title = title;
            return this;
        }

        public int getImg() {
            return img;
        }

        public SystemStruct setImg(int img) {
            this.img = img;
            return this;
        }
    }
}
