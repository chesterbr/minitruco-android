package me.chester.minitruco.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.Random;
import java.util.Vector;

import me.chester.minitruco.R;
import me.chester.minitruco.core.Carta;
import me.chester.minitruco.core.Jogador;
import me.chester.minitruco.core.Jogo;

/* SPDX-License-Identifier: BSD-3-Clause */
/* Copyright © 2005-2023 Carlos Duarte do Nascimento "Chester" <cd@pobox.com> */

/**
 * Representa visualmente o andamento de um jogo, permitindo que o usuário
 * interaja.
 * <p>
 * Para simplificar o acesso, alguns métodos/propriedades são static - o que só
 * reitera que só deve existir uma instância desta View.
 * <p>
 */
public class MesaView extends View {

    public static final int FPS_ANIMANDO = 60;
    public static final int FPS_PARADO = 4;
    private final Paint paintPergunta = new Paint();
    protected int velocidade;
    private int posicaoVez;

    private int valorMao;

    private final float density = getResources().getDisplayMetrics().density;

    private static final Random rand = new Random();
    private int corFundoCartaBalao = Color.WHITE;
    private final Paint paintIconesRodadas = new Paint();

    public MesaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MesaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MesaView(Context context) {
        super(context);
    }

    public void setTrucoActivity(TrucoActivity trucoActivity) {
        this.trucoActivity = trucoActivity;
    }

    /**
     * Informa à mesa que uma animação começou (garantindo refreshes da tela
     * enquanto ela durar).
     *
     * @param fim
     *            timestamp de quando a animação vai acabar
     */
    public void notificaAnimacao(long fim) {
        if (animandoAte < fim) {
            animandoAte = fim;
        }
        threadAnimacao.interrupt(); // Hora de acordar e subir o frame rate!
    }

