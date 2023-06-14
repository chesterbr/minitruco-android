package me.chester.minitruco.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import me.chester.minitruco.R;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Permite ao usuário escolher um baralho (i.e., o desenho da carta fechada).
 * <p>
 * Os nomes e resources das opções são definidos em /res/values/baralhos.xml.
 */
public class BaralhoPreference extends Preference {

    private PreferenceViewHolder holder;
    private String[] nomes;
    private int[] ids;

    public BaralhoPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        carregaNomeseIdsDosBaralhos();
        setWidgetLayoutResource(R.layout.baralho_preference_preview_widget);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        this.holder = holder;
        atualizaPreviewDoBaralhoEscolhido();
    }

    private void carregaNomeseIdsDosBaralhos() {
        nomes = getContext().getResources().getStringArray(R.array.baralhos_nomes);
        ids = new int[nomes.length];
        TypedArray ta = getContext().getResources().obtainTypedArray(R.array.baralhos_ids);
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ta.getResourceId(i, 0);
        }
        ta.recycle();
    }

    private void atualizaPreviewDoBaralhoEscolhido() {
        int indice = getSharedPreferences().getInt("indiceDesenhoCartaFechada", 0);
        TypedArray ta = getContext().getResources().obtainTypedArray(R.array.baralhos_ids);
        int drawable = ta.getResourceId((Integer) indice, 0);
        ImageView preview = (ImageView) holder.findViewById(R.id.imageViewBaralhoPreview);
        if (preview != null && drawable != 0) {
            preview.setImageResource(drawable);
        }
        ta.recycle();
    }

    @Override
    protected void onClick() {
        super.onClick();

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.baralho_preference_lista, null);
        GridView gridView = dialogView.findViewById(R.id.gridViewBaralho);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setTitle("Selecione um baralho")
            .setView(dialogView)
            .create();
        gridView.setAdapter(new BaralhosAdapter(dialog));
        dialog.show();
    }

    /**
     * Gera cada item (view) da lista de baralhos, com o nome e a imagem.
     */
    private class BaralhosAdapter extends ArrayAdapter<String> {
        private final AlertDialog dialog;

        public BaralhosAdapter(AlertDialog dialog) {
            super(BaralhoPreference.this.getContext(), 0, BaralhoPreference.this.nomes);
            this.dialog = dialog;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext()); // or (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewItem = inflater.inflate(R.layout.baralho_preference_item, null);
            viewItem.findViewById(R.id.imagem).setBackgroundResource(ids[position]);
            ((TextView)viewItem.findViewById(R.id.texto)).setText(nomes[position]);
            viewItem.setOnClickListener(v -> {
                PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit()
                    .putInt("indiceDesenhoCartaFechada", position)
                    .apply();
                atualizaPreviewDoBaralhoEscolhido();
                dialog.dismiss();
            });
            return viewItem;
        }
    }
}
