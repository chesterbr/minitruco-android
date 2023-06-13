package me.chester.minitruco.android;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import me.chester.minitruco.R;

public class EscolheBaralhoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.escolhe_baralho);

        String[] nomes = getResources().getStringArray(R.array.baralhos_nomes);
        int[] ids = new int[nomes.length];
        TypedArray ta = getResources().obtainTypedArray(R.array.baralhos_ids);
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ta.getResourceId(i, 0);
        }
        ta.recycle();

        ListView listView = findViewById(R.id.listViewBaralho);
        listView.setAdapter(new ArrayAdapter<String>(this, 0, nomes) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(EscolheBaralhoActivity.this); // or (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View viewItem = inflater.inflate(R.layout.item_escolhe_baralho, null);
                viewItem.findViewById(R.id.imagem).setBackgroundResource(ids[position]);
                ((TextView)viewItem.findViewById(R.id.texto)).setText(nomes[position]);
                viewItem.setOnClickListener(v -> {
                    PreferenceManager.getDefaultSharedPreferences(EscolheBaralhoActivity.this)
                        .edit()
                        .putInt("indiceDesenhoCartaFechada", position)
                        .apply();
                    finish();
                });
                return viewItem;
            }
        });
    }
}