    /**
     * Coloca a thread chamante em sleep até que as animações acabem.
     */
    public void aguardaFimAnimacoes() {
        long milisecAteFimAnimacao;
        while ((milisecAteFimAnimacao = animandoAte
                - System.currentTimeMillis()) > 0) {
            try {
                Thread.sleep(milisecAteFimAnimacao);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Ajusta o tamanho das cartas e sua posição de acordo com a resolução.
     * <p>
     * Na primeira chamada, incializa as cartas e dá início à partida (tem que
     * ser feito aqui porque não é uma boa idéia começar sem saber onde as
     * coisas vão aparecer)
     */
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {

        // Ajusta o tamanho da carta (tudo depende dele) ao da mesa e os
        // "pontos de referência" importantes (baralho decorativo, tamanho do
        // texto, etc.)
        CartaVisual.ajustaTamanho(w, h);
        leftBaralho = w - CartaVisual.largura - MARGEM - 4;
        topBaralho = MARGEM + 4;
        tamanhoFonte = 12.0f * (h / 270.0f);

        // Na primeira chamada (inicialização), instanciamos as cartas
        if (!inicializada) {
            for (int i = 0; i < cartas.length; i++) {
                cartas[i] = new CartaVisual(this, leftBaralho, topBaralho, null, corFundoCartaBalao, getResources());
                cartas[i].movePara(leftBaralho, topBaralho);
            }
            cartas[0].visible = false;
        }

        // Define posição e tamanho da caixa de diálogo e seus botões
        int alturaDialog = CartaVisual.altura;
        int larguraDialog = CartaVisual.largura * 3;
        int topDialog = (h - alturaDialog) / 2;
        int leftDialog = (w - larguraDialog) / 2;
        rectDialog = new Rect(leftDialog, topDialog,
                leftDialog + larguraDialog, topDialog + alturaDialog);
        int alturaBotao = (int) (tamanhoFonte * 0.6f);
        rectBotaoSim = new RectF(leftDialog + 8, topDialog + alturaDialog
                - alturaBotao - 32, leftDialog + larguraDialog / 2 - 8,
                topDialog + alturaDialog - 16);
        rectBotaoNao = new RectF(leftDialog + larguraDialog / 2 + 8,
                rectBotaoSim.top, leftDialog + larguraDialog - 8,
                rectBotaoSim.bottom);

        // Posiciona o vira e as cartas decorativas do baralho, que são fixos
        cartas[0].movePara(leftBaralho, topBaralho);
        cartas[1].movePara(leftBaralho + 4, topBaralho + 4);
        cartas[2].movePara(leftBaralho + 2, topBaralho + 2);
        cartas[3].movePara(leftBaralho, topBaralho);

        if (!inicializada) {
            // Inicia as threads internas que cuidam de animações e de responder
            // a diálogos e faz a activity começar o jogo
            threadAnimacao.start();
            respondeDialogos.start();
            if (this.trucoActivity != null) {
                this.trucoActivity.criaEIniciaNovoJogo();
            }
        } else {
            // Rolou um resize, reposiciona as cartas não-decorativas
            for (int i = 0; i <= 15; i++) {
                CartaVisual cv = cartas[i];
                if (cv != null) {
                    cv.resetBitmap();
                    if (i >= 4) {
                        int numJogador = (i - 1) / 3;
                        if (trucoActivity.jogo.jogoFinalizado) {
                            cv.movePara(leftBaralho, topBaralho);
                        } else if (cv.descartada) {
                            cv.movePara(calcPosLeftDescartada(numJogador),
                                    calcPosTopDescartada(numJogador));
                        } else {
                            int pos = (i - 1) % 3;
                            cv.movePara(calcPosLeftCarta(numJogador, pos),
                                    calcPosTopCarta(numJogador, pos));
                        }
                    }
                }
            }
        }

        // Se o tamanho da tela mudou (ex.: rotação), precisamos recalcular
        // estes bitmaps
        int lado = CartaVisual.altura / 2;
        iconesRodadas = new Bitmap[23];
        iconesRodadas[0] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.placarrodada0)).getBitmap(), lado, lado, true);
        iconesRodadas[1] =  Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.placarrodada1)).getBitmap(), lado, lado, true);
        iconesRodadas[2] =  Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.placarrodada2)).getBitmap(), lado, lado, true);
        iconesRodadas[3] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.placarrodada3)).getBitmap(), lado, lado, true);
        // indice = valorMao + 10
        iconesRodadas[10] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.placarrodada0)).getBitmap(), lado, lado, true);
        iconesRodadas[11] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.valorrodada1)).getBitmap(), lado, lado, true);
        iconesRodadas[12] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.valorrodada2)).getBitmap(), lado, lado, true);
        iconesRodadas[13] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.valorrodada3)).getBitmap(), lado, lado, true);
        iconesRodadas[14] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.valorrodada4)).getBitmap(), lado, lado, true);
        iconesRodadas[16] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.valorrodada6)).getBitmap(), lado, lado, true);
        iconesRodadas[19] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.valorrodada9)).getBitmap(), lado, lado, true);
        iconesRodadas[20] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.valorrodada10)).getBitmap(), lado, lado, true);
        iconesRodadas[22] = Bitmap.createScaledBitmap(((BitmapDrawable) ContextCompat
            .getDrawable(getContext(), R.drawable.valorrodada12)).getBitmap(), lado, lado, true);

        if (!inicializada && isInEditMode()) {
            velocidade = 4;
            distribuiMao();
        }

        inicializada = true;
}

    /**
     * Recupera a carta visual correspondente a uma carta do jogo.
     *
     * @param c
     *            carta do jogo
     * @return Carta visual com o valor desta, ou <code>null</code> se não achar
     */
    public CartaVisual getCartaVisual(Carta c) {
        for (CartaVisual cv : cartas) {
            if (c != null && c.equals(cv)) {
                return cv;
            }
        }
        return null;
    }

    /**
     * Atualiza o resultado de uma rodada, destacando a carta vencedora e
     * piscando a rodada atual por um instante.
     *
     * @param numRodada
     *            rodada que finalizou
     * @param resultado
     *            (0 a 3, vide {@link #resultadoRodada}
     * @param jogadorQueTorna
     *            jogador cuja carta venceu a rodada
     */
    public void atualizaResultadoRodada(int numRodada, int resultado,
            Jogador jogadorQueTorna) {
        aguardaFimAnimacoes();
        if (resultado != 3) {
            cartaQueFez = getCartaVisual(trucoActivity.jogo
                    .getCartasDaRodada(numRodada)[jogadorQueTorna.getPosicao() - 1]);
            cartaQueFez.destacada = true;
        }
        for (CartaVisual c : cartas) {
            c.escura = c.descartada;
        }
        resultadoRodada[numRodada - 1] = resultado;
        numRodadaPiscando = numRodada;
        rodadaPiscaAte = System.currentTimeMillis() + 1600;
        notificaAnimacao(rodadaPiscaAte);
    }

    /**
     * Torna as cartas da mão de 10/11 visíveis
     *
     * @param cartasParceiro
     *            cartas do seu parceiro
     */
    public void mostraCartasMaoDeX(Carta[] cartasParceiro) {
        for (int i = 0; i <= 2; i++) {
            cartas[10 + i].copiaCarta(cartasParceiro[i]);
        }
    }

    /**
     * Faz com que o balão mostre uma frase por um tempo para um jogador.
     * <p>
     * As frases estão no strings.xml no formato balao_<chave>, e são arrays de
     * strings (das quais uma será sorteada para exibição).
     *
     * @param chave
     *            diz o tipo de texto que aparece no balão. Ex.: "aumento_3"
     *            para pedido de truco.
     * @param posicao
     *            posição (1 a 4) do jogador que "dirá" a frase
     * @param tempoMS
     *            tempo em que ela aparecerá (reduzido se a velocidade das animações for > 1)
     * @param rndFrase
     *              Número "grande" que identifica a frase do strings.xml dita
     *              pelo jogador (índice_da_frase = rndFrase % frases.length())
     */
    public void diz(String chave, int posicao, int tempoMS, int rndFrase) {
        aguardaFimAnimacoes();
        mostraBalaoAte = System.currentTimeMillis() + tempoMS / Math.min(velocidade, 2);
        Resources res = getResources();
        String[] frasesBalao = res.getStringArray(res.getIdentifier("balao_"
                + chave, "array", "me.chester.minitruco"));
        fraseBalao = frasesBalao[rndFrase % frasesBalao.length];
        posicaoBalao = posicao;
        notificaAnimacao(mostraBalaoAte);
    }

    /**
     * Verifica qual elemento foi tocado (ex.: uma das cartas do jogador ou um
     * dos botões) e executa a ação associada a ele.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            return true;
        case MotionEvent.ACTION_UP:
            if (rectBotaoSim.contains((int) event.getX(), (int) event.getY())) {
                respondePergunta(true);
            }
            if (rectBotaoNao.contains((int) event.getX(), (int) event.getY())) {
                respondePergunta(false);
            }
            // Verificamos primeiro a carta mais à direita porque ela é desenhada
            // em cima da do meio, e esta em cima da carta à esquerda
            for (int i = 6; i >= 4; i--) {
                if (cartas[i].isDentro(event.getX(), event.getY())) {
                    jogaCarta(i - 4);
                }
            }
            return true;
        default:
            return super.onTouchEvent(event);
        }
    }

    /**
     * Responde pergunta em exibição (aceita truco, aceita mão de 10/11, etc.)
     * e oculta a pergunta, desde que uma pergunta esteja sendo exibida.
     *
     * @param resposta resposta do jogador (true=sim, false=não)
     */
    public void respondePergunta(boolean resposta) {
        if (mostrarPerguntaAumento) {
            mostrarPerguntaAumento = false;
            if (resposta) {
                aceitarAumento = true;
            } else {
                recusarAumento = true;
            }
        } else if (mostrarPerguntaMaoDeX) {
            mostrarPerguntaMaoDeX = false;
            if (resposta) {
                aceitarMaoDeX = true;
            } else {
                recusarMaoDeX = true;
            }
        }
    }

    /**
     * Joga a carta na posição indicada, desde que seja a vez do jogador humano
     * e a carta não tenha ainda sido descartada.
     *
     * @param posicao
     *            posição da carta na mão do jogador (0 a 2)
     */
    public void jogaCarta(int posicao) {
        CartaVisual carta = cartas[posicao + 4];
        if (carta.descartada) return;
        if (statusVez != STATUS_VEZ_HUMANO_OK) return;

        statusVez = STATUS_VEZ_OUTRO;
        carta.setFechada(vaiJogarFechada);
        trucoActivity.jogo.jogaCarta(
            trucoActivity.jogadorHumano, carta);
    }

    private TrucoActivity trucoActivity;

    private Rect rectDialog;

    private RectF rectBotaoSim;

    private RectF rectBotaoNao;

    private float tamanhoFonte;

    /**
     * Cartas que estão na mesa, na ordem de empilhamento. cartas[0] é o vira,
     * cartas[1..3] são o baralho decorativo, cartas[4..6] são as do jogador na
     * posição 1 (inferior), cartas[7..9] o jogador 2 e assim por diante para os
     * jogadores 3 e 4.
     * <p>
     * TODO: refatorar esses magic numbers para algo melhor.
     */
    public final CartaVisual[] cartas = new CartaVisual[16];

    /**
     * É true se a view já está pronta para responder a solicitações do jogo
     * (mover cartas, acionar balões, etc)
     */
    private boolean inicializada = false;

    private long calcTempoAteFimAnimacaoMS() {
        return animandoAte - System.currentTimeMillis();
    }

    /**
     * Thread/runnable que faz as animações acontecerem (invalidando
     * o display -> forçando um redraw várias vezes por segundo)
     * <p>
     */
    final Thread threadAnimacao = new Thread(new Runnable() {

        public void run() {
            int tempoEntreFramesAnimando = 1000 / FPS_ANIMANDO;
            int tempoEntreFramesParado = 1000 / FPS_PARADO;
            // Aguarda o jogo existir
            while (trucoActivity.jogo == null) {
                sleep(200);
            }
            // Roda até a activity-mãe se encerrar, num frame rate que depende
            // de estarmos animando algo ou não (mas sempre atualiza, pra não
            // perder mudanças por algum arredondaento de ms ou por serem
            // instantâneas)
            while (!trucoActivity.isFinishing()) {
                if (visivel) {
                    postInvalidate();
                }
                if (calcTempoAteFimAnimacaoMS() >= 0) {
                    sleep(tempoEntreFramesAnimando);
                } else{
                    sleep(tempoEntreFramesParado);
                }
            }
        }

        private void sleep(int tempoMS) {
            try {
                Thread.sleep(tempoMS);
            } catch (InterruptedException e) {
                // Não faz nada; vamos interromper esse sleep sempre que
                // uma animação começar!
            }

        }
    });

    final Thread respondeDialogos = new Thread() {
        @Override
        public void run() {
            // Aguarda o jogo existir
            while (trucoActivity.jogo == null) {
                try {
                    sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Roda até a activity-mãe se encerrar
            while (!trucoActivity.isFinishing()) {
                try {
                    sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Jogo jogo = trucoActivity.jogo;
                if (recusarMaoDeX) {
                    recusarMaoDeX = false;
                    jogo.decideMaoDeX(trucoActivity.jogadorHumano, false);
                }
                if (aceitarMaoDeX) {
                    aceitarMaoDeX = false;
                    jogo.decideMaoDeX(trucoActivity.jogadorHumano, true);
                }
                if (recusarAumento) {
                    recusarAumento = false;
                    jogo.respondeAumento(trucoActivity.jogadorHumano, false);
                }
                if (aceitarAumento) {
                    aceitarAumento = false;
                    jogo.respondeAumento(trucoActivity.jogadorHumano, true);
                }
            }
        }

    };

    /**
     * Permite à Activity informar se o humano está na própria vez e liberado para jogar,
     * se está na própria vez mas aguarda resposta de um truco, mão de 10/11, etc.,
     * ou se é a vez de outro jogador.
     *
     * @param vezHumano
     *            um entre STATUS_VEZ_HUMANO_OK, STATUS_VEZ_HUMANO_AGUARDANDO e
     *            STATUS_VEZ_OUTRO
     */
    public void setStatusVez(int vezHumano) {
        aguardaFimAnimacoes();
        this.statusVez = vezHumano;
    }

    /**
     * Entrega as cartas iniciais na mão de cada jogador.
     */
    public void distribuiMao() {

        aguardaFimAnimacoes();

        // Distribui as cartas em círculo
        for (int i = 0; i <= 2; i++) {
            for (int j = 1; j <= 4; j++) {
                CartaVisual c = cartas[4 + i + 3 * (j - 1)];
                c.setFechada(true);
                entregaCarta(c, j, i);
            }
        }

        // Atribui o valor correto às cartas do jogador e exibe
        for (int i = 0; i <= 2; i++) {
            CartaVisual c = cartas[4 + i];
            if (isInEditMode()) {
                c.setLetra("A23".charAt(i));
                c.setNaipe(i);
            } else {
                c.copiaCarta(trucoActivity.jogadorHumano.getCartas()[i]);
            }
            c.setFechada(false);
        }

        // Abre o vira, se for manilha nova
        if (!trucoActivity.jogo.getModo().isManilhaVelha()) {
            cartas[0].copiaCarta(trucoActivity.jogo.cartaDaMesa);
            cartas[0].visible = true;
        }

    }

    public void aceitouAumentoAposta(Jogador j, int valor) {
        valorMao = valor;
        if (statusVez == STATUS_VEZ_HUMANO_AGUARDANDO) {
            statusVez = STATUS_VEZ_HUMANO_OK;
        }
    }

    /**
     * Recolhe o vira e as cartas jogadas de volta para o baralho
     */
    public void recolheMao() {
        aguardaFimAnimacoes();
        cartas[0].visible = false;
        for (int i = 4; i <= 15; i++) {
            CartaVisual c = cartas[i];
            if ((c.top != topBaralho) || (c.left != leftBaralho)) {
                c.movePara(leftBaralho, topBaralho, 50);
                c.copiaCarta(null);
                c.descartada = false;
                c.escura = false;
                cartasJogadas.remove(c);
            }
        }
    }

    /**
     * Joga a carta no meio da mesa
     *
     */
    public void descarta(Carta c, int posicao) {

        aguardaFimAnimacoes();

        // Coloca a carta no meio da tela, mas "puxando" na direção
        // de quem jogou
        int topFinal = calcPosTopDescartada(posicao);
        int leftFinal = calcPosLeftDescartada(posicao);

        // Pega uma carta visual naquela posição...
        CartaVisual cv = null;
        for (int i = 0; i <= 2; i++) {
            CartaVisual cvCandidata = cartas[i + 1 + posicao * 3];
            // ...que não tenha sido descartada...
            if (cvCandidata.descartada) {
                continue;
            }
            // ...e, no caso de um humano (ou parceiro em mão de 10/11), que
            // corresponda à carta do jogo
            cv = cvCandidata;
            if (c.equals(cvCandidata)) {
                break;
            }
        }

        // Executa a animação de descarte
        cv.copiaCarta(c);
        cv.movePara(leftFinal, topFinal, 200);
        cv.descartada = true;
        cartasJogadas.addElement(cv);

    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    /**
     *
     * @return posição (x) de uma carta descartada pelo jogador (no meio da
     *         tela, mas puxando para a direçãod dele com um breve distúrbio
     *         aleatório)
     */
    private int calcPosLeftDescartada(int numJogador) {
        int leftFinal;
        leftFinal = getWidth() / 2 - CartaVisual.largura / 2;
        if (numJogador == 2) {
            leftFinal += CartaVisual.largura;
        } else if (numJogador == 4) {
            leftFinal -= CartaVisual.largura;
        }
        leftFinal += System.currentTimeMillis() % 5 - 2;
        return leftFinal;
    }

    /**
     * Exibe uma carta na posição apropriada, animando
     * <p>
     * @param carta
     *               Carta a distribuir
     * @param numJogador
     *            Posição do jogador, de 1 a 4 (1 = humano).
     * @param posicao
     *            posição da carta na mão do jogador (0 a 2)
     */
    private void entregaCarta(CartaVisual carta, int numJogador, int posicao) {
        if (numJogador == 3 || numJogador == 4) {
            posicao = 2 - posicao;
        }
        carta.movePara(calcPosLeftCarta(numJogador, posicao),
                calcPosTopCarta(numJogador, posicao), 85);
    }

    /**
     *
     * @return Posição (x) da i-ésima carta na mão do jogador em questão
     */
    private int calcPosLeftCarta(int numJogador, int i) {
        int deslocamentoHorizontalEntreCartas = CartaVisual.largura * 7 / 8;
        int leftFinal = 0;
        switch (numJogador) {
        case 1:
        case 3:
            leftFinal = (getWidth() / 2) - (CartaVisual.largura / 2) + (i - 1)
                    * deslocamentoHorizontalEntreCartas;
            break;
        case 2:
            leftFinal = getWidth() - CartaVisual.largura - MARGEM;
            break;
        case 4:
            leftFinal = MARGEM;
            break;
        }
        return leftFinal;
    }

    /**
     *
     * @return Posição (y) da i-ésima carta na mão do jogador em questão
     */
    private int calcPosTopCarta(int numJogador, int i) {
        int deslocamentoVerticalEntreCartas = 4;
        int topFinal = 0;
        switch (numJogador) {
        case 1:
            topFinal = getHeight() - (CartaVisual.altura + MARGEM);
            break;
        case 2:
        case 4:
            topFinal = getHeight() / 2 - CartaVisual.altura / 2 - (i - 1)
                    * deslocamentoVerticalEntreCartas;
            break;
        case 3:
            topFinal = MARGEM;
            break;
        }
        return topFinal;
    }

    /**
     *
     * @return posição (y) de uma carta descartada pelo jogador (no meio da
     *         tela, mas puxando para a direçãod dele com um breve distúrbio
     *         aleatório)
     */
    private int calcPosTopDescartada(int numJogador) {
        int topFinal;
        topFinal = getHeight() / 2 - CartaVisual.altura / 2;
        if (numJogador == 1) {
            topFinal += CartaVisual.altura / 2;
        } else if (numJogador == 3) {
            topFinal -= CartaVisual.altura / 2;
        }
        topFinal += System.currentTimeMillis() % 5 - 2;
        return topFinal;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Fundo verde
        canvas.drawRGB(27, 142, 60);

        // Desenha as cartas que já foram jogadas (se houverem),
        // na ordem em que foram jogadas
        for (int i = 0; i < cartasJogadas.size(); i++) {
            try {
                cartasJogadas.elementAt(i).draw(canvas);
            } catch (ArrayIndexOutOfBoundsException e) {
                // Não faz nada (o jogo encerrou no meio de um refresh,
                // por isso a carta não está lá)
            }
        }

        // Desenha as cartas restantes, e o vira por cima de todas
        for (CartaVisual carta : cartas) {
            if (carta != null && !cartasJogadas.contains(carta)
                    && carta != cartas[0]) {
                carta.draw(canvas);
            }
        }
        cartas[0].draw(canvas);

        // Desliga o destaque da carta que fez a rodada e escurece as cartas já
        // descartadas (para não confundir com as próximas)
        long agora = System.currentTimeMillis();
        if ((agora > rodadaPiscaAte) && (numRodadaPiscando > 0)) {
            if (cartaQueFez != null) {
                cartaQueFez.destacada = false;
            }
            numRodadaPiscando = 0;
        }

        // Ícones das rodadas
        if (iconesRodadas != null) {
            for (int i = 0; i <= 3; i++) {
                Bitmap bmpIcone = null;
                if (i == 0) {
                    // O último ícone é o placar da rodada
                    if (valorMao > 0) {
                        bmpIcone = iconesRodadas[valorMao + 10];
                    }
                } else if (i != numRodadaPiscando || (agora / 250) % 2 == 0) {
                    // Desenha se não for a rodada piscando, ou, se for, alterna o
                    // desenho a cada 250ms
                    bmpIcone = iconesRodadas[resultadoRodada[i - 1]];
                }

                if (isInEditMode()) {
                    bmpIcone = iconesRodadas[i == 0 ? 11 : i];
                }

                if (bmpIcone != null) {
                    canvas.drawBitmap(bmpIcone,
                            MARGEM + (i % 2) * (1 + iconesRodadas[0].getWidth()),
                            MARGEM + (i / 2) * (1 + iconesRodadas[0].getHeight()),
                            paintIconesRodadas);
                }
            }
        }

        // Caixa de diálogo (mão de 10/11 ou aumento)
        if (mostrarPerguntaMaoDeX || mostrarPerguntaAumento) {
            String textoPergunta;
            if (mostrarPerguntaAumento) {
                textoPergunta = "Aceita?"; // TODO descrever?
            } else {
                textoPergunta = "Aceita mão de " + trucoActivity.jogo.getModo().pontuacaoParaMaoDeX();
            }
            paintPergunta.setAntiAlias(true);
            paintPergunta.setColor(Color.BLACK);
            paintPergunta.setStyle(Style.FILL);
            canvas.drawRect(rectDialog, paintPergunta);
            paintPergunta.setColor(Color.WHITE);
            paintPergunta.setStyle(Style.STROKE);
            canvas.drawRect(rectDialog, paintPergunta);
            paintPergunta.setTextSize(tamanhoFonte * 0.5f);
            paintPergunta.setTextAlign(Align.CENTER);
            paintPergunta.setStyle(Style.FILL);
            canvas.drawText(textoPergunta, rectDialog.centerX(),
                    rectDialog.top + paintPergunta.getTextSize() * 1.5f, paintPergunta);
            desenhaBotao("Sim", canvas, rectBotaoSim);
            desenhaBotao("Nao", canvas, rectBotaoNao);
        }

        desenhaBalao(canvas);
        desenhaIndicadorDeVez(canvas);

        if (trucoActivity != null && trucoActivity.jogo.isJogoAutomatico()) {
            jogaCarta(0);
            jogaCarta(1);
            jogaCarta(2);
            respondePergunta(rand.nextBoolean());
        }
    }

    private void desenhaBotao(String texto, Canvas canvas, RectF outerRect) {
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        paint.setTextSize(tamanhoFonte * 0.75f);
        // Borda
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(outerRect, tamanhoFonte * 4 / 5,
                tamanhoFonte * 4 / 5, paint);
        // Interior
        paint.setColor(Color.BLACK);
        RectF innerRect = new RectF(outerRect.left + 4, outerRect.top + 4,
                outerRect.right - 4, outerRect.bottom - 4);
        canvas.drawRoundRect(innerRect, tamanhoFonte * 4 / 5,
                tamanhoFonte * 4 / 5, paint);
        // Texto
        paint.setStyle(Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Align.CENTER);
        canvas.drawText(texto, outerRect.centerX(), outerRect.centerY() - tamanhoFonte * 0.2f
                + tamanhoFonte * 0.5f, paint);
    }

    /**
     * Cache dos ícones que informam o resultado das rodadas
     */
    public static Bitmap[] iconesRodadas;

    /**
     * Resultado das rodadas (0=não jogada; 1=vitória; 2=derrota; 3=empate)
     */
    protected final int[] resultadoRodada = { 0, 0, 0 };

    /**
     * Margem entre a mesa e as cartas
     */
    public static final int MARGEM = 2;

    /**
     * Posição do baralho na mesa
     */
    private int topBaralho, leftBaralho;

    /**
     * Timestamp em que as animações em curso irão acabar
     */
    private long animandoAte = System.currentTimeMillis();

    /**
     * Indica que é a vez do humano, e ele pode jogar
     */
    public static final int STATUS_VEZ_HUMANO_OK = 1;

    /**
     * Indica que é a vez de outro jogador
     */
    public static final int STATUS_VEZ_OUTRO = 0;

    /**
     * Indica que é a vez do humano, e ele está aguardando resposta (ex.: de
     * aumento) para jogar
     */
    public static final int STATUS_VEZ_HUMANO_AGUARDANDO = -1;

    /**
     * Diz se é a vez do jogador humano dessa mesa ou de outro, e, no primeiro
     * caso se está aguardando resposta de truco
     */
    private int statusVez = 0;

    /**
     * Guarda as cartas que foram jogadas pelos jogadores (para saber em que
     * ordem desenhar)
     */
    private final Vector<CartaVisual> cartasJogadas = new Vector<>(12);

    private int numRodadaPiscando = 0;

    private long rodadaPiscaAte = System.currentTimeMillis();

    /**
     * Carta que "fez" a última rodada (para fins de destaque)
     */
    private CartaVisual cartaQueFez;

    public boolean mostrarPerguntaMaoDeX = false;

    private boolean recusarMaoDeX = false;
    private boolean aceitarMaoDeX = false;

    public boolean mostrarPerguntaAumento = false;

    private boolean recusarAumento = false;
    private boolean aceitarAumento = false;

    private int posicaoBalao = 1;

    private long mostraBalaoAte = System.currentTimeMillis();

    private String fraseBalao = null;

    private void desenhaIndicadorDeVez(Canvas canvas) {
        if (statusVez == STATUS_VEZ_HUMANO_AGUARDANDO) {
            return;
        }
        Paint paintSetaVez = new Paint();
        paintSetaVez.setColor(Color.YELLOW);
        paintSetaVez.setTextAlign(Align.CENTER);
        paintSetaVez.setTextSize(CartaVisual.altura / 3);
        switch (posicaoVez) {
        case 1:
            canvas.drawText("\u21E9", getWidth() / 2, getHeight()
                    - CartaVisual.altura * 14 / 12, paintSetaVez);
            break;
        case 2:
            canvas.drawText("\u21E8", getWidth() - CartaVisual.largura * 15
                    / 12, getHeight() / 2, paintSetaVez);
            break;
        case 3:
            canvas.drawText("\u21E7", getWidth() / 2,
                    CartaVisual.altura * 16 / 12, paintSetaVez);
            break;
        case 4:
            canvas.drawText("\u21E6", CartaVisual.largura * 15 / 12,
                    getHeight() / 2, paintSetaVez);
            break;
        }
    }

    /**
     * Desenha a parte gráfica do balão (sem o texto). O nome é meio mentiroso,
     * porque também desenha a ponta. É chamada várias vezes para compor o
     * contorno, antes de estampar o texto
     *
     * @param canvas
     *            onde ele será desenhado
     * @param x
     *            esquerda
     * @param y
     *            topo
     * @param largBalao
     *            largura
     * @param altBalao
     *            altura
     * @param quadrantePonta
     *            Quadrante (cartesiano) onde aparece a ponta do balão (com
     *            relação a ele mesmo)
     */
    private void desenhaElipseBalao(Canvas canvas, int x, int y, int largBalao,
            int altBalao, int quadrantePonta, Paint paint) {
        // Elipse principal
        paint.setAntiAlias(true);
        canvas.drawArc(new RectF(x, y, x + largBalao - 1, y + altBalao - 1), 0,
                360, false, paint);
        // Ponta (é um triângulo que desenhamos linha a linha)
        paint.setAntiAlias(false);
        int xi;
        for (int i = 0; i < altBalao; i++) {
            if (quadrantePonta == 2 || quadrantePonta == 3) {
                xi = x + altBalao * 3 / 2 - i;
            } else {
                xi = x - altBalao * 3 / 2 + i + largBalao;
            }
            int sinaly = quadrantePonta < 3 ? -1 : 1;
            canvas.drawLine(xi, y + altBalao / 2, xi, y + altBalao / 2 + i
                    * sinaly, paint);
        }
    }

    /**
     * Desenha o balão no lugar certo, se ele estiver visível
     *
     * @param canvas
     *            canvas onde ele será (ou não) desenhado.
     */
    private void desenhaBalao(Canvas canvas) {
        if (fraseBalao != null && mostraBalaoAte > System.currentTimeMillis()) {

            // Determina o tamanho e a posição do balão e o quadrante da
            // ponta
            final int MARGEM_BALAO_LEFT = (int) tamanhoFonte;
            final int MARGEM_BALAO_TOP = (int) tamanhoFonte / 2;
            Paint paintFonte = new Paint();
            paintFonte.setAntiAlias(true);
            paintFonte.setTextSize(tamanhoFonte);
            Rect bounds = new Rect();
            paintFonte.setColor(Color.BLACK);
            paintFonte
                    .getTextBounds(fraseBalao, 0, fraseBalao.length(), bounds);

            int largBalao = bounds.width() + 2 * MARGEM_BALAO_LEFT;
            int altBalao = bounds.height() + 2 * MARGEM_BALAO_TOP;
            int x = 0, y = 0;
            int quadrantePonta = 0;
            switch (posicaoBalao) {
            case 1:
                x = (canvas.getWidth() - largBalao) / 2 - CartaVisual.largura;
                y = canvas.getHeight() - altBalao * 4 - MARGEM - 3;
                quadrantePonta = 4;
                break;
            case 2:
                x = canvas.getWidth() - largBalao - MARGEM - 3;
                y = (canvas.getHeight() - altBalao) / 2;
                quadrantePonta = 1;
                break;
            case 3:
                x = (canvas.getWidth() - largBalao) / 2 + CartaVisual.largura;
                y = MARGEM + 3 + altBalao / 2;
                quadrantePonta = 2;
                break;
            case 4:
                x = MARGEM + 3;
                y = (canvas.getHeight() - altBalao) / 2 - CartaVisual.altura;
                quadrantePonta = 3;
                break;
            }

            // O balão tem que ser branco, com uma borda preta. Como
            // ele só aparece em um refresh, vamos pela força bruta,
            // desenhando ele deslocado em torno da posição final em
            // preto e em seguida desenhando ele em branco na posição
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.BLACK);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    desenhaElipseBalao(canvas, x + i, y + j, largBalao,
                            altBalao, quadrantePonta, paint);
                }
            }
            paint.setColor(corFundoCartaBalao);
            desenhaElipseBalao(canvas, x, y, largBalao, altBalao,
                    quadrantePonta, paint);

            // Finalmente, escreve o texto do balão
            paint.setAntiAlias(true);
            canvas.drawText(fraseBalao, x + MARGEM_BALAO_LEFT, y + altBalao
                    - MARGEM_BALAO_TOP - 2, paintFonte);

        } else {
            fraseBalao = null;
        }

    }

    private boolean visivel = false;

    public boolean vaiJogarFechada;

    public void setPosicaoVez(int posicaoVez) {
        this.posicaoVez = posicaoVez;
    }

    public void setValorMao(int m) {
        valorMao = m;
    }

    public void setCorFundoCartaBalao(int corFundoCartaBalao) {
        this.corFundoCartaBalao = corFundoCartaBalao;
    }
}
