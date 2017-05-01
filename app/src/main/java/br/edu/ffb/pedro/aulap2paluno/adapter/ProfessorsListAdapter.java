package br.edu.ffb.pedro.aulap2paluno.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import br.edu.ffb.pedro.aulap2paluno.R;
import br.edu.ffb.pedro.aulap2paluno.activity.LoginActivity;
import br.edu.ffb.pedro.aulap2paluno.adapter.holders.ProfessorsListViewHolder;
import br.edu.ffb.pedro.bullyelectionp2p.BullyElectionP2pDevice;

public class ProfessorsListAdapter extends RecyclerView.Adapter {

    private ArrayList<BullyElectionP2pDevice> professorsDevice;
    private Context context;

    public ProfessorsListAdapter(ArrayList<BullyElectionP2pDevice> professorsDevice, Context context) {
        this.professorsDevice = professorsDevice;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_professor, parent, false);
        return new ProfessorsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ProfessorsListViewHolder professorsListViewHolder = (ProfessorsListViewHolder) holder;
        final BullyElectionP2pDevice professorDevice = professorsDevice.get(position);
        professorsListViewHolder.professorDeviceReadableName.setText(professorDevice.readableName);

        professorsListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) context).onProfessorsListItemClick(professorDevice);
            }
        });
    }


    @Override
    public int getItemCount() {
        return professorsDevice.size();
    }
}
