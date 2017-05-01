package br.edu.ffb.pedro.aulap2paluno.adapter.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import br.edu.ffb.pedro.aulap2paluno.R;

public class ProfessorsListViewHolder extends RecyclerView.ViewHolder {

    public TextView professorDeviceReadableName;

    public ProfessorsListViewHolder(View itemView) {
        super(itemView);
        professorDeviceReadableName = (TextView) itemView.findViewById(R.id.professorDeviceReadableName);
    }
}
