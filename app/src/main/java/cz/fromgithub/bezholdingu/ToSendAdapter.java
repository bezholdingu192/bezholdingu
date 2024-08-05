package cz.fromgithub.bezholdingu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ToSendAdapter extends RecyclerView.Adapter<ToSendAdapter.ToSendHolder> {

    public static class ToSendHolder extends RecyclerView.ViewHolder {
        public TextView txtId;
        public TextView txtText1;
        public TextView txtText2;

        public ToSendHolder(View itemView) {
            super(itemView);

            txtId = itemView.findViewById(R.id.txtId);
            txtText1 = itemView.findViewById(R.id.txtText1);
            txtText2 = itemView.findViewById(R.id.txtText2);
        }
    }

    private final List<TosendData> dataKOdeslani;
    private final ToSendActivity cont;

    public ToSendAdapter (List<TosendData> data, ToSendActivity cont){
        this.dataKOdeslani = data;
        this.cont = cont;
    }

    @Override
    public ToSendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_tosend_polozka, parent, false);
        rowItem.setOnClickListener(mItemClick);
        return new ToSendHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(ToSendHolder holder, int position) {
        TosendData data = dataKOdeslani.get(position);
        holder.txtId.setText(String.valueOf(position));
        holder.txtText1.setText(data.date.concat(", kód: ").concat(data.code));
        holder.txtText2.setText(data.desc.replace("\n", " "));
    }

    @Override
    public int getItemCount() {
        return dataKOdeslani.size();
    }


    private View.OnClickListener mItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView txtId = (TextView)v.findViewById(R.id.txtId);
            ToSendActivity.onClickRecyclerItem(Integer.parseInt(txtId.getText().toString()), cont);
        }
    };
}
