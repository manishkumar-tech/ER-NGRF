package com.encardio.er_ngrf.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewDataAdapter extends RecyclerView.Adapter<ViewDataAdapter.Viewholder> {

    private final List<ViewDataModel> viewDataModelList;

    public ViewDataAdapter(List<ViewDataModel> viewDataModelList) {
        this.viewDataModelList = viewDataModelList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_data_item, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        holder.setData(viewDataModelList.get(position).getDate(), viewDataModelList.get(position).getParameter());
    }

    @Override
    public int getItemCount() {
        return viewDataModelList.size();
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        private final TextView txt_date;
        private final TextView txt_parameter;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            txt_date = itemView.findViewById(R.id.txt_date);
            txt_parameter = itemView.findViewById(R.id.txt_parameter);
        }

        private void setData(String date, String parameter) {
            txt_date.setText(date);
            txt_parameter.setText(parameter);

        }
    }
}
