package me.chester.minitruco.android.multiplayer.bluetooth;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.chester.minitruco.R;
import me.chester.minitruco.android.CriadorDePartida;
import me.chester.minitruco.android.SalaActivity;
import me.chester.minitruco.android.TrucoActivity;
import me.chester.minitruco.core.Partida;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Activity que atua como cliente ou servidor Bluetooth
 * <p>
 * Esta classe consolida as partes comuns a ambos: exibir os nomes dos jogadores,
 * garantir que o Bluetooth está ligado e as permissões cedidas, etc.
 */
public abstract class BluetoothActivity extends SalaActivity implements
        Runnable {

    public static String[] BLUETOOTH_PERMISSIONS;
    static {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            // Android 11 ou anterior pede permissões genéricas
            BLUETOOTH_PERMISSIONS = new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
            };
        } else {
            // Android 12 ou superior pede permissões mais refinadas
            BLUETOOTH_PERMISSIONS = new String[] {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
            };
        }
    }

    /**
     * Separador de linha recebido
     */
    public static final int SEPARADOR_REC = '*';

    /**
     * Separador de linha enviado (tanto no sentido client-server quanto no
     * server-client).
     * <p>
     * É propositalmente um conjunto de SEPARADOR_REC, para garantir que o
     * recebimento seja detectado (linhas em branco são ignoradas de qualquer
     * forma).
     */
    public static final byte[] SEPARADOR_ENV = "**".getBytes();

    /**
     * Identificadores Bluetooth do "serviço miniTruco"
     */
    public static final String NOME_BT = "miniTruco";
    public static final UUID UUID_BT = UUID
            .fromString("3B175368-ABB4-11DB-A508-C2B155D89593");

    protected BluetoothAdapter btAdapter;
    protected final String[] apelidos = new String[4];
    protected String modo;
    protected Button btnIniciar;
    protected Button btnInverter;
    protected Button btnTrocar;
    protected View layoutIniciar;
    private TextView textViewMensagem;
    private TextView textViewStatus;
    private TextView[] textViewsJogadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CriadorDePartida.setActivitySala(this);
        setContentView(R.layout.sala);
        layoutIniciar = findViewById(R.id.layoutIniciar);
        btnIniciar = findViewById(R.id.btnIniciarBluetooth);
        btnInverter = findViewById(R.id.btnInverter);
        btnTrocar = findViewById(R.id.btnTrocar);
        textViewMensagem = findViewById(R.id.textViewMensagem);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewsJogadores = new TextView[4];
        textViewsJogadores[0] = findViewById(R.id.textViewJogador1);
        textViewsJogadores[1] = findViewById(R.id.textViewJogador2);
        textViewsJogadores[2] = findViewById(R.id.textViewJogador3);
        textViewsJogadores[3] = findViewById(R.id.textViewJogador4);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        String[] permissoesFaltantes = permissoesBluetoothFaltantes();
        if (permissoesFaltantes.length == 0) {
            iniciaAtividadeBluetooth();
        } else {
            permissionsLauncher.launch(permissoesFaltantes);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CriadorDePartida.setActivitySala(this);
    }

    private String[] permissoesBluetoothFaltantes() {
        // Antes do Android 6, permissões eram declaradas no manifest, e a app simplesmente
        // assumia que foi autorizada. A vida era simples. Eu sinto falta disso.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new String[0];
        }
        // Versões mais novas pedem permissões de runtime, então começa a dança da manivela:
        List<String> permissoes = new ArrayList<>();
        for (String permission: BLUETOOTH_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissoes.add(permission);
            }
        }
        return permissoes.toArray(new String[0]);
    }


    final ActivityResultLauncher<String[]> permissionsLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    String[] permissoesFaltantes = permissoesBluetoothFaltantes();
                    if (permissoesFaltantes.length == 0) {
                        iniciaAtividadeBluetooth();
                    } else {
                        for (int i = 0; i < permissoesFaltantes.length; i++) {
                            permissoesFaltantes[i] = permissoesFaltantes[i].replace("android.permission.", "");
                        }
                        msgErroFatal("Não foi possivel obter permissões: " +
                                     Arrays.toString(permissoesFaltantes) + ".\n\n" +
                                     "Se o problema persistir, tente autorizar "+
                                     "nas configurações do celular (em \"Aplicativos\"), " +
                                     "ou desinstalar e reinstalar o jogo.");
                    }
                });

    /**
     * As atividades de Bluetooth são vão iniciar quando as permissões estiverem garantidas,
     * através da chamada deste método
     */
    abstract void iniciaAtividadeBluetooth();

    protected void atualizaDisplay() {
        runOnUiThread(() -> {
            for (int i = 0; i < 4; i++) {
                textViewsJogadores[i].setText(apelidos[i]);
            }
            if (modo != null) {
                textViewStatus.setText("Modo: " + Partida.textoModo(modo));
            }
            btnIniciar.setEnabled(getNumClientes() > 0);
            btnInverter.setEnabled(getNumClientes() > 0);
            btnTrocar.setEnabled(getNumClientes() > 0);
        });
    }

    protected void setMensagem(String mensagem) {
        runOnUiThread(() -> {
            if (mensagem == null) {
                textViewMensagem.setVisibility(View.GONE);
            } else {
                textViewMensagem.setVisibility(View.VISIBLE);
                textViewMensagem.setText(mensagem);
            }
        });
    }

    protected void msgErroFatal(String mensagem) {
        runOnUiThread(() -> {
            encerraTrucoActivity();
            BluetoothActivity context = BluetoothActivity.this;
            if (context == null || context.isFinishing()) {
                return;
            }
            new AlertDialog.Builder(context)
                .setTitle("Erro")
                .setMessage(mensagem)
                .setOnCancelListener(dialog -> finish())
                .setNeutralButton("Ok",
                    (dialog, which) -> finish())
                .show();
        });
    }

    protected abstract int getNumClientes();

    protected void iniciaTrucoActivitySePreciso() {
        if (!TrucoActivity.isViva()) {
            startActivity(
                new Intent(this, TrucoActivity.class)
                    .putExtra("multiplayer", true));
        }
    }

    protected void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // não precisa tratar
        }
    }

}
