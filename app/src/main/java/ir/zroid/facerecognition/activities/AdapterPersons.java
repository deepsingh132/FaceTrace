package ir.zroid.facerecognition.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.zroid.facerecognition.R;
import ir.zroid.facerecognition.database.MyDatabase;


public class AdapterPersons extends RecyclerView.Adapter<AdapterPersons.MyViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private List<String> items;
    private MyDatabase db;
    final String root =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "FaceRecognition" + File.separator + "data";

    public AdapterPersons(Context context, List<String> strings) {
        this.context = context;
        this.items = strings;
        this.inflater = LayoutInflater.from(context);
        this.db = new MyDatabase(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_persons, parent, false);
        MyViewHolder VH = new MyViewHolder(v);
        return VH;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        String codeMeli = db.getString("SELECT * FROM persons WHERE id = '" + items.get(position) + "'", "codeMeli");

        holder.tv_firstName.setText(db.getString("SELECT * FROM persons WHERE id = '" + items.get(position) + "'", "firstName") + " " + db.getString("SELECT * FROM persons WHERE id = '" + items.get(position) + "'", "lastName"));
        holder.tv_reason.setText(codeMeli);

        Picasso.get().load(new File(root + File.separator + codeMeli + ".png")).into(holder.img_person);

        holder.cdPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String codeMeli = db.getString("SELECT * FROM persons WHERE id = '" + items.get(position) + "'", "codeMeli");
                Intent intent = new Intent(context, PersonDetailsActivity.class);
                intent.putExtra("codeMeli", codeMeli);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_firstName, tv_reason;
        CircleImageView img_person;
        CardView cdPerson;
        public MyViewHolder(View itemView) {
            super(itemView);
            tv_firstName = (TextView) itemView.findViewById(R.id.tv_firstName);
            img_person = (CircleImageView) itemView.findViewById(R.id.img_person);
            tv_reason = (TextView) itemView.findViewById(R.id.tv_reason);
            cdPerson = (CardView)itemView.findViewById(R.id.cdPerson);
        }
    }
}
