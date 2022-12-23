package com.encardio.er_ngrf.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UploadFilesAdapter extends RecyclerView.Adapter<UploadFilesAdapter.Viewholder> {

    private final List<UploadFilesModel> uploadFilesModelList;

    public UploadFilesAdapter(List<UploadFilesModel> uploadFilesModelList) {
        this.uploadFilesModelList = uploadFilesModelList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_data_item, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder holder, int position) {
        holder.setData(position);

        holder.checkBox_isSelected.setChecked(uploadFilesModelList.get(position).isSelected());
        holder.checkBox_isSelected.setText(uploadFilesModelList.get(position).getFileName());

        // holder.checkBox.setTag(R.integer.btnplusview, convertView);
        holder.checkBox_isSelected.setTag(position);
        holder.checkBox_isSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer pos = (Integer) holder.checkBox_isSelected.getTag();

                uploadFilesModelList.get(pos).setSelected(!uploadFilesModelList.get(pos).isSelected());
            }
        });

    }

    @Override
    public int getItemCount() {
        return uploadFilesModelList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {

        private final CheckBox checkBox_isSelected;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            checkBox_isSelected = itemView.findViewById(R.id.checkBox_isSelected);
        }

        private void setData(int position) {
            checkBox_isSelected.setText(uploadFilesModelList.get(position).getFileName());
            checkBox_isSelected.setChecked(uploadFilesModelList.get(position).isSelected());
        }
    }
}
